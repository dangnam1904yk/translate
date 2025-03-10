package com.translate.trans.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.translate.trans.model.Request.ContentText;
import com.translate.trans.model.Request.GenerationConfig;
import com.translate.trans.model.Request.Part;
import com.translate.trans.model.Request.RequestBodySend;
import com.translate.trans.model.Request.RequestUser;
import com.translate.trans.model.Response.RequestBodyResponse;
import com.translate.trans.model.error.ErrorResponseData;
import com.translate.trans.model.error.ErrorResponseHead;
import com.translate.trans.until.Constain;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class RestChatController {

    @Value("${spring.key.gemini.url}")
    private String URL_GEMINI;

    @Value("${spring.key.gemini.topk}")
    private double TOP_K;
    @Value("${spring.key.gemini.topp}")
    private double TOP_P;
    // @Value("${spring.key.gemini.temperature}")
    private double TEMPERATURE;
    @Value("${spring.key.gemini.maxOutputTokens}")
    private int MAX_OUT_PUT_TOKENS;
    @Value("${spring.key.gemini.responseMimeType}")
    private String RESPONSE_MIME_TYPE;

    private String MODEL_GEMINI;
    private String API_KEY;
    private List<ContentText> listHistory = new ArrayList<>();

    @GetMapping("/check-key")
    public ResponseEntity<?> getMethodName(
            @RequestParam("type") String typeModel,
            @RequestParam("apiKey") String apiKey,
            @RequestParam("temperature") double temperature) throws InterruptedException, URISyntaxException {
        StringBuilder url = new StringBuilder();
        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_EXP)) {
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_EXP;
            url.append(
                    URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_EXP
                            + apiKey);
        }
        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_THINK_EXP_01_21)) {
            url.append(
                    URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_THINK_EXP_01_21
                            + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_THINK_EXP_01_21;
        }
        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_02_05)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_02_05
                    + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_02_05;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_FLASH;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_PRE_02_05)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_PRE_02_05 + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_PRE_02_05;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_FLASH)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_FLASH + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_1_5_FLASH;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_PRO)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_PRO + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_1_5_PRO;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_FLASH_8B)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_FLASH_8B + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_1_5_FLASH_8B;
        }

        if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_UNLIMITED)) {
            url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_UNLIMITED + apiKey);
            MODEL_GEMINI = Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_UNLIMITED;
        }

        API_KEY = apiKey;
        TEMPERATURE = temperature;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        Gson gson = new Gson();
        List<ContentText> listHistory = new ArrayList<>();
        List<Part> parts = Collections.singletonList(new Part("Xin chào, bạn là ai ?"));
        listHistory.add(new ContentText(parts, "user"));
        GenerationConfig config = new GenerationConfig(temperature, TOP_K, TOP_P,
                MAX_OUT_PUT_TOKENS,
                RESPONSE_MIME_TYPE);
        RequestBodySend requestBody = new RequestBodySend(listHistory, config);
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String bodyData = gson.toJson(requestBody);

        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(new URI(url.toString()))
                .POST(HttpRequest.BodyPublishers.ofString(bodyData, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> responseData;
        RequestBodyResponse responseGemini = null;
        try {
            responseData = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseData.body() == null || responseData.body().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ERROR  GEMINI");
            }

            try {
                responseGemini = gson.fromJson(responseData.body(),
                        RequestBodyResponse.class);
            } catch (IllegalStateException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ERROR  GEMINI");
            }
            if (responseGemini.getCandidates() != null
                    && !responseGemini.getCandidates().isEmpty()
                    && !responseGemini.getCandidates().get(0).content.getParts()
                            .isEmpty()) {

                String dataResponse = responseGemini.getCandidates().get(0).content.getParts()
                        .get(0)
                        .getText();
                listHistory.add(responseGemini.getCandidates().get(0).content);
                return ResponseEntity.ok().body(responseGemini.getCandidates().get(0).content);
            } else {
                ErrorResponseHead responseHead = gson.fromJson(responseData.body(),
                        ErrorResponseHead.class);
                ErrorResponseData error = responseHead.getError();
                if (error != null) {
                    System.out
                            .println(String.format(
                                    "Ma code loi: %d, message: %s, status = %s",
                                    error.getCode(), error.getMessage(),
                                    error.getStatus()));
                } else {
                    System.out.println(responseData.body().toString());
                }
                return ResponseEntity.ok().body(responseData.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/start-chat")
    public ResponseEntity<?> startChat(@RequestBody RequestUser requestUser)
            throws InterruptedException, URISyntaxException {
        StringBuilder url = new StringBuilder();
        url.append(URL_GEMINI + MODEL_GEMINI + API_KEY);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        Gson gson = new Gson();
        List<Part> parts = Collections.singletonList(new Part(requestUser.getRequest()));
        listHistory.add(new ContentText(parts, "user"));
        GenerationConfig config = new GenerationConfig(TEMPERATURE, TOP_K, TOP_P,
                MAX_OUT_PUT_TOKENS,
                RESPONSE_MIME_TYPE);
        RequestBodySend requestBody = new RequestBodySend(listHistory, config);
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String bodyData = gson.toJson(requestBody);

        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(new URI(url.toString()))
                .POST(HttpRequest.BodyPublishers.ofString(bodyData, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> responseData;
        RequestBodyResponse responseGemini = null;
        try {
            responseData = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseData.body() == null || responseData.body().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ERROR  GEMINI");
            }

            try {
                responseGemini = gson.fromJson(responseData.body(),
                        RequestBodyResponse.class);
            } catch (IllegalStateException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ERROR  GEMINI");
            }
            if (responseGemini.getCandidates() != null
                    && !responseGemini.getCandidates().isEmpty()
                    && !responseGemini.getCandidates().get(0).content.getParts()
                            .isEmpty()) {

                String dataResponse = responseGemini.getCandidates().get(0).content.getParts()
                        .get(0)
                        .getText();
                listHistory.add(responseGemini.getCandidates().get(0).content);
                return ResponseEntity.ok().body(responseGemini.getCandidates().get(0).content);
            } else {
                ErrorResponseHead responseHead = gson.fromJson(responseData.body(),
                        ErrorResponseHead.class);
                ErrorResponseData error = responseHead.getError();
                if (error != null) {
                    System.out
                            .println(String.format(
                                    "Ma code loi: %d, message: %s, status = %s",
                                    error.getCode(), error.getMessage(),
                                    error.getStatus()));
                } else {
                    System.out.println(responseData.body().toString());
                }
                return ResponseEntity.ok().body(responseData.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("resetHistory")
    public ResponseEntity<?> getMethodName() {
        listHistory = new ArrayList<>();
        return ResponseEntity.ok().body("succes");
    }

}
