package com.translate.trans.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.darkprograms.speech.translator.GoogleTranslate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.translate.trans.model.LanguageConfig;
import com.translate.trans.model.LanguageOption;
import com.translate.trans.model.Request.ContentText;
import com.translate.trans.model.Request.GenerationConfig;
import com.translate.trans.model.Request.Part;
import com.translate.trans.model.Request.RequestBodySend;
import com.translate.trans.model.Response.RequestBodyResponse;
import com.translate.trans.model.error.*;
import com.translate.trans.service.DocxProcessorService;
import com.translate.trans.service.ExcelExportService;
import com.translate.trans.until.Constain;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.InputStream;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private DocxProcessorService docxProcessorService;

    @Value("${spring.key.gemini.token}")
    private String apiKey;

    @Value("${spring.key.gemini.url}")
    private String URL_GEMINI;

    @Value("${spring.key.gemini.topk}")
    private double TOP_K;
    @Value("${spring.key.gemini.topp}")
    private double TOP_P;
    @Value("${spring.key.gemini.temperature}")
    private double TEMPERATURE;
    @Value("${spring.key.gemini.maxOutputTokens}")
    private int MAX_OUT_PUT_TOKENS;
    @Value("${spring.key.gemini.responseMimeType}")
    private String RESPONSE_MIME_TYPE;

    @GetMapping("/")
    public String getMethodName() {
        return "index1";
    }

    private static void extractImageSize(long widthEMU, long heightEMU, XWPFPicture picture) {
        int widthPx = (int) (widthEMU / Units.EMU_PER_PIXEL);
        int heightPx = (int) (heightEMU / Units.EMU_PER_PIXEL);

        System.out.println("Imgae: " + picture.getPictureData().getFileName());
        System.out.println("Size img: " + widthPx + " x " + heightPx + " px");
    }

    @PostMapping("/convert")
    public String convertDocxToXml(@RequestParam("file") MultipartFile file, Model model) {
        StringBuilder xmlContent = new StringBuilder();

        try (InputStream inputStream = file.getInputStream()) {
            // Open the DOCX file as a ZIP input stream
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;

            // Iterate through the entries in the DOCX file
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Look for the main document XML file (usually "word/document.xml")
                if (entry.getName().equals("word/document.xml")) {
                    StringWriter writer = new StringWriter();
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        writer.write(new String(buffer, 0, len));
                    }
                    xmlContent.append(writer.toString());
                    break;
                }
            }
            zipInputStream.close();

        } catch (IOException e) {
            model.addAttribute("error", "Error processing file: " + e.getMessage());
            return "uploadForm";
        }

        // Add the XML content to the model to be displayed in the Thymeleaf template
        model.addAttribute("xmlContent", xmlContent.toString());
        return "displayXml";
    }

    @PostMapping("/upload")
    public void exportWord(HttpServletResponse response,
            @RequestParam("file") MultipartFile file,
            @RequestParam("sourceLanguage") String sourceLanguage,
            @RequestParam("targetLanguage") String targetLanguage,
            @RequestParam("type") String typeModel,
            @RequestParam("apiKey") String apiKey,
            @RequestParam("requestModel") String requestModel,
            @RequestParam("temperature") double temperature,
            @RequestParam("maxLength") long maxLength,
            Model model)
            throws IOException, InvalidFormatException, URISyntaxException,
            ArrayIndexOutOfBoundsException,
            IndexOutOfBoundsException,
            InterruptedException {

        long waitTime = 2000;
        int maxRetries = Integer.MAX_VALUE;
        int retryCount = 0;
        List<ContentText> listHistory = new ArrayList<>();

        Optional<LanguageOption> resultfindOptional1 = LanguageConfig.getLanguages().stream()
                .filter(e -> e.getValue().equals(sourceLanguage))
                .findFirst();

        Optional<LanguageOption> resultfindOptional2 = LanguageConfig.getLanguages().stream()
                .filter(e -> e.getValue().equals(targetLanguage))
                .findFirst();
        LanguageOption languageOptionSource = null;
        LanguageOption languageOptionTarget = null;

        if (resultfindOptional1.isPresent()) {
            languageOptionSource = resultfindOptional1.get();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "LANGUAGE NO SUPPORT");

        }

        if (resultfindOptional2.isPresent()) {
            languageOptionTarget = resultfindOptional2.get();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "LANGUAGE NO SUPPORT");
        }

        String typeContent = file.getContentType();
        if (!typeContent.equals(Constain.ContenType.DOC) && !typeContent.equals(Constain.ContenType.DOCX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "FILE NO SUPPORT");
        }
        response.setContentType(typeContent);
        if (typeContent.equals(Constain.ContenType.DOC)) {
            response.setHeader("Content-Disposition", "attachment; filename=document.docx");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=document.doc");
        }
        long startTime = System.nanoTime();
        try (XWPFDocument sourceDoc = new XWPFDocument(file.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            StringBuilder url = new StringBuilder();
            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_EXP)) {
                url.append(
                        URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_EXP
                                + apiKey);
            }
            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_THINK_EXP_01_21)) {
                url.append(
                        URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_THINK_EXP_01_21
                                + apiKey);
            }
            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_02_05)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_02_05
                        + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_PRE_02_05)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_FLASH_PRE_02_05 + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_FLASH)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_FLASH + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_PRO)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_PRO + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_1_5_FLASH_8B)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_1_5_FLASH_8B + apiKey);
            }

            if (typeModel.equals(Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_UNLIMITED)) {
                url.append(URL_GEMINI + Constain.MODEL_GEMINI.GEMINI_2_0_PRO_EXP_UNLIMITED + apiKey);
            }
            boolean isCallBackLimit = false;
            for (XWPFTable table : sourceDoc.getTables()) {
                if (isCallBackLimit) {
                    break;
                }
                for (XWPFTableRow row : table.getRows()) {
                    if (isCallBackLimit) {
                        break;
                    }
                    for (XWPFTableCell cell : row.getTableCells()) {

                        if (isCallBackLimit) {
                            break;
                        }
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            StringBuilder fullText = new StringBuilder();
                            List<XWPFRun> runs = paragraph.getRuns();
                            for (XWPFRun run : runs) {

                                String updatedText = run.getText(0);
                                if (updatedText != null) {
                                    fullText.append(updatedText);
                                }
                            }
                            StringBuilder translatedText = new StringBuilder();
                            if (!fullText.toString().isEmpty() && fullText.toString().matches(".*\\p{L}.*")) {

                                if (!fullText.toString().isEmpty()) {
                                    if (typeModel.equals(Constain.GOOGLE_TRANSLATE)) {
                                        translatedText
                                                .append(GoogleTranslate.translate(sourceLanguage, targetLanguage,
                                                        fullText.toString()));
                                    } else {
                                        HttpClient client = HttpClient.newHttpClient();
                                        HttpRequest request;

                                        Gson gson = new Gson();
                                        List<Part> parts = Collections.singletonList(new Part(requestModel
                                                + " " + languageOptionSource.getText() + " sang "
                                                + languageOptionTarget.getText()
                                                + " không cần diễn giải lại yêu cầu của tôi :" + fullText.toString()));

                                        List<ContentText> contents = Collections
                                                .singletonList(new ContentText(parts, "user"));
                                        GenerationConfig config = new GenerationConfig(temperature, TOP_K, TOP_P,
                                                MAX_OUT_PUT_TOKENS,
                                                RESPONSE_MIME_TYPE);
                                        RequestBodySend requestBody = new RequestBodySend(contents, config);
                                        gson = new GsonBuilder().setPrettyPrinting().create();
                                        String bodyData = gson.toJson(requestBody);

                                        request = HttpRequest.newBuilder()
                                                .header("Content-Type", "application/json")
                                                .uri(new URI(url.toString()))
                                                .POST(HttpRequest.BodyPublishers.ofString(bodyData,
                                                        StandardCharsets.UTF_8))
                                                .build();

                                        HttpResponse<String> responseData;
                                        RequestBodyResponse responseGemini = null;

                                        while (retryCount < maxRetries) {
                                            try {
                                                responseData = client.send(request,
                                                        HttpResponse.BodyHandlers.ofString());
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

                                                    break;
                                                } else {

                                                    try {
                                                        Thread.sleep(waitTime);
                                                    } catch (InterruptedException ex) {
                                                        Thread.currentThread().interrupt();
                                                    }
                                                    retryCount++;
                                                    // waitTime *= 1.3;
                                                    // if (waitTime >= Constain.WAIT_TIME) {
                                                    // waitTime = 1000;
                                                    // }
                                                }
                                                System.out.println("Try  " + retryCount + " after  " + waitTime + "ms");

                                            } catch (IOException e) {
                                                retryCount++;
                                                System.out.println("Try  " + retryCount + " after  " + waitTime + "ms");

                                                try {
                                                    Thread.sleep(waitTime);
                                                } catch (InterruptedException ex) {
                                                    Thread.currentThread().interrupt();
                                                }
                                                retryCount++;
                                                // waitTime *= 1.3;
                                                // if (waitTime >= Constain.WAIT_TIME) {
                                                // waitTime = 1000;
                                                // }
                                            }
                                        }

                                        if (retryCount >= maxRetries) {
                                            isCallBackLimit = true;
                                            System.out.println("ERROR: GEMINI KHONH PHAN HOI");
                                            continue;
                                        }
                                        String dataResponse = responseGemini.getCandidates().get(0).content.getParts()
                                                .get(0)
                                                .getText();
                                        translatedText.append(dataResponse);
                                    }
                                }
                            }

                            if (!fullText.toString().isEmpty()) {
                                if (!runs.isEmpty()) {
                                    if (fullText.toString().matches(".*\\p{L}.*")) {
                                        runs.get(0).setText(translatedText.toString(), 0);
                                    } else {
                                        runs.get(0).setText(fullText.toString(), 0);
                                    }

                                    while (runs.size() > 1) {
                                        paragraph.removeRun(1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StringBuilder largetText = new StringBuilder();
            StringBuilder translatedText = new StringBuilder();
            int indexPar = 0;
            for (XWPFParagraph paragraph : sourceDoc.getParagraphs()) {
                if (isCallBackLimit) {
                    break;
                }
                List<XWPFRun> runs = paragraph.getRuns();
                if (runs != null) {
                    StringBuilder fullText = new StringBuilder();
                    for (XWPFRun run : runs) {
                        String text = run.getText(0);

                        if (text != null && runs.size() > 1) {
                            fullText.append(text);
                            fullText.append(Constain.BREAK_RUN);
                        } else if (text != null) {
                            fullText.append(text);
                        }
                    }
                    if (!fullText.toString().isEmpty()) {
                        largetText.append(fullText.toString());
                        largetText.append(Constain.BREAK_PARAGRAPH);
                    } else {
                        largetText.append(Constain.DATA_EMPTY_REPLACLE);
                        largetText.append(Constain.BREAK_PARAGRAPH);
                    }
                }
                indexPar++;

                if (largetText.toString().length() > maxLength || sourceDoc.getParagraphs().size() == indexPar) {

                    if (!largetText.toString().isEmpty()
                    // && largetText.toString().matches(".*\\p{L}.*")
                    ) {

                        if (typeModel.equals(Constain.GOOGLE_TRANSLATE)) {
                            translatedText
                                    .append(GoogleTranslate.translate(sourceLanguage, targetLanguage,
                                            largetText.toString()));
                        } else {
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request;

                            Gson gson = new Gson();
                            List<Part> parts = Collections.singletonList(new Part(requestModel
                                    + " " + languageOptionSource.getText() + " sang " + languageOptionTarget.getText()
                                    + "và làm ơn không cần diễn giải lại yêu cầu và không thêm hoặc bớt các ký tự, tôi chỉ muốn nhận kết quả: "
                                    + largetText.toString()));

                            // List<ContentText> contents = Collections.singletonList(new ContentText(parts,
                            // "user"));
                            listHistory.add(new ContentText(parts, "user"));
                            GenerationConfig config = new GenerationConfig(temperature, TOP_K, TOP_P,
                                    MAX_OUT_PUT_TOKENS,
                                    RESPONSE_MIME_TYPE);
                            RequestBodySend requestBody = new RequestBodySend(listHistory, config);
                            gson = new GsonBuilder().setPrettyPrinting().create();
                            String bodyData = gson.toJson(requestBody);

                            request = HttpRequest.newBuilder()
                                    .header("Content-Type", "application/json")
                                    .uri(new URI(url.toString()))
                                    .POST(HttpRequest.BodyPublishers.ofString(bodyData, StandardCharsets.UTF_8))
                                    .build();

                            HttpResponse<String> responseData;
                            RequestBodyResponse responseGemini = null;

                            while (retryCount < maxRetries) {
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

                                        break;
                                    } else {
                                        ErrorResponseHead responseHead = gson.fromJson(responseData.body(),
                                                ErrorResponseHead.class);
                                        ErrorResponseData error = responseHead.getError();
                                        System.out
                                                .println(String.format("Ma code loi: %d, message: %s, status = %s",
                                                        error.getCode(), error.getMessage(), error.getStatus()));
                                        try {
                                            Thread.sleep(waitTime);
                                        } catch (InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                        }
                                        retryCount++;
                                        // waitTime *= 1.3;
                                        // if (waitTime >= Constain.WAIT_TIME) {
                                        // waitTime = 1000;
                                        // }

                                    }
                                    System.out.println("Try  " + retryCount + " after  " + waitTime + "ms");

                                } catch (IOException e) {
                                    retryCount++;
                                    System.out.println("Try  " + retryCount + " after  " + waitTime + "ms");

                                    try {
                                        Thread.sleep(waitTime);
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }

                                    retryCount++;
                                    // waitTime *= 1.3;
                                    // if (waitTime >= Constain.WAIT_TIME) {
                                    // waitTime = 1000;
                                    // }
                                }
                            }
                            if (retryCount >= maxRetries) {
                                isCallBackLimit = true;
                                System.out.println("ERROR: GEMINI KHONH PHAN HOI");
                                continue;
                            }
                            retryCount = 0;
                            waitTime = 1000;
                            String dataResponse = responseGemini.getCandidates().get(0).content.getParts().get(0)
                                    .getText();
                            listHistory.add(responseGemini.getCandidates().get(0).content);
                            translatedText.append(dataResponse);
                        }
                        largetText.setLength(0);
                    }
                }
            }

            String[] data = translatedText.toString().split(Constain.BREAK_PARAGRAPH);
            // data = Arrays.stream(data)
            // .filter(str -> !str.equals(Constain.DATA_EMPTY_REPLACLE))
            // .toArray(String[]::new);
            int index = 0;
            for (XWPFParagraph paragraph : sourceDoc.getParagraphs()) {
                List<XWPFRun> runs = paragraph.getRuns();
                if (index >= data.length) {
                    break;
                }
                String[] dataRun = data[index].split(Constain.BREAK_RUN.trim());
                if (runs != null) {
                    int i = 0;
                    for (XWPFRun run : runs) {
                        if (i < dataRun.length) {
                            run.setText(dataRun[i].equals(Constain.DATA_EMPTY_REPLACLE + Constain.DATA_ERROR1) ? " "
                                    : dataRun[i], 0);
                            // run.setText(dataRun[i], 0);
                            i++;
                        }
                    }
                }
                index++;

            }
            response.setContentType(typeContent);

            ObjectMapper objectMapper = new ObjectMapper();
            GenerationConfig config = new GenerationConfig(temperature, TOP_K, TOP_P,
                    MAX_OUT_PUT_TOKENS,
                    RESPONSE_MIME_TYPE);
            objectMapper.writeValue(new File("datacall.json"), new RequestBodySend(listHistory, config));
            if (typeContent.equals(Constain.ContenType.DOC)) {
                response.setHeader("Content-Disposition", "attachment; filename=processed_document.doc");
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=processed_document.docx");
            }
            sourceDoc.write(response.getOutputStream());
            retryCount = 0;
        } catch (

        IOException e) {
            e.printStackTrace();
            retryCount = 0;
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Chuyển thành millisecond
        double minutes = duration / 60000.0; // Chia cho 60,000
        System.out.println("Time end: " + minutes + " minutes");
        waitTime = 1000;
    }

    private List<ContentText> getConversationHistory(ContentText contentText, List<ContentText> listHistory) {
        listHistory.add(contentText);
        return listHistory;
    }
}

// for (XWPFParagraph paragraph : sourceDoc.getParagraphs()) {
// String textParagraph = paragraph.getParagraphText();
// List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
// /// size 14 :82 , 16: 76
// ///
// ///
// final int MAX_LENGTH = 75;
// StringBuilder fullText = new StringBuilder();
// for (XWPFRun run : runs) {
// String text = run.getText(0);

// if (text != null && text.length() > 0) {
// run.setText(GoogleTranslate.translate(targetLanguage, text));
// textParagraph = "";
// }

// //CTR ctr = run.getCTR();

// // if (ctr.getDrawingArray().length > 0) {

// // for (XWPFPicture picture : run.getEmbeddedPictures()) {

// // CTDrawing drawing = (CTDrawing) run.getCTR().getDrawingArray(0);

// // int heightPx1 = 0;
// // int widthPx1 = 0;
// // if (drawing != null) {
// // // Kiểm tra xem là Inline hay Anchor
// // if (drawing.getInlineArray().length > 0) {
// // CTInline inline = drawing.getInlineArray(0);
// // extractImageSize(inline.getExtent().getCx(), inline.getExtent().getCy(),
// // picture);
// // heightPx1 = (int) (inline.getExtent().getCy() / Units.EMU_PER_PIXEL);
// // widthPx1 = (int) (inline.getExtent().getCx() / Units.EMU_PER_PIXEL);
// // } else if (drawing.getAnchorArray().length > 0) {
// // CTAnchor anchor = drawing.getAnchorArray(0);
// // extractImageSize(anchor.getExtent().getCx(), anchor.getExtent().getCy(),
// // picture);
// // heightPx1 = (int) (anchor.getExtent().getCy() / Units.EMU_PER_PIXEL);
// // widthPx1 = (int) (anchor.getExtent().getCx() / Units.EMU_PER_PIXEL);
// // } else {
// // System.out.println("Không tìm thấy thông tin kích thước cho ảnh: "
// // + picture.getPictureData().getFileName());
// // }
// // }

// // XWPFPictureData pictureData = picture.getPictureData();
// // byte[] imageBytes = pictureData.getData();

// // String fileName = "output_" + pictureData.getFileName();

// // int widthImg = 0;
// // int heightImg = 0;
// // try (ByteArrayInputStream byteArrayInputStream = new
// // ByteArrayInputStream(imageBytes)) {
// // BufferedImage img = ImageIO.read(byteArrayInputStream);
// // if (img != null) {
// // widthImg = img.getWidth();
// // heightImg = img.getHeight();
// // }
// // }

// // // Lưu ảnh tạm thời
// // try (FileOutputStream fos = new FileOutputStream(fileName)) {
// // fos.write(imageBytes);
// // }

// // // Chèn ảnh vào tài liệu mới
// // XWPFRun newRunImage = newParagraph.createRun();
// // Path imagePath = Paths.get("").resolve(fileName);
// // if (widthPx1 > 0 && heightPx1 > 0) {
// // try (InputStream imageStream = Files.newInputStream(imagePath)) {
// // newRunImage.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG,
// // imagePath.getFileName().toString(), Units.toEMU(widthPx1),
// // Units.toEMU(heightPx1));
// // } catch (InvalidFormatException e) {
// // e.printStackTrace();
// // } finally {
// // // Xóa file ảnh tạm sau khi xử lý
// // Files.deleteIfExists(imagePath);
// // }
// // } else {
// // try (InputStream imageStream = Files.newInputStream(imagePath)) {
// // newRunImage.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG,
// // imagePath.getFileName().toString(), Units.toEMU(widthImg),
// // Units.toEMU(heightImg));
// // } catch (InvalidFormatException e) {
// // e.printStackTrace();
// // } finally {
// // // Xóa file ảnh tạm sau khi xử lý
// // Files.deleteIfExists(imagePath);
// // }
// // }
// // }
// // }
// }
// }