package com.example.pdfapi;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Controller
public class PdfDossier {

    private final RestTemplate restTemplate = new RestTemplate();

    // Utility class for Thymeleaf template processing
    public static class TemplateEngineUtil {
        private static final org.thymeleaf.TemplateEngine templateEngine;

        static {
            templateEngine = new org.thymeleaf.TemplateEngine();
            org.thymeleaf.templateresolver.ClassLoaderTemplateResolver resolver = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
            resolver.setPrefix("templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            templateEngine.setTemplateResolver(resolver);
        }

        public static String process(String templateName, Map<String, Object> variables) {
            Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        }
    }

    public static class ApiResponse {
        private DossierWrapper dossier;
        private Map<String, String> fileDetails;

        public DossierWrapper getDossier() {
            return dossier;
        }

        public void setDossier(DossierWrapper dossier) {
            this.dossier = dossier;
        }

        public Map<String, String> getFileDetails() {
            return fileDetails;
        }

        public void setFileDetails(Map<String, String> fileDetails) {
            this.fileDetails = fileDetails;
        }
    }

    public static class DossierWrapper {
        private Long id;
        private String numeroDossier;
        private String intitule;
        private String etat;
        private String typePassation; // This should match the API response field name
        private LocalDateTime dateSoumission;
        private Long chargeDossierId;
        private Map<String, String> fileDetails;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNumeroDossier() {
            return numeroDossier;
        }

        public void setNumeroDossier(String numeroDossier) {
            this.numeroDossier = numeroDossier;
        }

        public String getIntitule() {
            return intitule;
        }

        public void setIntitule(String intitule) {
            this.intitule = intitule;
        }

        public String getEtat() {
            return etat;
        }

        public void setEtat(String etat) {
            this.etat = etat;
        }

        public String getTypePassation() {
            return typePassation;
        }

        public void setTypePassation(String typePassation) {
            this.typePassation = typePassation;
        }

        public LocalDateTime getDateSoumission() {
            return dateSoumission;
        }

        public void setDateSoumission(LocalDateTime dateSoumission) {
            this.dateSoumission = dateSoumission;
        }

        public Long getChargeDossierId() {
            return chargeDossierId;
        }

        public void setChargeDossierId(Long chargeDossierId) {
            this.chargeDossierId = chargeDossierId;
        }

        public Map<String, String> getFileDetails() {
            return fileDetails;
        }

        public void setFileDetails(Map<String, String> fileDetails) {
            this.fileDetails = fileDetails;
        }
    }

    @GetMapping("/generate-dossier-pdf/{id}")
    public void generateDossierPdf(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 📌 Récupérer le JWT à partir des cookies
            String cookies = request.getHeader("Cookie");

            // 🔗 Appel API pour récupérer le dossier par ID
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
                DossierWrapper dossierWrapper = apiResponse.getDossier();

                if (dossierWrapper != null) {
                    DossierDto dossierDto = new DossierDto();
                    dossierDto.setNumeroDossier(dossierWrapper.getNumeroDossier());
                    dossierDto.setIntitule(dossierWrapper.getIntitule());
                    dossierDto.setTypePassation(dossierWrapper.getTypePassation()); // Populate TypePassation
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    if (dossierWrapper.getDateSoumission() != null) {
                        try {
                            dossierDto.setDateSoumission(LocalDate.from(dateFormatter.parse(dossierWrapper.getDateSoumission().toString())));
                        } catch (Exception e) {
                            System.err.println("Error parsing date: " + dossierWrapper.getDateSoumission());
                            e.printStackTrace();
                            dossierDto.setDateSoumission(null);
                        }
                    }

                    List<String> fileNames = new ArrayList<>();
                    if (apiResponse.getFileDetails() != null) {
                        fileNames.addAll(apiResponse.getFileDetails().keySet());
                    }
                    dossierDto.setFileNames(fileNames);

                    System.out.println("File Names in DossierDto: " + dossierDto.getFileNames()); // LOGGING
                    System.out.println("Number of File Names: " + dossierDto.getFileNames().size()); // LOGGING
                    System.out.println("Type de Passation in DossierDto: " + dossierDto.getTypePassation()); // LOGGING

                    Map<String, Object> data = new HashMap<>();
                    data.put("dossier", dossierDto);
                    data.put("title", "Dossier " + dossierDto.getNumeroDossier());
                    data.put("currentDate", Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    data.put("modePassation", dossierDto.getTypePassation()); // Pass TypePassation to template

                    // 🏗 Générer le HTML final avec Thymeleaf
                    String filledHtml = TemplateEngineUtil.process("dossier_single_template", data);

                    // 🖨 Générer le PDF avec Flying Saucer
                    ITextRenderer renderer = new ITextRenderer();
                    renderer.setDocumentFromString(filledHtml);
                    renderer.layout();

                    // Préparer la réponse pour le PDF
                    response.setContentType("application/pdf");
                    String filename = "dossier_" + dossierDto.getNumeroDossier() + ".pdf";
                    response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\""); // Or "attachment; ..."

                    OutputStream outputStream = response.getOutputStream();
                    renderer.createPDF(outputStream);
                    outputStream.close();

                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Dossier non trouvé.");
                }
            } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Dossier non trouvé.");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la récupération du dossier.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la génération du PDF.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}