package com.example.pdfapi;

import com.example.pdfapi.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class PdfController {

    @Autowired
    private RestTemplate restTemplate;


    @GetMapping("/generate-pdf")
    @ResponseBody
    public void generatePdf(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 📌 Récupérer les cookies de l'utilisateur
            String cookies = request.getHeader("Cookie");

            // 🔗 Appel API sécurisé avec les cookies JWT
            String url = "http://localhost:8081/api/users";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookies);  // On transmet les cookies JWT
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<UserDto[]> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, UserDto[].class
            );

            UserDto[] utilisateurs = responseEntity.getBody();

            // 📄 Charger le template HTML
            ClassPathResource resource = new ClassPathResource("templates/template.html");
            String htmlContent = Files.readString(Paths.get(resource.getURI()));

            Map<String, Object> data = new HashMap<>();
            data.put("title", "Rapport des utilisateurs");
            data.put("utilisateurs", Arrays.asList(utilisateurs));

            String filledHtml = TemplateEngineUtil.process(htmlContent, data);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(filledHtml);
            renderer.layout();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=rapport.pdf");

            OutputStream outputStream = response.getOutputStream();
            renderer.createPDF(outputStream);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

