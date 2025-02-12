package com.translate.trans.controller;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.darkprograms.speech.translator.GoogleTranslate;
import com.translate.trans.service.DocxProcessorService;
import com.translate.trans.service.ExcelExportService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
// import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private DocxProcessorService docxProcessorService;

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        // Dữ liệu mẫu
        List<String[]> data = Arrays.asList(
                new String[] { "1", "Nguyen Van A", "a@example.com" },
                new String[] { "2", "Tran Thi B", "b@example.com" },
                new String[] { "3", "Le Van C", "c@example.com" });

        // Gọi service để xuất file
        excelExportService.exportToExcel(response, data);
    }

    @PostMapping("/upload")
    public void exportWord(HttpServletResponse response, @RequestParam("file") MultipartFile file)
            throws IOException, InvalidFormatException {
        // Đặt header để trình duyệt biết đây là file Word
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=document.docx");

        try (XWPFDocument sourceDoc = new XWPFDocument(file.getInputStream());
                XWPFDocument targetDoc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            for (XWPFParagraph paragraph : sourceDoc.getParagraphs()) {
                String textParagraph = paragraph.getParagraphText();
                List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
                /// size 14 :82 , 16: 76
                ///
                ///
                final int MAX_LENGTH = 75;
                StringBuilder fullText = new StringBuilder();
                for (XWPFRun run : runs) {
                    XWPFParagraph newParagraph = targetDoc.createParagraph();
                    XWPFRun newRun = newParagraph.createRun();
                    String text = run.getText(0);
                    if (textParagraph != null && textParagraph.length() > 0) {

                        newParagraph.setAlignment(paragraph.getAlignment());
                        newParagraph.setIndentationFirstLine(paragraph.getIndentationFirstLine());
                        String targetText = GoogleTranslate.translate("vi", textParagraph);
                        if (targetText.length() < MAX_LENGTH) {
                            newRun.setText(targetText);
                            newRun.setText("\n");
                        } else {
                            newRun.setText(targetText);
                        }
                        newRun.setFontSize(paragraph.getFontAlignment());
                        newRun.setFontFamily("Times New Roman");
                        newRun.setBold(run.isBold());
                        newRun.setItalic(run.isItalic());
                        newRun.setColor(run.getColor());
                        newRun.setUnderline(run.getUnderline());
                        textParagraph = "";
                    }

                    // if (text != null) {
                    // fullText.append(text);
                    // newParagraph.setAlignment(paragraph.getAlignment());
                    // newParagraph.setIndentationFirstLine(paragraph.getIndentationFirstLine());
                    // String targetText = GoogleTranslate.translate("vi", fullText.toString());
                    // if (targetText.length() < MAX_LENGTH) {
                    // newRun.setText(targetText);
                    // newRun.setText("\n");
                    // } else {
                    // newRun.setText(targetText);
                    // }
                    // newRun.setFontSize(paragraph.getFontAlignment());
                    // newRun.setFontFamily("Times New Roman");
                    // newRun.setBold(run.isBold());
                    // newRun.setItalic(run.isItalic());
                    // newRun.setColor(run.getColor());
                    // newRun.setUnderline(run.getUnderline());
                    // }
                    // if (fullText.toString().length() > 0) {
                    // String targetText = GoogleTranslate.translate("vi", fullText.toString());
                    // if (targetText.length() < MAX_LENGTH) {
                    // newRun.setText(targetText);
                    // newRun.setText("\n");
                    // } else {
                    // newRun.setText(targetText);
                    // }
                    // }
                    // newParagraph.addRun(newRun);

                    CTR ctr = run.getCTR();

                    if (ctr.getDrawingArray().length > 0) {

                        for (XWPFPicture picture : run.getEmbeddedPictures()) {

                            CTDrawing drawing = (CTDrawing) run.getCTR().getDrawingArray(0);

                            int heightPx1 = 0;
                            int widthPx1 = 0;
                            if (drawing != null) {
                                // Kiểm tra xem là Inline hay Anchor
                                if (drawing.getInlineArray().length > 0) {
                                    CTInline inline = drawing.getInlineArray(0);
                                    extractImageSize(inline.getExtent().getCx(), inline.getExtent().getCy(), picture);
                                    heightPx1 = (int) (inline.getExtent().getCy() / Units.EMU_PER_PIXEL);
                                    widthPx1 = (int) (inline.getExtent().getCx() / Units.EMU_PER_PIXEL);
                                } else if (drawing.getAnchorArray().length > 0) {
                                    CTAnchor anchor = drawing.getAnchorArray(0);
                                    extractImageSize(anchor.getExtent().getCx(), anchor.getExtent().getCy(), picture);
                                    heightPx1 = (int) (anchor.getExtent().getCy() / Units.EMU_PER_PIXEL);
                                    widthPx1 = (int) (anchor.getExtent().getCx() / Units.EMU_PER_PIXEL);
                                } else {
                                    System.out.println("Không tìm thấy thông tin kích thước cho ảnh: "
                                            + picture.getPictureData().getFileName());
                                }
                            }

                            XWPFPictureData pictureData = picture.getPictureData();
                            byte[] imageBytes = pictureData.getData();

                            String fileName = "output_" + pictureData.getFileName();

                            int widthImg = 0;
                            int heightImg = 0;
                            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes)) {
                                BufferedImage img = ImageIO.read(byteArrayInputStream);
                                if (img != null) {
                                    widthImg = img.getWidth();
                                    heightImg = img.getHeight();
                                }
                            }

                            // Lưu ảnh tạm thời
                            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                fos.write(imageBytes);
                            }

                            // Chèn ảnh vào tài liệu mới
                            XWPFRun newRunImage = newParagraph.createRun();
                            Path imagePath = Paths.get("").resolve(fileName);
                            if (widthPx1 > 0 && heightPx1 > 0) {
                                try (InputStream imageStream = Files.newInputStream(imagePath)) {
                                    newRunImage.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG,
                                            imagePath.getFileName().toString(), Units.toEMU(widthPx1),
                                            Units.toEMU(heightPx1));
                                } catch (InvalidFormatException e) {
                                    e.printStackTrace();
                                } finally {
                                    // Xóa file ảnh tạm sau khi xử lý
                                    Files.deleteIfExists(imagePath);
                                }
                            } else {
                                try (InputStream imageStream = Files.newInputStream(imagePath)) {
                                    newRunImage.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG,
                                            imagePath.getFileName().toString(), Units.toEMU(widthImg),
                                            Units.toEMU(heightImg));
                                } catch (InvalidFormatException e) {
                                    e.printStackTrace();
                                } finally {
                                    // Xóa file ảnh tạm sau khi xử lý
                                    Files.deleteIfExists(imagePath);
                                }
                            }
                        }
                    }
                }
            }

            // Ghi file Word đích vào response để tải xuống
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=processed_document.docx");
            targetDoc.write(response.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/")
    public String getMethodName() {
        return "index1";
    }

    private static void extractImageSize(long widthEMU, long heightEMU, XWPFPicture picture) {
        int widthPx = (int) (widthEMU / Units.EMU_PER_PIXEL);
        int heightPx = (int) (heightEMU / Units.EMU_PER_PIXEL);

        System.out.println("Ảnh: " + picture.getPictureData().getFileName());
        System.out.println("Kích thước: " + widthPx + " x " + heightPx + " px");
    }

}
