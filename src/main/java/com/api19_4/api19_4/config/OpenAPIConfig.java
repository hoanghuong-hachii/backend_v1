package com.api19_4.api19_4.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

//@Configuration
//@OpenAPIDefinition(info = @Info(
//        title = "Your API Title",
//        version = "1.0",
//        description = "Your API Description",
//        termsOfService = "URL",
//        contact = @Contact(
//                name = "Huong nhi",
//                email = "toicuthe@gmail.com"
//        )
//))
@RestController
public class OpenAPIConfig {
    @Value("${food.openapi.dev-url}")
    private String devUrl;

    @Value("${food.openapi.prod-url}")
    private String prodUrl;

    @Value("${food.openapi.users-url}")
    private String usersUrl;

    @Value("${food.openapi.images-url}")
    private String imageUrl;

//    @Value("${food.openapi.product-images-url}")
//    private String product_imageUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        Server devServer = new Server();
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server Url in production environment");

        Server usersServer = new Server();
        usersServer.setUrl(usersUrl);
        usersServer.setDescription("Server Url in users environment");

        Server imagesServer = new Server();
        imagesServer.setUrl(imageUrl);
        imagesServer.setDescription("Server Url in images environment");


        io.swagger.v3.oas.models.info.Contact contact = new Contact();
        contact.setEmail("hoanghuong10024012@gmal.com");
        contact.setName("HuongNhi");
        contact.setUrl("https://www.foodfresh.com");

        License mitLicense = new License().name("MIT Licence").url("https://choosealicense.com/licenses/mit/");

        io.swagger.v3.oas.models.info.Info info = new Info()
                .title("FoodFresh Management API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage App FoodFresh")
                .license(mitLicense);

        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(info);
        openAPI.setServers(Arrays.asList( prodServer));
        openAPI .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));

        return openAPI;
    }

}

