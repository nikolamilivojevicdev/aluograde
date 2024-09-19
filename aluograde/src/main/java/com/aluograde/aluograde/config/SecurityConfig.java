package com.aluograde.aluograde.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Ispravna sintaksa za onemogućavanje CSRF u Spring Security 6+
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/ograde/create","/ograde/index/**", "/ograde/edit/**", "/ograde/delete/**").hasRole("ADMIN")
                .requestMatchers("/ograde/login", "/static/**").permitAll() // Dozvoljeno svima
                .anyRequest().authenticated() // Svi ostali zahtevi zahtevaju autentifikaciju
            )
            .formLogin((form) -> form
                .loginPage("/ograde/login") // Definisanje custom login stranice
                .defaultSuccessUrl("/ograde", true) // Uspešan login preusmerava na index
                .permitAll()
            )
            .logout((logout) -> logout
                .permitAll() // Dozvoli svima logout
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Definiši korisnike u memoriji
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("123"))
            .roles("ADMIN")
            .build();

        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder.encode("123"))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}