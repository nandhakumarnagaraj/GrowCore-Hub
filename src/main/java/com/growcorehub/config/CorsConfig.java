package com.growcorehub.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Allow specific origins (update for production)
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", // Angular development server
				"http://localhost:3000", // Alternative frontend port
				"https://yourdomain.com" // Production domain
		));

		// Or use patterns for more flexibility
		// configuration.setAllowedOriginPatterns(Arrays.asList("*"));

		// Allow specific HTTP methods
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// Allow specific headers
		configuration
				.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "cache-control"));

		// Expose headers to frontend
		configuration.setExposedHeaders(Arrays.asList("x-auth-token"));

		// Allow credentials (cookies, authorization headers)
		configuration.setAllowCredentials(true);

		// Cache preflight response
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}