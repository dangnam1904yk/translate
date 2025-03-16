package com.translate.trans.controller;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.darkprograms.speech.translator.GoogleTranslate;
import com.translate.trans.service.DocxProcessorService;
import com.translate.trans.service.TranslationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.cloud.translate.v3.*;

@RestController
@RequestMapping("/api/docx")
public class DocxController {

    @Autowired
    private DocxProcessorService docxProcessorService;
    @Autowired
    private TranslationService translationService;

    @PostMapping("/process")
    public ResponseEntity<InputStreamResource> processDocx(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.badRequest().body(null);
        }

        ByteArrayInputStream copiedDoc = docxProcessorService.copyDocxWithImages(file);
        // ByteArrayInputStream copiedDoc = docxProcessorService.readDocxWithImg(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_with_images.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(copiedDoc));
    }

    @PostMapping("/process1")
    public ResponseEntity<InputStreamResource> processDocx1(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.badRequest().body(null);
        }

        // 1. Đọc và lấy văn bản từ file DOCX
        String extractedText = docxProcessorService.extractText(file);

        // 2. Xử lý văn bản ở đây (nếu cần)
        // Ví dụ: bạn có thể thêm thay đổi vào extractedText nếu cần xử lý dữ liệu

        // 3. Ghi lại văn bản vào file mới (giữ nguyên hình ảnh, bảng biểu)
        ByteArrayInputStream newDoc = docxProcessorService.writeDocxWithText(extractedText, file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_with_text.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(newDoc));
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertEmfToDocx(@RequestParam("file") MultipartFile file) {
        try {
            File pngFile = docxProcessorService.convertEmfToPng(file.getInputStream());
            ByteArrayOutputStream docxOutput = docxProcessorService.insertPngToDocx(pngFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.docx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(docxOutput.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/documents")
    public ResponseEntity<byte[]> translateDocuments(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sourceLanguageCode") String sourceLanguageCode,
            @RequestParam("targetLanguageCode") String targetLanguageCode,
            @RequestParam("projectId") String projectId,
            @RequestParam("location") String location) {

        try {
            byte[] translatedDocument = translationService.translateDocument(
                    file, sourceLanguageCode, targetLanguageCode, projectId, location);

            // Sử dụng mimeType từ response của API
            TranslateDocumentResponse response = translationService.getResponse(file, sourceLanguageCode,
                    targetLanguageCode, projectId, location);
            String mimeType = response.getDocumentTranslation().getMimeType();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType)); // Sử dụng mimeType
            headers.setContentDispositionFormData("attachment", "translated_" + file.getOriginalFilename());

            return new ResponseEntity<>(translatedDocument, headers, HttpStatus.OK);

        } catch (IOException e) {
            // Xử lý lỗi tốt hơn trong ứng dụng thực tế
            e.printStackTrace(); // Log lỗi
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Xử lý các exception khác
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final int MAX_PAGES_PER_CHUNK = 20;

    @PostMapping("/document")
    public ResponseEntity<byte[]> translateDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sourceLanguageCode") String sourceLanguageCode,
            @RequestParam("targetLanguageCode") String targetLanguageCode,
            @RequestParam("projectId") String projectId,
            @RequestParam("location") String location) {

        try {
            // 1. Chia nhỏ PDF
            List<byte[]> pdfChunks = splitPdf(file);

            // 2. Dịch từng chunk
            ByteArrayOutputStream translatedOutput = new ByteArrayOutputStream();
            String mimeType = "";
            for (int i = 0; i < pdfChunks.size(); i++) {
                byte[] chunk = pdfChunks.get(i);

                // Tạo một MultipartFile giả từ byte[]
                MultipartFile chunkFile = new CustomMultipartFile(chunk, file.getOriginalFilename(),
                        file.getContentType());

                TranslateDocumentResponse response = translationService.getResponse(chunkFile, sourceLanguageCode,
                        targetLanguageCode, projectId, location);

                // byte[] translatedDocument = translationService.translateDocument(
                // chunkFile, sourceLanguageCode, targetLanguageCode, projectId, location);

                mimeType = response.getDocumentTranslation().getMimeType();
                translatedOutput.write(response.getDocumentTranslation().toByteArray());
            }

            byte[] translatedDocuments = translatedOutput.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDispositionFormData("attachment", "translated_" + file.getOriginalFilename());

            return new ResponseEntity<>(translatedDocuments, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<byte[]> splitPdf(MultipartFile file) throws IOException {
        List<byte[]> chunks = new java.util.ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
                PDDocument document = PDDocument.load(inputStream)) {

            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(MAX_PAGES_PER_CHUNK);

            List<PDDocument> pages = splitter.split(document);

            for (PDDocument page : pages) {
                try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                    page.save(out);
                    chunks.add(out.toByteArray());
                } finally {
                    page.close();
                }
            }
        }
        return chunks;
    }

    // Inner class để tạo MultipartFile từ byte[]
    private static class CustomMultipartFile implements MultipartFile {

        private final byte[] content;
        private final String originalFilename;
        private final String contentType;

        public CustomMultipartFile(byte[] content, String originalFilename, String contentType) {
            this.content = content;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "file"; // Or any name you prefer
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            // Not needed for this use case, but you could implement it if necessary
            throw new UnsupportedOperationException("transferTo is not supported");
        }
    }
}