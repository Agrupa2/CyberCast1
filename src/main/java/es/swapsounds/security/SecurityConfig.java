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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .securityMatcher("/api/**")
                .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

        http
                .authorizeHttpRequests(authorize -> authorize
                        // PRIVATE ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/categories/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/sounds/{soundId}/comments").hasRole("USER")
                        .requestMatchers("/api/sounds/{soundId}/comments/{commentId}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{username}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/users/{username}/username").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/users/{username}/avatar").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{username}").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/sounds").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sounds/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sounds/{id}/audio").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/sounds/{id}/image").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/sounds/{id}").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/sounds/{id}/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sounds/{id}/audio").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sounds/").hasRole("USER")
                        .requestMatchers("/api/secret-sounds/**").hasRole("USER")

                        // PUBLIC ENDPOINTS
                        .anyRequest().permitAll());

        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add JWT Token filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Static resources
                        // Public pages
                        .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/sounds", "/sounds/{soundId}")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/sounds/image/**", "/sounds/audio/**").permitAll()
                        .requestMatchers("/signup").permitAll()

                        // Comment EndPoints
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments/{commentId}/edit").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/{soundId}/comments/{commentId}/delete")
                        .hasRole("USER")

                        // Sound EndPoints
                        .requestMatchers("/sounds/upload", "/sounds/download").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/*/edit").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/sounds/*/delete").hasRole("USER")

                        // Private pages
                        .requestMatchers("/sounds/**").hasRole("USER")
                        .requestMatchers("/profile/**", "/delete-account", "/secret-sounds/**").hasRole("USER")
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
                        .accessDeniedPage("/error"));

        return http.build();
    }
}