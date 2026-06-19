package com.relatos.api_gateway.service;

import com.relatos.api_gateway.dto.ValidateTokenRequest;
import com.relatos.api_gateway.dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenValidationService {

	private static final String VALIDATE_URI = "http://users-service/api/v1/auth/token/validate";

	private final WebClient loadBalancedWebClient;

	public Mono<ValidateTokenResponse> validate(String opaqueToken) {
		log.debug("Validando token opaco contra users-service: {}", opaqueToken);

		return loadBalancedWebClient.post()
				.uri(VALIDATE_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new ValidateTokenRequest(opaqueToken))
				.retrieve()
				.bodyToMono(ValidateTokenResponse.class)
				.doOnNext(r -> log.debug("Respuesta de validacion: valid={}, email={}", r.isValid(), r.getEmail()))
				.onErrorResume(WebClientResponseException.class, ex -> {
					log.warn("Token rechazado por users-service: {} {}", ex.getStatusCode(), ex.getMessage());
					return Mono.empty();
				})
				.onErrorResume(Exception.class, ex -> {
					log.error("Error contactando users-service para validar token: {}", ex.getMessage());
					return Mono.empty();
				});
	}
}
