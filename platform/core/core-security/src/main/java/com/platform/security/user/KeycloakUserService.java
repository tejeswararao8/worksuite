package com.platform.security.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages users in Keycloak via Admin REST API.
 * Used when creating/updating/deleting users from the platform.
 * Keycloak is the single source of truth for user credentials.
 */
@Slf4j
@Service
public class KeycloakUserService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:vve-platform}")
    private String realm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────────────────────────────────────
    // CREATE USER in Keycloak
    // Called when new employee is onboarded
    // ─────────────────────────────────────────────
    public String createUser(String email, String firstName,
                              String lastName, String password,
                              String companyId, List<String> roles) {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> userPayload = Map.of(
            "username",   email,
            "email",      email,
            "firstName",  firstName,
            "lastName",   lastName,
            "enabled",    true,
            "attributes", Map.of("companyId", List.of(companyId)),
            "credentials", List.of(Map.of(
                "type",      "password",
                "value",     password,
                "temporary", false
            ))
        );

        HttpHeaders headers = bearerHeaders(token);
        ResponseEntity<Void> response = restTemplate.exchange(
            url, HttpMethod.POST,
            new HttpEntity<>(userPayload, headers),
            Void.class
        );

        // Extract Keycloak user ID from Location header
        String location = response.getHeaders().getFirst("Location");
        String keycloakUserId = location != null
            ? location.substring(location.lastIndexOf("/") + 1)
            : null;

        // Assign roles if provided
        if (keycloakUserId != null && roles != null && !roles.isEmpty()) {
            assignRoles(keycloakUserId, roles, token);
        }

        log.info("Created Keycloak user: {} id: {}", email, keycloakUserId);
        return keycloakUserId;
    }

    // ─────────────────────────────────────────────
    // UPDATE USER in Keycloak
    // ─────────────────────────────────────────────
    public void updateUser(String keycloakUserId, String firstName, String lastName) {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        Map<String, Object> payload = Map.of(
            "firstName", firstName,
            "lastName",  lastName
        );

        restTemplate.exchange(url, HttpMethod.PUT,
            new HttpEntity<>(payload, bearerHeaders(token)), Void.class);

        log.info("Updated Keycloak user: {}", keycloakUserId);
    }

    // ─────────────────────────────────────────────
    // RESET PASSWORD in Keycloak
    // ─────────────────────────────────────────────
    public void resetPassword(String keycloakUserId, String newPassword) {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm
            + "/users/" + keycloakUserId + "/reset-password";

        Map<String, Object> payload = Map.of(
            "type",      "password",
            "value",     newPassword,
            "temporary", false
        );

        restTemplate.exchange(url, HttpMethod.PUT,
            new HttpEntity<>(payload, bearerHeaders(token)), Void.class);

        log.info("Reset password for Keycloak user: {}", keycloakUserId);
    }

    // ─────────────────────────────────────────────
    // DISABLE USER in Keycloak (on offboarding)
    // ─────────────────────────────────────────────
    public void disableUser(String keycloakUserId) {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        restTemplate.exchange(url, HttpMethod.PUT,
            new HttpEntity<>(Map.of("enabled", false), bearerHeaders(token)), Void.class);

        log.info("Disabled Keycloak user: {}", keycloakUserId);
    }

    // ─────────────────────────────────────────────
    // ASSIGN ROLES to user in Keycloak
    // ─────────────────────────────────────────────
    public void assignRoles(String keycloakUserId, List<String> roleNames, String token) {
        String rolesUrl = keycloakUrl + "/admin/realms/" + realm + "/roles";
        String userRolesUrl = keycloakUrl + "/admin/realms/" + realm
            + "/users/" + keycloakUserId + "/role-mappings/realm";

        // Fetch role representations
        List<Map<String, Object>> roleReps = roleNames.stream()
            .map(roleName -> {
                ResponseEntity<Map> resp = restTemplate.exchange(
                    rolesUrl + "/" + roleName, HttpMethod.GET,
                    new HttpEntity<>(bearerHeaders(token)), Map.class);
                return resp.getBody();
            })
            .toList();

        restTemplate.exchange(userRolesUrl, HttpMethod.POST,
            new HttpEntity<>(roleReps, bearerHeaders(token)), Void.class);

        log.info("Assigned roles {} to user {}", roleNames, keycloakUserId);
    }

    // ─────────────────────────────────────────────
    // Get admin token for Keycloak Admin API calls
    // ─────────────────────────────────────────────
    private String getAdminToken() {
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id",  adminClientId);
        form.add("username",   adminUsername);
        form.add("password",   adminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.POST,
            new HttpEntity<>(form, headers), Map.class);

        return (String) response.getBody().get("access_token");
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
