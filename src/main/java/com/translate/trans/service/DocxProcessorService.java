package com.translate.trans.service;

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
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;

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

import javax.imageio.ImageIO;

import com.darkprograms.speech.translator.GoogleTranslate;

@Service
public class DocxProcessorService {

    // Đọc nội dung từ file DOCX
    public String readDocx(MultipartFile file) {
        StringBuilder content = new StringBuilder();

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            // Đọc các đoạn văn bản
            for (XWPFParagraph para : document.getParagraphs()) {
                content.append(para.getText()).append("\n");
            }

            // Đọc nội dung trong bảng (nếu có)
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        content.append(cell.getText()).append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    // Ghi nội dung vào file DOCX mới
    public ByteArrayInputStream writeDocx(String content) {
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText(content);

            document.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ByteArrayInputStream copyDocxWithImages(MultipartFile file) {
        try (XWPFDocument sourceDoc = new XWPFDocument(file.getInputStream());
                XWPFDocument targetDoc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            StringBuilder stringBuilder = new StringBuilder();
            // Copy các đoạn văn
            for (XWPFParagraph para : sourceDoc.getParagraphs()) {
                XWPFParagraph newPara = targetDoc.createParagraph();

                String ab = GoogleTranslate.translate("en", "vi", para.getText());
                stringBuilder.append(GoogleTranslate.translate("vi", para.getText()) + "\n");
            }
            XWPFParagraph newPara = targetDoc.createParagraph();
            newPara.createRun().setText(stringBuilder.toString());

            // Copy tất cả hình ảnh
            List<XWPFPictureData> pictures = sourceDoc.getAllPictures();
            for (XWPFPictureData pic : pictures) {
                try {

                    byte[] imageBytes = pic.getData();

                    String fileName = "output_" + pic.getFileName();

                    int widthImg = 0;
                    int heightImg = 0;
                    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes)) {
                        BufferedImage img = ImageIO.read(byteArrayInputStream);
                        if (img != null) {
                            widthImg = img.getWidth();
                            heightImg = img.getHeight();
                        }
                    }

                    try (FileOutputStream fos = new FileOutputStream(fileName)) {
                        fos.write(imageBytes);
                    }

                    File fileImg = new File(fileName);
                    boolean check = convertToSupportedFormat(fileImg.getAbsolutePath(),
                            "image1_converted.jpg");
                    if (check) {
                        fileImg = new File("image1_converted.jpg");
                    }
                    targetDoc.addPictureData(pic.getData(), Document.PICTURE_TYPE_JPEG);
                } catch (InvalidFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // Ghi file mới vào ByteArrayOutputStream
            targetDoc.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void convertEmfToPng(String emfFilePath, String outputFilePath) throws IOException {
        String command = "convert " + emfFilePath + " " + outputFilePath;
        Process process = Runtime.getRuntime().exec(command);

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String extractText(MultipartFile file) {
        StringBuilder content = new StringBuilder();

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            // Đọc tất cả các đoạn văn bản
            for (XWPFParagraph para : document.getParagraphs()) {
                content.append(para.getText()).append("\n");
            }

            // Đọc nội dung trong bảng (nếu có)
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        content.append(cell.getText()).append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    // Ghi văn bản vào file DOCX mới
    public ByteArrayInputStream writeDocxWithText(String text, MultipartFile file) {
        try (XWPFDocument sourceDoc = new XWPFDocument(file.getInputStream());
                XWPFDocument targetDoc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Copy các phần khác (hình ảnh, bảng biểu) từ tài liệu gốc
            copyPictures(sourceDoc, targetDoc);

            // Thêm văn bản đã xử lý vào file mới
            XWPFParagraph newPara = targetDoc.createParagraph();
            newPara.createRun().setText(text);

            // Ghi file mới vào ByteArrayOutputStream
            targetDoc.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Copy hình ảnh từ tài liệu gốc sang tài liệu mới
    private void copyPictures(XWPFDocument sourceDoc, XWPFDocument targetDoc) {
        List<XWPFPictureData> pictures = sourceDoc.getAllPictures();
        for (XWPFPictureData pic : pictures) {
            try {
                targetDoc.addPictureData(pic.getData(), pic.getPictureType());
            } catch (InvalidFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // Chuyển EMF sang PNG
    public File convertEmfToPng(InputStream emfInputStream) throws IOException {
        EMFInputStream emfStream = new EMFInputStream(emfInputStream, EMFInputStream.DEFAULT_VERSION);
        BufferedImage image = new BufferedImage(
                emfStream.readHeader().getBounds().width,
                emfStream.readHeader().getBounds().height,
                BufferedImage.TYPE_INT_ARGB);

        File pngFile = File.createTempFile("converted", ".png");
        ImageIO.write(image, "png", pngFile);
        return pngFile;
    }

    // Tạo DOCX và chèn PNG
    public ByteArrayOutputStream insertPngToDocx(File pngFile) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
                FileInputStream imageInputStream = new FileInputStream(pngFile);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addPicture(imageInputStream,
                    XWPFDocument.PICTURE_TYPE_PNG,
                    pngFile.getName(),
                    Units.toEMU(300),
                    Units.toEMU(200));

            document.write(out);
            return out;
        }
    }

    @SuppressWarnings("deprecation")
    public ByteArrayInputStream readDocxWithImg(MultipartFile file) {
        try (XWPFDocument sourceDoc = new XWPFDocument(file.getInputStream());
                XWPFDocument targetDoc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph newParagraph = targetDoc.createParagraph();

            for (XWPFParagraph paragraph : sourceDoc.getParagraphs()) {
                // Sao chép các XWPFRun sang một danh sách mới để tránh
                // ConcurrentModificationException
                List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());

                for (XWPFRun run : runs) { // Lặp qua danh sách đã sao chép
                    String text = run.getText(0);
                    if (text != null) {
                        XWPFRun newRun = newParagraph.createRun();
                        newRun.setText(text);
                        newRun.setFontSize(14);
                        newRun.setBold(run.isBold());
                        newRun.setItalic(run.isItalic());
                        newRun.setUnderline(run.getUnderline());
                    }

                    CTR ctr = run.getCTR();
                    if (ctr.getDrawingArray().length > 0) {
                        for (XWPFPicture picture : run.getEmbeddedPictures()) {
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

                            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                fos.write(imageBytes);
                            }

                            XWPFRun newRunImage = paragraph.insertNewRun(paragraph.getRuns().indexOf(run) + 1);
                            File fileImg = new File(fileName);
                            boolean check = convertToSupportedFormat(fileImg.getAbsolutePath(),
                                    "image1_converted.jpg");
                            if (check) {
                                fileImg = new File("image1_converted.jpg");
                            }
                            String extension = fileImg.getName().substring(fileImg.getName().lastIndexOf('.') + 1);
                            Path imagePath = Paths.get("").resolve(fileName);
                            try {
                                newRunImage.addPicture(Files.newInputStream(imagePath), XWPFDocument.PICTURE_TYPE_PNG,
                                        imagePath.getFileName().toString(), Units.toEMU(100), Units.toEMU(100));
                            } catch (InvalidFormatException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            // try (FileInputStream imageStream = new FileInputStream(fileImg)) {

                            // try {
                            // newRunImage.addPicture(
                            // imageStream,
                            // Document.PICTURE_TYPE_JPEG,
                            // fileImg.getAbsolutePath(),
                            // Units.toEMU(widthImg),
                            // Units.toEMU(heightImg));
                            // } catch (InvalidFormatException e) {
                            // e.printStackTrace();
                            // }
                            // }
                        }
                    }

                }
            }

            targetDoc.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean convertToSupportedFormat(String inputFilePath, String outputFilePath) {
        try {
            // Đọc ảnh từ file gốc
            File inputFile = new File(inputFilePath);
            BufferedImage image = ImageIO.read(inputFile);

            if (image == null) {
                System.out.println("Không thể đọc ảnh từ: " + inputFilePath);
                return false;
            }

            // Ghi ảnh vào file đầu ra với định dạng .jpg
            File outputFile = new File(outputFilePath);
            boolean isWritten = ImageIO.write(image, "jpg", outputFile);

            if (!isWritten) {
                System.out.println("Không thể ghi ảnh vào file: " + outputFilePath);
                return false;
            }

            System.out.println("Chuyển đổi ảnh thành công: " + outputFilePath);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
