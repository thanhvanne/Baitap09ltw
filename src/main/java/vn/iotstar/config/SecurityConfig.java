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

                // =====================================
                // PUBLIC
                // =====================================

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


                // =====================================
                // ADMIN ONLY
                // =====================================

                .requestMatchers(
                    "/users/**",
                    "/dashboard"
                )
                .hasRole("ADMIN")


                // =====================================
                // USER + ADMIN
                //
                // Ownership của Product được kiểm tra
                // thêm ở ProductService.
                // =====================================

                .requestMatchers(
                    "/products/**"
                )
                .authenticated()


                // =====================================
                // CÒN LẠI
                // =====================================

                .anyRequest()
                .authenticated()
            )


            // =========================================
            // LOGIN
            // =========================================

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                /*
                 * Giá trị của field "username"
                 * có thể là:
                 *
                 * admin
                 * hoặc admin@gmail.com
                 */
                .usernameParameter("username")

                .passwordParameter("password")

                .defaultSuccessUrl("/", true)

                .failureUrl("/login?error=true")

                .permitAll()
            )


            // =========================================
            // LOGOUT
            // =========================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl(
                    "/login?logout=true"
                )

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies("JSESSIONID")

                .permitAll()
            )


            // =========================================
            // ACCESS DENIED
            // =========================================

            .exceptionHandling(exception ->
                exception.accessDeniedPage(
                    "/access-denied"
                )
            );


        return http.build();
    }
}