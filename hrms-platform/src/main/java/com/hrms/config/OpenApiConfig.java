package com.hrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080" + contextPath).description("Local Development"),
                        new Server().url("https://api.hrms.com" + contextPath).description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT access token obtained from POST /auth/login. Format: Bearer {token}")));
    }

    private Info apiInfo() {
        return new Info()
                .title("HRMS Platform API")
                .description("""
                        ## Enterprise Multi-Tenant HRMS Platform
                        
                        A production-grade Human Resource Management System supporting multiple companies (tenants).
                        
                        ### Authentication
                        All endpoints (except `/auth/login` and `/auth/refresh`) require a valid **JWT Bearer token**.
                        
                        1. Call `POST /auth/login` with your email and password.
                        2. Copy the `accessToken` from the response.
                        3. Click **Authorize** above and enter: `Bearer <your_token>`
                        
                        ### Multi-Tenancy
                        Every request is scoped to the authenticated user's company. Data from other companies is never accessible.
                        
                        ### Roles & Permissions
                        | Role | Description |
                        |------|-------------|
                        | `SUPER_ADMIN` | Platform-level admin, manages companies |
                        | `COMPANY_ADMIN` | Full access within a company |
                        | `HR` | HR operations — employees, documents, assets |
                        | `MANAGER` | View team members, approve requests |
                        | `EMPLOYEE` | Self-service — own profile, documents |
                        
                        ### Pagination
                        All list endpoints support `page` (0-based) and `size` query parameters.
                        Default page size is **20**. Maximum is **100**.
                        
                        ### Response Format
                        All responses follow a standard envelope:
                        ```json
                        {
                          "success": true,
                          "message": "Success",
                          "data": { ... },
                          "timestamp": "2024-01-01T10:00:00"
                        }
                        ```
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("HRMS Platform Team")
                        .email("support@hrms.com")
                        .url("https://hrms.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://hrms.com/license"));
    }
}
