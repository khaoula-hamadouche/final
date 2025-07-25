package com.example.pdfapi;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PdfToImageService {

    public List<String> convertPdfToBase64Images(byte[] pdfBytes) throws IOException {
        List<String> base64Images = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 200); // bonne qualité
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                base64Images.add("data:image/png;base64," + base64);
            }
        }

        return base64Images;
    }
}
