package es.swapsounds.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para uploads y avatares
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(
                "file:uploads/",
                "classpath:/static/default-avatars/");

        // Configuración para recursos estáticos estándar (CSS/JS/Images)
        registry
            .addResourceHandler("/**")
            .addResourceLocations(
                "classpath:/static/css/",
                "classpath:/static/js/", 
                "classpath:/static/images/",
                "classpath:/static/");
    }
}