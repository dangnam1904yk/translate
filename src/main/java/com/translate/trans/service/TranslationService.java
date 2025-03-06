package com.translate.trans.service;

import com.google.cloud.translate.v3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class TranslationService {

    public byte[] translateDocument(
            MultipartFile file, // Sử dụng MultipartFile cho file upload
            String sourceLanguageCode,
            String targetLanguageCode,
            String projectId,
            String location) throws IOException {

        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            DocumentInputConfig documentInputConfig = DocumentInputConfig.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(file.getBytes()))
                    .setMimeType(file.getContentType())
                    .build();

            TranslateDocumentRequest request = TranslateDocumentRequest.newBuilder()
                    .setParent(LocationName.of(projectId, location).toString())
                    .setSourceLanguageCode(sourceLanguageCode)
                    .setTargetLanguageCode(targetLanguageCode)
                    .setDocumentInputConfig(documentInputConfig)
                    .build();

            TranslateDocumentResponse response = client.translateDocument(request);
            return response.getDocumentTranslation().getByteStreamOutputs(0).toByteArray();

        }
    }

    public TranslateDocumentResponse getResponse(
            MultipartFile file,
            String sourceLanguageCode,
            String targetLanguageCode,
            String projectId,
            String location) throws IOException {
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            DocumentInputConfig documentInputConfig = DocumentInputConfig.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(file.getBytes()))
                    .setMimeType(file.getContentType())
                    .build();
            TranslateDocumentRequest request = TranslateDocumentRequest.newBuilder()
                    .setParent(LocationName.of(projectId, location).toString())
                    .setSourceLanguageCode(sourceLanguageCode)
                    .setTargetLanguageCode(targetLanguageCode)
                    .setDocumentInputConfig(documentInputConfig)
                    .build();

            return client.translateDocument(request);

        }

    }
}