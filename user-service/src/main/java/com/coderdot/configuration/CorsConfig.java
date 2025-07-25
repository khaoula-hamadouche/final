package com.coderdot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Liste des domaines autorisés (pas "*")
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200","http://localhost:8085", "http://10.16.100.36"));

        // Autorise l'envoi des credentials (cookies, Authorization headers)
        config.setAllowCredentials(true);

        // Autoriser uniquement les méthodes utilisées
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Autoriser les headers nécessaires
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
