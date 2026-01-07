package com.translator.application.service;

import com.translator.domain.model.Book;
import com.translator.domain.model.Page;
import com.translator.domain.valueobject.BookMetadata;
import com.translator.domain.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service for handling EPUB file processing: parsing, unzipping, updating, and zipping.
 */
@Service
public class EpubProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(EpubProcessingService.class);

    /**
     * Parses the EPUB file by unzipping it and reading its structure.
     */
    public Book parseEpub(String filePath) throws TranslationException {
        Path tempDir = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("EPUB file not found: " + filePath);
            }

            // Create Book with source path
            Book book = new Book(filePath);

            // Unzip to temp dir to read structure
            tempDir = Files.createTempDirectory("epub_parse_" + UUID.randomUUID());
            unzip(file, tempDir.toFile());

            // Parse container.xml to find OPF
            File containerFile = new File(tempDir.toFile(), "META-INF/container.xml");
            if (!containerFile.exists()) {
                 throw new TranslationException("Invalid EPUB: META-INF/container.xml not found");
            }

            String opfPath = parseContainerXml(containerFile);
            File opfFile = new File(tempDir.toFile(), opfPath);
            if (!opfFile.exists()) {
                throw new TranslationException("OPF file not found at: " + opfPath);
            }

            // Parse OPF to populate Book pages
            parseOpf(book, opfFile, tempDir.toFile());

            logger.info("EPUB parsed: {}, {} pages", file.getName(), book.getTotalPages());
            return book;

        } catch (Exception e) {
            logger.error("Failed to parse EPUB: {}", filePath, e);
            throw new TranslationException("Failed to parse EPUB: " + e.getMessage(), e);
        } finally {
            // Clean up temp dir
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    /**
     * Creates a translated EPUB by updating the original files with translated content.
     */
    public String createTranslatedEpub(Book book, String outputDirectory) throws TranslationException {
        Path tempDir = null;
        try {
            File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Unzip original to temp
            tempDir = Files.createTempDirectory("epub_gen_" + UUID.randomUUID());
            unzip(new File(book.getSourceFilePath()), tempDir.toFile());

            // Update files with translated content
            for (Page page : book.getPages()) {
                if (page.isTranslated() && page.getTranslatedContent() != null && !page.getTranslatedContent().isEmpty()) {
                    // Page ID is the relative path from zip root
                    File pageFile = new File(tempDir.toFile(), page.getId());
                    if (pageFile.exists()) {
                        // Write translated content, preserving UTF-8
                        Files.writeString(pageFile.toPath(), page.getTranslatedContent(), StandardCharsets.UTF_8);
                    } else {
                        logger.warn("Page file not found for replacement: {}", page.getId());
                    }
                }
            }

            // Zip to output
            String outputFileName = book.getTranslatedFileName();
            File outputFile = new File(outputDir, outputFileName);
            
            // Re-zip
            zip(tempDir.toFile(), outputFile);

            logger.info("Translated EPUB created: {}", outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            logger.error("Failed to create translated EPUB", e);
            throw new TranslationException("Failed to create EPUB: " + e.getMessage(), e);
        } finally {
             if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    private String parseContainerXml(File containerFile) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(containerFile);
        doc.getDocumentElement().normalize();

        NodeList rootfiles = doc.getElementsByTagName("rootfile");
        if (rootfiles.getLength() > 0) {
            Element rootfile = (Element) rootfiles.item(0);
            return rootfile.getAttribute("full-path");
        }
        throw new Exception("No rootfile found in container.xml");
    }

    private void parseOpf(Book book, File opfFile, File zipRoot) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(opfFile);
        doc.getDocumentElement().normalize();

        // Simple Metadata Extraction
        String title = getTagValue(doc, "title"); // try local name
        if (title == null) title = getTagValue(doc, "dc:title");
        
        String author = getTagValue(doc, "creator");
        if (author == null) author = getTagValue(doc, "dc:creator");
        
        String language = getTagValue(doc, "language");
        if (language == null) language = getTagValue(doc, "dc:language");

        book.updateMetadata(new BookMetadata(
            title != null ? title : "Unknown Title",
            author != null ? List.of(author) : List.of("Unknown Author"),
            language != null ? language : "en",
            "", "", ""
        ));

        // Manifest: Map ID -> Href
        Map<String, String> manifest = new HashMap<>();
        NodeList items = doc.getElementsByTagName("item");
        // Also try with prefix if not found, but usually DOM getElementsByTagName searches all
        if (items.getLength() == 0) items = doc.getElementsByTagName("opf:item");
        
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String id = item.getAttribute("id");
            String href = item.getAttribute("href");
            manifest.put(id, href);
        }

        // Spine: Reading Order
        NodeList itemrefs = doc.getElementsByTagName("itemref");
        if (itemrefs.getLength() == 0) itemrefs = doc.getElementsByTagName("opf:itemref");
        
        Path opfDir = opfFile.getParentFile().toPath();
        Path rootDir = zipRoot.toPath();

        for (int i = 0; i < itemrefs.getLength(); i++) {
            Element itemref = (Element) itemrefs.item(i);
            String idref = itemref.getAttribute("idref");
            String href = manifest.get(idref);
            
            if (href != null) {
                // Resolve full path relative to zip root
                Path fullPath = opfDir.resolve(href).normalize();
                
                // Read content
                String content = Files.readString(fullPath, StandardCharsets.UTF_8);
                
                // Calculate ID (relative path from zip root) for Page object
                String relativePath = rootDir.relativize(fullPath).toString().replace("\\", "/");
                
                Page page = new Page(
                    relativePath, // ID is the relative file path
                    i + 1,
                    "Page " + (i + 1), // Simple title
                    content
                );
                book.addPage(page);
            }
        }
    }

    private String getTagValue(Document doc, String tagName) {
        NodeList list = doc.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return null;
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
    
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private void zip(File sourceDir, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = sourceDir.toPath();
            
            // Handle mimetype specifically if it exists to be first and uncompressed
            File mimetypeFile = new File(sourceDir, "mimetype");
            if (mimetypeFile.exists()) {
                 ZipEntry mimetypeEntry = new ZipEntry("mimetype");
                 mimetypeEntry.setMethod(ZipEntry.STORED);
                 mimetypeEntry.setSize(mimetypeFile.length());
                 mimetypeEntry.setCrc(calculateCrc(mimetypeFile));
                 zos.putNextEntry(mimetypeEntry);
                 Files.copy(mimetypeFile.toPath(), zos);
                 zos.closeEntry();
            }
            
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> !path.getFileName().toString().equals("mimetype"))
                .forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString().replace("\\", "/"));
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }
    }
    
    private long calculateCrc(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                crc.update(buffer, 0, count);
            }
            return crc.getValue();
        }
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        file.delete();
    }
}