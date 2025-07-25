package com.example.pdfapi;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    // Fonction pour extraire tous les emails qui contiennent le mot-clé
    public List<String> extractEmailsWithKeyword(File pdfFile, String keyword) throws IOException {
        // Charger le document PDF
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();

        // Extraire tout le texte du PDF
        String text = pdfStripper.getText(document);
        document.close();

        // Expression régulière pour trouver les emails
        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zAZ0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(text);

        // Liste pour stocker les emails qui contiennent le mot-clé
        List<String> filteredEmails = new ArrayList<>();

        // Parcourir tous les emails extraits
        while (matcher.find()) {
            String email = matcher.group();
            if (email.contains(keyword)) {  // Si l'email contient le mot-clé
                filteredEmails.add(email);
            }
        }

        return filteredEmails;
    }
}
