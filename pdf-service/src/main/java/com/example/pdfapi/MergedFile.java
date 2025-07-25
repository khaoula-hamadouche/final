package com.example.pdfapi;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.util.*;

@RestController // manquait ici
public class MergedFile {

    @Autowired
    private RestTemplate restTemplate; // manquait aussi ici

    @GetMapping("/generate-merged-files-pdf/{id}")
    public void generateMergedFilesPdf(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            String cookies = request.getHeader("Cookie");

            // 🔗 Appel API pour récupérer les données du dossier
            String url = "http://localhost:8085/api/dossiers/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.COOKIE, cookies);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<ApiResponse> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ApiResponse.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                ApiResponse apiResponse = responseEntity.getBody();

                if (apiResponse != null && apiResponse.getFileDetails() != null && !apiResponse.getFileDetails().isEmpty()) {
                    List<InputStream> pdfStreams = new ArrayList<>();

                    // Télécharger chaque fichier PDF
                    for (String fileName : apiResponse.getFileDetails().keySet()) {
                        String fileUrl = apiResponse.getFileDetails().get(fileName);
                        try {
                            ResponseEntity<byte[]> fileResponse = restTemplate.exchange(
                                    fileUrl,
                                    HttpMethod.GET,
                                    new HttpEntity<>(headers),
                                    byte[].class
                            );

                            if (fileResponse.getStatusCode() == HttpStatus.OK) {
                                byte[] fileBytes = fileResponse.getBody();
                                if (fileBytes != null) {
                                    pdfStreams.add(new ByteArrayInputStream(fileBytes));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Erreur lors de la récupération du fichier : " + fileUrl);
                            e.printStackTrace();
                        }
                    }

                    if (!pdfStreams.isEmpty()) {
                        // 📎 Fusionner les fichiers PDF
                        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
                        PDFMergerUtility merger = new PDFMergerUtility();
                        merger.setDestinationStream(mergedOutput);

                        for (InputStream pdfStream : pdfStreams) {
                            merger.addSource(pdfStream);
                        }

                        merger.mergeDocuments(null); // fusion

                        // 📨 Retourner le PDF fusionné dans la réponse HTTP
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition", "inline; filename=\"merged_files_dossier_" + id + ".pdf\"");
                        OutputStream responseOutput = response.getOutputStream();
                        responseOutput.write(mergedOutput.toByteArray());
                        responseOutput.flush();
                        responseOutput.close();
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucun fichier à fusionner.");
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucun fichier trouvé pour ce dossier.");
                }
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible de récupérer les détails du dossier.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la fusion des fichiers PDF.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
