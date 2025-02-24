package com.translate.trans.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;

public class GeminiImageDescription {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        String imagePath = "img.jpg"; // Thay đổi đường dẫn đến hình ảnh của bạn
        String apiKey = "YOUR_GOOGLE_API_KEY"; // Thay đổi API key của bạn
        String file = "input.txt";

        // try {
        // String fileContent = readFileContent(file);
        // String jsonPayload = createJsonPayload1(fileContent);
        // String response = sendRequest1(jsonPayload);
        // System.out.println(response);
        // // process the json response.
        // processJsonResponse(response);

        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        try {
            String base64Image = encodeImageToBase64(file);
            String jsonPayload = createJsonPayload(base64Image);
            String response = sendRequest(jsonPayload);
            System.out.println(response); // In phản hồi từ API
            // process the json response.
            processJsonResponse(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String encodeImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String createJsonPayload1(String fileContent) {
        JSONObject textPart = new JSONObject();
        textPart.put("text", fileContent);

        JSONArray parts = new JSONArray();
        parts.put(textPart);

        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        content.put("parts", parts);
        contents.put(content);

        JSONObject payload = new JSONObject();
        payload.put("contents", contents);

        return payload.toString();
    }

    private static String sendRequest1(String jsonPayload)
            throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();

        // RequestBody body = RequestBody.(jsonPayload, "application/json");
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(new URI(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyCKZj6FQVPKYc7eBd8aIODrfnUz5ribNao"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body() != null) {
            return response.body();
        } else {
            // throw new IOException("Request failed: " + response.code() + " " +
            // response.message());
        }
        return response.body().toString();

    }

    private static void processJsonResponse1(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        // Example of how to extract the text from the response.
        try {
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONArray parts = candidate.getJSONObject("content").getJSONArray("parts");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    result.append(part.getString("text"));
                }
            }
            System.out.println("Extracted text: " + result.toString());
        } catch (Exception e) {
            System.out.println("Error processing JSON response.");
            e.printStackTrace();
        }
    }

    private static String createJsonPayload(String base64Image) {
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", "text/plain");
        inlineData.put("data", base64Image);

        JSONObject imagePart = new JSONObject();
        imagePart.put("inline_data", inlineData);

        JSONObject textPart = new JSONObject();
        textPart.put("text", "Tell me about this instrument");

        JSONArray parts = new JSONArray();
        parts.put(textPart);
        parts.put(imagePart);

        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        content.put("parts", parts);
        contents.put(content);

        JSONObject payload = new JSONObject();
        payload.put("contents", contents);

        return payload.toString();
    }

    private static String sendRequest(String jsonPayload) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();

        // RequestBody body = RequestBody.(jsonPayload, "application/json");
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(new URI(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyCKZj6FQVPKYc7eBd8aIODrfnUz5ribNao"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body() != null) {
            return response.body();
        } else {
            // throw new IOException("Request failed: " + response.code() + " " +
            // response.message());
        }
        return response.body().toString();

    }

    private static void processJsonResponse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        // Example of how to extract the text from the response.
        try {
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONArray parts = candidate.getJSONObject("content").getJSONArray("parts");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    result.append(part.getString("text"));
                }
            }
            System.out.println("Extracted text: " + result.toString());
        } catch (Exception e) {
            System.out.println("Error processing JSON response.");
            e.printStackTrace();
        }
    }
}