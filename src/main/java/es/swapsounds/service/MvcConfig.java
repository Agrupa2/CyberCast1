package es.swapsounds.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration for users uploads and default avatars
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:uploads/",
                        "classpath:/static/default-avatars/");

        // Configuration for static resources
        registry
                .addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/images/",
                        "classpath:/static/");
    }
}