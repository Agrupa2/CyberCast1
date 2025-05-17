package es.swapsounds.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import es.swapsounds.security.jwt.JwtRequestFilter;
import es.swapsounds.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    RepositoryUserDetailsService userDetailsService;

    @Autowired
    private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Recursos estáticos
                        // Public pages
                        .requestMatchers("/", "/login", "/error",
                                "/css/**", "/js/**",
                                "/sounds", "/sounds/{soundId}")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/sounds/image/**",
                                "/sounds/audio/**")
                        .permitAll()
                        .requestMatchers("/signup").permitAll()

                        //Comment EndPoints
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments/{commentId}/edit").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments/{commentId}/delete").hasRole("USER")

                        // Sound EndPoints
                        .requestMatchers("/sounds/upload", "/sounds/download").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/*/edit").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/*/delete").hasRole("USER")

                        // Private pages
                        .requestMatchers("/sounds/**").hasRole("USER")
                        .requestMatchers("/profile/**", "/delete-account").hasRole("USER")
                        .requestMatchers("/users/{id}/delete").hasRole("ADMIN")
                        .requestMatchers("/admin/users").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/sounds", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                        .exceptionHandling(ex -> ex
            .accessDeniedPage("/error")
        );


        return http.build();
    }
}