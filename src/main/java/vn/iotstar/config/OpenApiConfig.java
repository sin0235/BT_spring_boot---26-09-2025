package vn.iotstar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Category & Product Management API")
                        .description("RESTful API cho quản lý Category và Product với Spring Boot")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Trần Phúc Toàn")
                                .email("tranphuctoan@student.hcmute.edu.vn")
                                .url("https://github.com/sin0235"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.example.com").description("Production Server")
                ));
    }
}
