package com.relatos.api_gateway.filter;

import com.relatos.api_gateway.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro de autenticacion basado en el patron phantom token.
 * Se ejecuta antes que RequestTranslationFilter (orden 0 < 10001).
 *
 * IMPORTANTE: switchIfEmpty se coloca ANTES de flatMap para evitar que se
 * dispare cuando chain.filter() completa con Mono<Void> vacio.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ACCESS_TOKEN_HEADER = "accessToken";

	private final TokenValidationService tokenValidationService;

	@Value("${app.gateway.public-paths}")
	private List<String> publicPaths;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if (isPublicPath(path)) {
			log.debug("Ruta publica, sin validacion de token: {}", path);
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			log.info("Acceso denegado a {} - falta header Authorization", path);
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		String opaqueToken = authHeader.substring(BEARER_PREFIX.length()).trim();

		return tokenValidationService.validate(opaqueToken)
				.switchIfEmpty(Mono.error(new TokenInvalidException("Token invalido o expirado")))
				.flatMap(tokenResponse -> {
					if (!tokenResponse.isValid() || tokenResponse.getAccessToken() == null) {
						log.info("users-service rechazo el token para ruta {}", path);
						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
						return exchange.getResponse().setComplete();
					}

					log.debug("Token valido para usuario {} en ruta {}", tokenResponse.getEmail(), path);

					ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
							.header(ACCESS_TOKEN_HEADER, tokenResponse.getAccessToken())
							.header("X-User-Id", String.valueOf(tokenResponse.getUserId()))
							.header("X-User-Email", tokenResponse.getEmail())
							.header("X-User-Role", tokenResponse.getRole())
							.build();

					return chain.filter(exchange.mutate().request(mutatedRequest).build());
				})
				.onErrorResume(TokenInvalidException.class, e -> {
					log.info("Token invalido o expirado para ruta {}: {}", path, e.getMessage());
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				});
	}

	@Override
	public int getOrder() {
		return 0;
	}

	private boolean isPublicPath(String path) {
		return publicPaths.stream().anyMatch(path::startsWith);
	}

	private static class TokenInvalidException extends RuntimeException {
		TokenInvalidException(String message) {
			super(message);
		}
	}
}
