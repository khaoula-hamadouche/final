package com.example.pdfapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class Pdf2Controller {

    @Autowired
    private PdfToImageService pdfToImageService;

    @PostMapping("/convert")
    public ResponseEntity<?> convertPdfToImages(@RequestParam("file") MultipartFile file) {
        try {
            byte[] pdfBytes = file.getBytes();
            List<String> base64Images = pdfToImageService.convertPdfToBase64Images(pdfBytes);

            Map<String, Object> response = new HashMap<>();
            response.put("images", base64Images);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la conversion : " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
