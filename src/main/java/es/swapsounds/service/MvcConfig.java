package es.swapsounds.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration for static resources
        registry
                .addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                    HttpServletResponse response,
                    Object handler) throws Exception {
                // Añadir en el interceptor
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Content-Security-Policy",
                        // Base
                        "default-src 'self'; " +

                // Scripts
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                // Scripts externos (archivos .js)
                                "script-src-elem 'self' 'unsafe-inline' " + // <- Añade 'unsafe-inline' aquí
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                // Styles
                                "style-src 'self' 'unsafe-inline' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; " +

                // Hojas de estilo externas
                                "style-src-elem 'self' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; " +

                // Imágenes
                                "img-src 'self' data:; " + // Permite data: URIs para imágenes en línea

                // Fuentes
                                "font-src 'self' data: https://fonts.gstatic.com; " + // <- Añade esto

                // Otros
                                "object-src 'none';");

                return true;
            }
        });
    }
}