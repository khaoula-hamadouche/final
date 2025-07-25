package com.example.pdfapi;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Controller
public class PdfGenerationController {

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
        private String typePassation;
        private LocalDateTime dateSoumission;
        private Long chargeDossierId;
        private Map<String, String> fileDetails;

        // Getters and setters for DossierWrapper
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

    public static class DossierDto {
        private String numeroDossier;
        private String intitule;
        private LocalDate dateSoumission;
        private String typePassation; // Added TypePassation in DTO
        private List<String> fileNames;

        // Getters and setters for DossierDto
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

        public LocalDate getDateSoumission() {
            return dateSoumission;
        }

        public void setDateSoumission(LocalDate dateSoumission) {
            this.dateSoumission = dateSoumission;
        }

        public String getTypePassation() {
            return typePassation;
        }

        public void setTypePassation(String typePassation) {
            this.typePassation = typePassation;
        }

        public List<String> getFileNames() {
            return fileNames;
        }

        public void setFileNames(List<String> fileNames) {
            this.fileNames = fileNames;
        }
    }

    @GetMapping("/generate-dossier-pdf")
    @ResponseBody
    public void generateDossierPdf(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 📌 Récupérer le JWT à partir des cookies
            String cookies = request.getHeader("Cookie");

            // 🔗 Appel API pour récupérer les dossiers
            String url = "http://localhost:8085/api/dossiers/";
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.COOKIE, cookies);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<ApiResponse[]> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ApiResponse[].class
            );

            ApiResponse[] apiResponses = responseEntity.getBody();
            List<DossierDto> dossiers = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

            if (apiResponses != null) {
                for (ApiResponse apiResponse : apiResponses) {
                    DossierWrapper dossierWrapper = apiResponse.getDossier();
                    if (dossierWrapper != null) {
                        DossierDto dossierDto = new DossierDto();
                        dossierDto.setNumeroDossier(dossierWrapper.getNumeroDossier());
                        dossierDto.setIntitule(dossierWrapper.getIntitule());
                        dossierDto.setTypePassation(dossierWrapper.getTypePassation()); // Set TypePassation
                        if (dossierWrapper.getDateSoumission() != null) {
                            try {
                                dossierDto.setDateSoumission(LocalDate.from(dateFormatter.parse(dossierWrapper.getDateSoumission().toString())));
                            } catch (Exception e) {
                                System.err.println("Error parsing date: " + dossierWrapper.getDateSoumission());
                                e.printStackTrace();
                                dossierDto.setDateSoumission(null); // Handle parsing error
                            }
                        }

                        List<String> fileNames = new ArrayList<>();
                        if (apiResponse.getFileDetails() != null) {
                            fileNames.addAll(apiResponse.getFileDetails().keySet());
                        }
                        dossierDto.setFileNames(fileNames);
                        dossiers.add(dossierDto);

                        System.out.println("→ Numéro : " + dossierDto.getNumeroDossier());
                        System.out.println("→ Intitulé : " + dossierDto.getIntitule());
                        System.out.println("→ Mode de passation : " + dossierDto.getTypePassation()); // Corrected log
                        System.out.println("→ Date : " + dossierDto.getDateSoumission());
                        System.out.println("→ Fichiers : " + dossierDto.getFileNames());
                    }
                }
                System.out.println("✅ Dossiers récupérés : " + dossiers.size());

                // 📄 Charger le template HTML (we only need the name now)
                String templateName = "dossier_template";

                Map<String, Object> data = new HashMap<>();
                data.put("title", "Rapport des Dossiers CME");
                data.put("dossiers", dossiers);

                // 🏗 Générer le HTML final avec Thymeleaf
                String filledHtml = TemplateEngineUtil.process(templateName, data);

                // 🖨 Générer le PDF avec Flying Saucer
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(filledHtml);
                renderer.layout();

                // 📝 Configurer la réponse HTTP
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=rapport_dossiers.pdf");

                OutputStream outputStream = response.getOutputStream();
                renderer.createPDF(outputStream);
                outputStream.close();

            } else {
                System.out.println("❌ Aucun dossier récupéré de l'API.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Consider using a proper logger
        }
    }
}