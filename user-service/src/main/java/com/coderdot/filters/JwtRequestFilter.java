package com.coderdot.filters;

import com.coderdot.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UserDetailsService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRequestFilter(UserDetailsService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractJwt(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("✅ Authentification réussie pour l'utilisateur : {}", username);
                } else {
                    log.warn("❌ JWT invalide.");
                }
            } catch (Exception e) {
                log.warn("❌ Erreur lors de la validation du JWT : {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ Aucun JWT valide trouvé dans la requête.");
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        // 🔹 1. Récupérer depuis le header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("🔍 Token récupéré via Authorization header");
            return authHeader.substring(7);
        }

        // 🔹 2. Récupérer depuis le cookie si pas dans Authorization
        if (request.getCookies() != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst();

            if (jwtCookie.isPresent()) {
                log.info("🔍 Token récupéré via cookie");
                return jwtCookie.get().getValue();
            }
        }

        log.warn("⚠️ Aucun token trouvé");
        return null;
    }

    private void deleteJwtCookie(HttpServletResponse response) {
        Cookie invalidCookie = new Cookie("jwt", "");
        invalidCookie.setHttpOnly(true);
        invalidCookie.setSecure(true);
        invalidCookie.setPath("/");
        invalidCookie.setMaxAge(0);
        response.addCookie(invalidCookie);
        log.info("JWT supprimé du cookie");
    }
}