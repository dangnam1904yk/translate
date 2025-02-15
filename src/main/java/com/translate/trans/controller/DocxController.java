package com.translate.trans.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.darkprograms.speech.translator.GoogleTranslate;
import com.translate.trans.service.DocxProcessorService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

@RestController
@RequestMapping("/api/docx")
public class DocxController {

    @Autowired
    private DocxProcessorService docxProcessorService;

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
}