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
public class Pdf1Service {

    // Fonction pour extraire les montants associés au mot-clé
    public List<String> extractAmountsByKeyword(File pdfFile, String keyword) throws IOException {
        // Charger le document PDF
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();

        // Extraire tout le texte du PDF
        String text = pdfStripper.getText(document);
        document.close();

        // Expression régulière pour trouver les montants (ex. 1000, 1,000, 1.000, 1000€, $1000)
        String amountRegex = "(\\d{1,3}(?:[.,']\\d{3})*(?:[.,]\\d+)?(?:\\s?\\$|€|€|\\s?)?)";
        Pattern amountPattern = Pattern.compile(amountRegex);
        Matcher amountMatcher = amountPattern.matcher(text);

        // Liste pour stocker les montants associés au mot-clé
        List<String> filteredAmounts = new ArrayList<>();

        // Rechercher le mot-clé dans le texte
        int keywordIndex = text.indexOf(keyword);

        // Tant que le mot-clé est trouvé dans le texte
        while (keywordIndex != -1) {
            // Trouver la position de ce mot-clé dans le texte
            String substring = text.substring(keywordIndex - 50, Math.min(keywordIndex + 50 + keyword.length(), text.length()));
            // Vérifier si un montant est présent autour de ce mot-clé
            while (amountMatcher.find()) {
                if (substring.contains(amountMatcher.group())) {
                    filteredAmounts.add(amountMatcher.group());
                }
            }

            // Rechercher le prochain occurrence du mot-clé
            keywordIndex = text.indexOf(keyword, keywordIndex + 1);
        }

        return filteredAmounts;
    }
}
