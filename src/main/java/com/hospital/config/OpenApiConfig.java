package com.hospital.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("🏥 Hospital Management System API")
                        .description("نظام إدارة المستشفى الشامل - واجهة برمجة التطبيقات\n\n" +
                                "يوفر هذا النظام إدارة كاملة لـ:\n" +
                                "- **المرضى** - تسجيل وإدارة بيانات المرضى\n" +
                                "- **الأطباء** - إدارة بيانات الأطباء والأقسام\n" +
                                "- **المواعيد** - جدولة وإدارة المواعيد\n" +
                                "- **السجلات الطبية** - تسجيل التشخيصات والوصفات\n" +
                                "- **الأقسام** - إدارة أقسام المستشفى")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hospital Management Team")
                                .email("support@hospital.com"))
                        .license(new License().name("MIT License")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("أدخل JWT Token هنا. مثال: Bearer {token}")));
    }
}
