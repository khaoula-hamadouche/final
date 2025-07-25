package com.example.pdfapi;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Controller
public class AllPdf {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public AllPdf() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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
        private Map<String, Object> details;

        public DossierWrapper getDossier() { return dossier; }
        public void setDossier(DossierWrapper dossier) { this.dossier = dossier; }
        public Map<String, String> getFileDetails() { return fileDetails; }
        public void setFileDetails(Map<String, String> fileDetails) { this.fileDetails = fileDetails; }
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
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

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNumeroDossier() { return numeroDossier; }
        public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }
        public String getIntitule() { return intitule; }
        public void setIntitule(String intitule) { this.intitule = intitule; }
        public String getEtat() { return etat; }
        public void setEtat(String etat) { this.etat = etat; }
        public String getTypePassation() { return typePassation; }
        public void setTypePassation(String typePassation) { this.typePassation = typePassation; }
        public LocalDateTime getDateSoumission() { return dateSoumission; }
        public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
        public Long getChargeDossierId() { return chargeDossierId; }
        public void setChargeDossierId(Long chargeDossierId) { this.chargeDossierId = chargeDossierId; }
        public Map<String, String> getFileDetails() { return fileDetails; }
        public void setFileDetails(Map<String, String> fileDetails) { this.fileDetails = fileDetails; }
    }

    @GetMapping("/generate-allpdf/{id}")
    public void generateDossierPdf(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            String cookies = request.getHeader("Cookie");
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.COOKIE, cookies);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // --- 1. Fetch Main Dossier Details ---
            String dossierUrl = "http://localhost:8085/api/dossiers/" + id;
            ResponseEntity<ApiResponse> dossierResponseEntity = restTemplate.exchange(
                    dossierUrl, HttpMethod.GET, entity, ApiResponse.class
            );

            if (dossierResponseEntity.getStatusCode() != HttpStatus.OK) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Dossier non trouvé ou erreur API: " + dossierResponseEntity.getStatusCode());
                return;
            }

            ApiResponse apiResponse = dossierResponseEntity.getBody();
            DossierWrapper dossierWrapper = apiResponse.getDossier();

            if (dossierWrapper == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Dossier non trouvé ou vide dans la réponse API.");
                return;
            }

            DossierDto dossierDto = new DossierDto();
            dossierDto.setNumeroDossier(dossierWrapper.getNumeroDossier());
            dossierDto.setIntitule(dossierWrapper.getIntitule());
            dossierDto.setTypePassation(dossierWrapper.getTypePassation());

            if (dossierWrapper.getDateSoumission() != null) {
                try {
                    dossierDto.setDateSoumission(dossierWrapper.getDateSoumission().toLocalDate());
                } catch (Exception e) {
                    System.err.println("Error parsing date 'dateSoumission': " + dossierWrapper.getDateSoumission() + " to LocalDate. Setting to null.");
                    e.printStackTrace();
                    dossierDto.setDateSoumission(null);
                }
            }

            List<String> fileNames = new ArrayList<>();
            if (dossierWrapper.getFileDetails() != null) {
                fileNames.addAll(dossierWrapper.getFileDetails().keySet());
            } else if (apiResponse.getFileDetails() != null) {
                fileNames.addAll(apiResponse.getFileDetails().keySet());
            }
            dossierDto.setFileNames(fileNames);

            if (apiResponse.getDetails() != null) {
                try {
                    String typePassation = dossierDto.getTypePassation();
                    System.out.println("DEBUG: Processing details for typePassation: " + typePassation);

                    switch (typePassation) {
                        case "APPEL_OFFRE_LANCEMENT":
                        case "Consultation_Prestataire_de_Lancement":
                        case "Consultation_Procurement_de_Lancement":
                            LancementDetails lancementDetails = objectMapper.convertValue(apiResponse.getDetails(), LancementDetails.class);
                            dossierDto.setLancementDetails(lancementDetails);
                            System.out.println("DEBUG: Mapped LancementDetails.");
                            break;
                        case "APPEL_OFFRE_ATTRIBUTION":
                        case "Consultation_Prestataire_dAttribution":
                        case "Consultation_Procurement_dAttribution":
                            AttributionDetails attributionDetails = objectMapper.convertValue(apiResponse.getDetails(), AttributionDetails.class);
                            dossierDto.setAttributionDetails(attributionDetails);
                            System.out.println("DEBUG: Mapped AttributionDetails.");
                            break;
                        case "AVENANT":
                            AvenantDetails avenantDetails = objectMapper.convertValue(apiResponse.getDetails(), AvenantDetails.class);
                            dossierDto.setAvenantDetails(avenantDetails);
                            System.out.println("DEBUG: Mapped AvenantDetails. Numero Contrat: " + (avenantDetails != null ? avenantDetails.getNumeroContrat() : "null"));
                            break;
                        case "GRE_A_GRE":
                            GreAGreDetails greAGreDetails = objectMapper.convertValue(apiResponse.getDetails(), GreAGreDetails.class);
                            dossierDto.setGreAGreDetails(greAGreDetails);
                            System.out.println("DEBUG: Mapped GreAGreDetails.");
                            break;
                        default:
                            System.out.println("DEBUG: Unknown or unhandled typePassation for details mapping: " + typePassation);
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR: Failed to convert 'details' object. Check DTO field types/names against JSON structure: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: 'details' object from API response is NULL or empty.");
            }

            // --- 2. Fetch Decisions Details and then User Details for each decision ---
            String decisionsUrl = "http://localhost:8085/api/decisions/dossiers/" + id;
            List<DecisionDto> decisions = new ArrayList<>();
            try {
                // IMPORTANT: Use ParameterizedTypeReference for List<DecisionDto>
                ResponseEntity<List<DecisionDto>> decisionsResponseEntity = restTemplate.exchange(
                        decisionsUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DecisionDto>>() {}
                );

                if (decisionsResponseEntity.getStatusCode() == HttpStatus.OK && decisionsResponseEntity.getBody() != null) {
                    List<DecisionDto> fetchedDecisions = decisionsResponseEntity.getBody();

                    for (DecisionDto decision : fetchedDecisions) {
                        // Use the chargeDossierId that was directly mapped from the decisions API
                        Long chargeDossierId = decision.getChargeDossierId(); // <-- THIS IS THE FIX

                        if (chargeDossierId != null) {
                            String userUrl = "http://localhost:8081/api/users/" + chargeDossierId;
                            try {
                                ResponseEntity<UserDto> userResponseEntity = restTemplate.exchange(
                                        userUrl, HttpMethod.GET, entity, UserDto.class
                                );
                                if (userResponseEntity.getStatusCode() == HttpStatus.OK && userResponseEntity.getBody() != null) {
                                    decision.setChargeDossier(userResponseEntity.getBody()); // Set the full UserDto
                                    System.out.println("DEBUG: Fetched user " + userResponseEntity.getBody().getName() + " for decision ID: " + decision.getId());
                                } else {
                                    System.out.println("DEBUG: Could not fetch user for ID: " + chargeDossierId + ". Status: " + userResponseEntity.getStatusCode());
                                    decision.setChargeDossier(null); // Set to null if user data not found
                                }
                            } catch (HttpClientErrorException.NotFound userEx) {
                                System.out.println("DEBUG: User API returned 404 Not Found for ID: " + chargeDossierId + ". Assuming user not found.");
                                decision.setChargeDossier(null);
                            } catch (Exception userEx) {
                                System.err.println("ERROR: Failed to fetch user details for ID: " + chargeDossierId + ". Error: " + userEx.getMessage());
                                userEx.printStackTrace();
                                decision.setChargeDossier(null);
                            }
                        } else {
                            System.out.println("DEBUG: chargeDossierId is null for decision ID: " + decision.getId());
                            decision.setChargeDossier(null); // Explicitly set UserDto to null if ID is missing
                        }
                        decisions.add(decision); // Add the decision (now with or without user details)
                    }
                    System.out.println("DEBUG: Processed " + decisions.size() + " decisions for dossier ID: " + id);
                } else {
                    System.out.println("DEBUG: No decisions found or empty response for dossier ID: " + id + ". Status: " + decisionsResponseEntity.getStatusCode());
                }
            } catch (HttpClientErrorException.NotFound e) {
                System.out.println("DEBUG: Decisions API returned 404 Not Found for dossier ID: " + id + ". Assuming no decisions.");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to fetch or map decisions for dossier ID: " + id + ". Error: " + e.getMessage());
                e.printStackTrace();
            }
            dossierDto.setDecisions(decisions);


            // --- Comprehensive DEBUG LOGGING of DossierDto State before Thymeleaf ---
            System.out.println("\n--- DossierDto State before Thymeleaf ---");
            System.out.println("DEBUG: dossierDto.getTypePassation() = " + dossierDto.getTypePassation());
            System.out.println("DEBUG: dossierDto.getDateSoumission() = " + dossierDto.getDateSoumission());
            System.out.println("DEBUG: File Names in DossierDto: " + (dossierDto.getFileNames() != null ? dossierDto.getFileNames().size() : 0) + " files.");

            if (dossierDto.getLancementDetails() != null) {
                System.out.println("DEBUG: LancementDetails is NOT null. Montant Estimé: " + dossierDto.getLancementDetails().getMontantEstime());
            } else { System.out.println("DEBUG: LancementDetails IS NULL."); }
            if (dossierDto.getAttributionDetails() != null) {
                System.out.println("DEBUG: AttributionDetails is NOT null. Nom Fournisseur: " + dossierDto.getAttributionDetails().getNomFournisseur());
            } else { System.out.println("DEBUG: AttributionDetails IS NULL."); }
            if (dossierDto.getAvenantDetails() != null) {
                System.out.println("DEBUG: AvenantDetails is NOT null. Numero Contrat: " + dossierDto.getAvenantDetails().getNumeroContrat());
            } else { System.out.println("DEBUG: AvenantDetails IS NULL."); }
            if (dossierDto.getGreAGreDetails() != null) {
                System.out.println("DEBUG: GreAGreDetails is NOT null. Montant Estimé: " + dossierDto.getGreAGreDetails().getMontantEstime());
            } else { System.out.println("DEBUG: GreAGreDetails IS NULL."); }
            if (dossierDto.getDecisions() != null) {
                System.out.println("DEBUG: Decisions are NOT null. Number of decisions: " + dossierDto.getDecisions().size());
                for (DecisionDto d : dossierDto.getDecisions()) {
                    if (d.getChargeDossier() != null) {
                        System.out.println("    DEBUG: Decision ID " + d.getId() + " by User: " + d.getChargeDossier().getName() + " (" + d.getChargeDossier().getEmail() + ")");
                    } else {
                        System.out.println("    DEBUG: Decision ID " + d.getId() + " - User details NOT found (ID: " + d.getChargeDossierId() + ").");
                    }
                }
            } else { System.out.println("DEBUG: Decisions IS NULL."); }
            System.out.println("--- End DossierDto State ---");


            Map<String, Object> data = new HashMap<>();
            data.put("dossier", dossierDto);
            data.put("title", "Dossier " + dossierDto.getNumeroDossier());
            data.put("currentDate", Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

            String filledHtml = TemplateEngineUtil.process("pv_template", data);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(filledHtml);
            renderer.layout();

            response.setContentType("application/pdf");
            String filename = "dossier_" + dossierDto.getNumeroDossier() + ".pdf";
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

            OutputStream outputStream = response.getOutputStream();
            renderer.createPDF(outputStream);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la génération du PDF: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}