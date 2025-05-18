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

                // Headers to prevent XSS and clickjacking attacks
                response.setHeader("Content-Security-Policy",
                        // default resources only from this origin
                        "default-src 'self'; " +

                // scripts that are allowed:
                // - from this origin
                // - inline and eval (required for Quill)
                // - from cdn.quilljs.com and cdn.jsdelivr.net
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                // for <script src="..."> specifically
                                "script-src-elem 'self' https://cdn.quilljs.com https://cdn.jsdelivr.net; " +

                // permitted resources:
                // - this origin
                // - inline (Quill uses inline resources)
                // - Quill CDN, jsDelivr and Google Fonts
                                "style-src 'self' 'unsafe-inline' " +
                                "https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; " +

                // for <link href="..."> specifically
                                "style-src-elem 'self' https://cdn.quilljs.com https://cdn.jsdelivr.net https://fonts.googleapis.com; "
                                +

                // images from:
                                "img-src 'self' data:; " +

                // Neither frame nor iframe
                                "object-src 'none';");

                return true;
            }
        });
    }
}