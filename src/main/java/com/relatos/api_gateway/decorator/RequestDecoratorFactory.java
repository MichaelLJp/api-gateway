package com.relatos.api_gateway.decorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relatos.api_gateway.model.GatewayRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestDecoratorFactory {

	private final ObjectMapper objectMapper;

	public ServerHttpRequestDecorator getDecorator(GatewayRequest request) {
		if (request.getTargetMethod() == null) {
			throw new IllegalArgumentException("targetMethod is required");
		}

		return switch (request.getTargetMethod().name()) {
			case "GET" -> new GetRequestDecorator(request);
			case "POST" -> new PostRequestDecorator(request, objectMapper);
			case "PUT" -> new PutRequestDecorator(request, objectMapper);
			case "PATCH" -> new PatchRequestDecorator(request, objectMapper);
			case "DELETE" -> new DeleteRequestDecorator(request);
			default -> throw new IllegalArgumentException("Unsupported targetMethod: " + request.getTargetMethod());
		};
	}
}
