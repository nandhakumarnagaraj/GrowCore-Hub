package com.growcorehub.config;

import com.growcorehub.service.UserService;
import com.growcorehub.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader("Authorization");

		String username = null;
		String jwtToken = null;

		// JWT Token is in the form "Bearer token". Remove Bearer word and get only the
		// Token
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				username = jwtUtil.getEmailFromJwtToken(jwtToken);
			} catch (Exception e) {
				log.error("Unable to get JWT Token or JWT Token has expired", e);
			}
		} else if (StringUtils.hasText(requestTokenHeader)) {
			log.warn("JWT Token does not begin with Bearer String");
		}

		// Once we get the token validate it and set authentication
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				UserDetails userDetails = this.userService.loadUserByUsername(username);

				// If token is valid configure Spring Security to manually set authentication
				if (jwtUtil.validateJwtToken(jwtToken)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// After setting the Authentication in the context, we specify
					// that the current user is authenticated. So it passes the Spring Security
					// Configurations successfully.
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			} catch (Exception e) {
				log.error("Cannot set user authentication", e);
			}
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/api/auth/") || path.equals("/api/health") || path.startsWith("/swagger-")
				|| path.startsWith("/v3/api-docs");
	}
}