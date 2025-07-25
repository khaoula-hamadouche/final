package com.example.pdfapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class Pdf1Controller {

    @Autowired
    private PdfService pdfService;

    // Endpoint pour extraire les emails contenant le mot-clé
    @PostMapping("/extract-emails")
    public List<String> extractEmails(@RequestParam("file") MultipartFile file,
                                      @RequestParam("keyword") String keyword) {
        try {
            // Sauvegarder le fichier PDF temporairement
            File tempFile = File.createTempFile("uploaded-", ".pdf");
            file.transferTo(tempFile);

            // Appeler le service pour extraire les emails avec le mot-clé
            return pdfService.extractEmailsWithKeyword(tempFile, keyword);
        } catch (IOException e) {
            e.printStackTrace();
            return null;  // Retourner une erreur si l'extraction échoue
        }
    }
}
