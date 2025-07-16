package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Imports for OpenAPI
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "basicAuth";

        return new OpenAPI()
                // Add security components for Basic Auth, as specified in the project plan
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Basic Authentication with username and password.")))
                // Apply the security globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // Define server information
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))

                // Define API metadata based on the project plan
                .info(new Info()
                        .title("Logistics & Tracking API")
                        .version("1.0")
                        .description("""
                                This API documentation, hosted by the **SecurityCheckpointService**, provides a comprehensive overview of the entire Logistics microservice ecosystem.
                                
                                ### Business Context:
                                A secured logistics company that transports goods between locations with tracking and verification.
                                
                                ### Microservices Overview:
                                *   **PackageService**: Manages the lifecycle (CRUD) of packages being transported.
                                *   **LocationService**: Stores and provides information about fixed transition points and warehouses.
                                *   **SecurityCheckpointService**: Controls secure transitions and stores checkpoint logs. **(This Service)**
                                *   **TrackingService**: Offers a CQRS-based view of a package's journey using Axon Framework.
                                
                                The endpoints documented here belong to the **SecurityCheckpointService**, which is secured using Basic Authentication.
                                """)
                        .contact(new Contact()
                                .name("Logistics Technical Support")
                                .email("support@belvi.com")
                                .url("https://belvi.com/contact"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                        .termsOfService("https://belvi.com/terms"));
    }
}