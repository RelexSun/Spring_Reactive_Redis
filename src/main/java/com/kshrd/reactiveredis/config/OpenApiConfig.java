package com.kshrd.reactiveredis.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Movie Service API",
        version = "1.0.0",
        description = "Reactive Movie API using Spring WebFlux, Redis, and R2DBC",
        contact = @Contact(name = "Relexsun Nop", url = "https://github.com/RelexSun", email = "relexsunnop@gmail.com")
        ))
//        security = @SecurityRequirement(name = "bearerAuth"))
//@SecurityScheme(name = "bearerAuth", scheme = "bearer", type = SecuritySchemeType.HTTP, description = "JWT Bearer authentication", bearerFormat = "JWT")
public class OpenApiConfig {

}

//@Configuration
//public class OpenApiConfig {
//
//    @Bean
//    public OpenAPI movieServiceOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Movie Service API")
//                        .description("Reactive Movie API using Spring WebFlux, Redis, and R2DBC")
//                        .version("v1.0.0")
//                        .contact(new Contact()
//                                .name("Relexsun Nop")
//                                .url("https://github.com/RelexSun")
//                                .email("relexsunnop@gmail.com"))
//                );
//    }
//
//    @Bean
//    public GroupedOpenApi movieApi() {
//        return GroupedOpenApi.builder()
//                .group("movie-service")
//                .pathsToMatch("/api/movies/**")
//                .build();
//    }
//
////    @Bean
////    public OpenAPI secureOpenAPI() {
////        return new OpenAPI()
////                .components(new Components()
////                        .addSecuritySchemes("bearerAuth",
////                                new SecurityScheme()
////                                        .type(SecurityScheme.Type.HTTP)
////                                        .scheme("bearer")
////                                        .bearerFormat("JWT")))
////                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
////    }
//}