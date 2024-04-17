package account.configuration;

import account.security.CustomAccessDeniedHandler;
import account.security.CustomAuthenticationEntryPoint;
import account.security.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

  @Autowired
  private CustomAccessDeniedHandler customAccessDeniedHandler;

  @Autowired
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Autowired
  private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .httpBasic()
        .authenticationEntryPoint(customAuthenticationEntryPoint)
        .and()

        .csrf(csrf -> csrf.disable()) // For Postman
        .headers(headers -> headers.frameOptions().disable()) // For the H2 console
        //.auth
        .authorizeHttpRequests(auth -> auth // manage access
                //.requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/signup",
                    "/actuator/shutdown").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/h2-console/**").permitAll()
                .requestMatchers( "/api/admin/**").hasRole("ADMINISTRATOR")
                .requestMatchers( "/api/acct/**").hasRole("ACCOUNTANT")
                .requestMatchers( "/api/security/**").hasRole("AUDITOR")
                .anyRequest().authenticated()
            // other matchers
        ).sessionManagement(sessions -> sessions
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling()
       .accessDeniedHandler(customAccessDeniedHandler);


    return http.build();
  }

//  @Bean
//  public UserDetailsService userDetailsService() {
//    UserDetails user1 = User.withUsername("user1")
//        .password(this.passwordEncoder().encode("pass1"))
//        .roles("ADMINISTRATOR")
//        .build();
//    UserDetails user2 = User
//        .withDefaultPasswordEncoder()
//        .username("user2")
//        .password("pass2")
//        .roles("ADMINISTRATOR")
//        .build();
//
//    return new InMemoryUserDetailsManager(user1, user2);
//  }
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
