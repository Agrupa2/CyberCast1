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

                // Cabecera CSP ampliada
                response.setHeader("Content-Security-Policy",
                    // recursos por defecto solo de este origen
                    "default-src 'self'; " +

                    // scripts que se pueden ejecutar:
                    // - desde este origen
                    // - inline y eval (imprescindible para Quill)
                    // - desde cdn.quilljs.com y cdn.jsdelivr.net
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                    // para <script src="..."> específicamente
                    "script-src-elem 'self' https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                    // estilos permitidos:
                    // - de este origen
                    // - inline (Quill usa estilos inline)
                    // - de Quill CDN, jsDelivr y Google Fonts
                    "style-src 'self' 'unsafe-inline' " +
                               "https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; " +

                    // para <link href="..."> específicamente
                    "style-src-elem 'self' https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; " +

                    // imágenes: este origen y data URIs (avatares, imágenes embebidas)
                    "img-src 'self' data:; " +

                    // ningún plugin de objeto
                    "object-src 'none';"
                );

                return true;
            }
        });
    }
}