package com.app.web.controlador;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @Autowired
    private Environment environment;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Object principal, 
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        System.out.println("🏠 === HOME ENDPOINT ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // ✅ VERIFICAR SI VIENE DEL LOGOUT
        if ("true".equals(logout)) {
            System.out.println("✅ User logged out successfully - showing message");
            model.addAttribute("logoutMessage", "Has cerrado sesión correctamente");
        }
        
        // Verificar si está autenticado Y no es usuario anónimo
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            System.out.println("✅ User authenticated: " + auth.getName());
            System.out.println("✅ Redirecting to dashboard");
            return "redirect:/dashboard";
        }
        
        System.out.println("📝 No authenticated user, showing login");
        return "login";
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal Object principal,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "error", required = false) String error,
                        Model model) {
        System.out.println("🔐 === LOGIN ENDPOINT ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // ✅ VERIFICAR SI VIENE DEL LOGOUT
        if ("true".equals(logout)) {
            System.out.println("✅ User logged out successfully - showing message on login page");
            model.addAttribute("logoutMessage", "Has cerrado sesión correctamente");
        }
        
        // ✅ VERIFICAR SI HAY ERROR OAUTH2
        if ("oauth2_error".equals(error)) {
            System.out.println("❌ OAuth2 error detected");
            model.addAttribute("oauth2Error", "Error en la autenticación con Auth0. Por favor, inténtalo nuevamente.");
        }
        
        // *** ARREGLO DEL BUCLE: Verificación más estricta ***
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            System.out.println("✅ User already authenticated: " + auth.getName());
            System.out.println("✅ Authorities: " + auth.getAuthorities());
            return "redirect:/dashboard";
        }
        
        System.out.println("📄 Showing login page");
        return "login";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal Object principal, Model model) {
        System.out.println("👤 === PROFILE ENDPOINT ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            System.out.println("❌ User not authenticated");
            return "redirect:/login";
        }
        
        String userName = "Usuario";
        String userEmail = "";
        boolean isAdmin = false;
        Object userObject = null;
        
        try {
            if (principal instanceof UserDetails) {
                // Login tradicional
                UserDetails userDetails = (UserDetails) principal;
                userName = userDetails.getUsername();
                userEmail = userDetails.getUsername();
                isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                System.out.println("✅ Form Login User: " + userEmail);
                System.out.println("🔑 Is Admin: " + isAdmin);
                
                userObject = new UserProfileInfo(userEmail, userEmail, userDetails.getUsername());
                model.addAttribute("user", userObject);
                model.addAttribute("userRoles", userDetails.getAuthorities());
                
            } else if (principal instanceof OidcUser) {
                // OAuth2 login
                OidcUser oidcUser = (OidcUser) principal;
                String fullName = oidcUser.getFullName();
                userEmail = oidcUser.getEmail();
                
                if (fullName != null && !fullName.isEmpty()) {
                    userName = fullName;
                } else if (userEmail != null && !userEmail.isEmpty()) {
                    userName = userEmail;
                }
                
                isAdmin = oidcUser.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                System.out.println("✅ OAuth2 User: " + userEmail);
                System.out.println("🔑 Is Admin: " + isAdmin);
                
                model.addAttribute("user", oidcUser);
                model.addAttribute("userRoles", oidcUser.getAuthorities());
            } else {
                System.out.println("⚠️ Unknown principal type");
                userName = auth.getName();
                userEmail = auth.getName();
                isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                userObject = new UserProfileInfo(userName, userEmail, userName);
                model.addAttribute("user", userObject);
                model.addAttribute("userRoles", auth.getAuthorities());
            }
            
            model.addAttribute("userName", userName);
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("isAdmin", isAdmin);
            
            System.out.println("📊 Profile loaded for: " + userName);
            
        } catch (Exception e) {
            System.out.println("❌ Error processing profile: " + e.getMessage());
            return "redirect:/dashboard?error=profile_error";
        }
        
        return "profile";
    }

    // 🔍 ENDPOINT DE DEBUG PARA OAUTH2
    @GetMapping("/debug/config")
    @ResponseBody
    public Map<String, String> debugConfig() {
        System.out.println("🔍 === DEBUG CONFIG ENDPOINT ===");
        
        Map<String, String> config = new HashMap<>();
        
        // Variables de entorno OAuth2
        String redirectUri = environment.getProperty("AUTH0_REDIRECT_URI", "NOT_SET");
        String clientId = environment.getProperty("AUTH0_CLIENT_ID", "NOT_SET");
        String domain = environment.getProperty("AUTH0_DOMAIN", "NOT_SET");
        String clientSecret = environment.getProperty("AUTH0_CLIENT_SECRET", "NOT_SET");
        String databaseUrl = environment.getProperty("DATABASE_URL", "NOT_SET");
        String logoutUri = environment.getProperty("AUTH0_LOGOUT_URI", "NOT_SET");
        
        config.put("redirect_uri", redirectUri);
        config.put("client_id", clientId);
        config.put("domain", domain);
        config.put("client_secret", clientSecret.equals("NOT_SET") ? "NOT_SET" : "***CONFIGURED***");
        config.put("logout_uri", logoutUri);
        config.put("database_url_set", databaseUrl.startsWith("postgresql") ? "YES" : "NO");
        
        // Variables desde Spring properties (para comparar)
        config.put("spring_redirect_uri", 
            environment.getProperty("spring.security.oauth2.client.registration.auth0.redirect-uri", "NOT_SET"));
        config.put("spring_client_id", 
            environment.getProperty("spring.security.oauth2.client.registration.auth0.client-id", "NOT_SET"));
        config.put("spring_issuer_uri", 
            environment.getProperty("spring.security.oauth2.client.provider.auth0.issuer-uri", "NOT_SET"));
        
        // Variables del server
        config.put("server_port", environment.getProperty("server.port", "NOT_SET"));
        config.put("server_env", environment.getProperty("SPRING_PROFILES_ACTIVE", "default"));
        
        // Log para debugging
        System.out.println("🔍 Environment Variables Status:");
        System.out.println("- AUTH0_REDIRECT_URI: " + redirectUri);
        System.out.println("- AUTH0_CLIENT_ID: " + clientId);
        System.out.println("- AUTH0_DOMAIN: " + domain);
        System.out.println("- AUTH0_CLIENT_SECRET: " + (clientSecret.equals("NOT_SET") ? "NOT_SET" : "***CONFIGURED***"));
        System.out.println("- AUTH0_LOGOUT_URI: " + logoutUri);
        System.out.println("- DATABASE_URL configured: " + (databaseUrl.startsWith("postgresql") ? "YES" : "NO"));
        
        return config;
    }

    // 🔍 ENDPOINT ADICIONAL PARA VER ERRORES DE OAUTH2
    @GetMapping("/debug/oauth2-error")
    @ResponseBody
    public Map<String, String> debugOAuth2Error() {
        Map<String, String> errorInfo = new HashMap<>();
        
        // Información de URLs esperadas
        errorInfo.put("expected_callback_url", "https://parcial-2-gestion-de-proyecto-1.onrender.com/login/oauth2/code/auth0");
        errorInfo.put("auth0_authorize_url", "https://dev-8p7c66amplkwoh3t.us.auth0.com/authorize");
        errorInfo.put("auth0_token_url", "https://dev-8p7c66amplkwoh3t.us.auth0.com/oauth/token");
        
        // URLs configuradas actualmente
        String currentRedirectUri = environment.getProperty("spring.security.oauth2.client.registration.auth0.redirect-uri", "NOT_SET");
        String currentIssuerUri = environment.getProperty("spring.security.oauth2.client.provider.auth0.issuer-uri", "NOT_SET");
        
        errorInfo.put("current_redirect_uri", currentRedirectUri);
        errorInfo.put("current_issuer_uri", currentIssuerUri);
        
        // Verificación de configuración
        boolean redirectUriCorrect = currentRedirectUri.equals("https://parcial-2-gestion-de-proyecto-1.onrender.com/login/oauth2/code/auth0");
        errorInfo.put("redirect_uri_correct", String.valueOf(redirectUriCorrect));
        
        errorInfo.put("fix_instructions", "Si redirect_uri_correct es false, configura AUTH0_REDIRECT_URI en Render");
        
        return errorInfo;
    }
    
    // Clase auxiliar
    public static class UserProfileInfo {
        private final String fullName;
        private final String email;
        private final String subject;
        
        public UserProfileInfo(String fullName, String email, String subject) {
            this.fullName = fullName;
            this.email = email;
            this.subject = subject;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public java.time.Instant getUpdatedAt() {
            return java.time.Instant.now();
        }
    }
}
