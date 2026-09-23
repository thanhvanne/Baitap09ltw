package vn.iotstar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/verify-otp",
                    "/register/resend-otp",
                    "/forgot-password",
                    "/reset-password",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/error",
                    "/access-denied"
                )
                .permitAll()

                .requestMatchers(
                    "/users/**",
                    "/dashboard"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    "/products/**"
                )
                .authenticated()

                .anyRequest()
                .authenticated()
            )

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                /*
                 * Field tên username nhưng value
                 * có thể là username hoặc email.
                 */
                .usernameParameter("username")

                .passwordParameter("password")

                .defaultSuccessUrl("/", true)

                .failureUrl(
                    "/login?error=true"
                )

                .permitAll()
            )

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl(
                    "/login?logout=true"
                )

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies(
                    "JSESSIONID"
                )

                .permitAll()
            )

            /*
             * Stateful session authentication.
             * Mỗi account chỉ giữ một session mới nhất.
             */
            .sessionManagement(session ->
                session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            )

            .exceptionHandling(exception ->
                exception.accessDeniedPage(
                    "/access-denied"
                )
            );

        return http.build();
    }
}