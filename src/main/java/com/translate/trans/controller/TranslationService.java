package com.translate.trans.controller;

import com.google.cloud.translate.v3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.google.protobuf.ByteString;

public interface TranslationService {
    TranslateDocumentResponse getResponse(MultipartFile file, String sourceLanguageCode, String targetLanguageCode,
            String projectId, String location) throws IOException;
}

@Service
class TranslationServiceImpl implements TranslationService {
    public TranslateDocumentResponse getResponse(MultipartFile file, String sourceLanguageCode,
            String targetLanguageCode, String projectId, String location) throws IOException {
        try (TranslationServiceClient client = TranslationServiceClient.create()) {

            LocationName parent = LocationName.of(projectId, location);

            DocumentInputConfig documentInputConfig = DocumentInputConfig.newBuilder()
                    .setContent(ByteString.readFrom(file.getInputStream()))
                    .setMimeType(file.getContentType()) // Set the MIME type
                    .build();

            TranslateDocumentRequest request = TranslateDocumentRequest.newBuilder()
                    .setParent(parent.toString())
                    .setSourceLanguageCode(sourceLanguageCode)
                    .setTargetLanguageCode(targetLanguageCode)
                    .setDocumentInputConfig(documentInputConfig)

                    .build();

            TranslateDocumentResponse response = client.translateDocument(request);
            return response;

        } catch (Exception e) {
            throw e;
        }
    }
}