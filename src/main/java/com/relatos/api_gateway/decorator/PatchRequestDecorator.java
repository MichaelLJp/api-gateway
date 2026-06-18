package com.relatos.api_gateway.decorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relatos.api_gateway.model.GatewayRequest;
import lombok.NonNull;
import org.springframework.http.HttpMethod;

public class PatchRequestDecorator extends PostRequestDecorator {

	public PatchRequestDecorator(GatewayRequest gatewayRequest, ObjectMapper objectMapper) {
		super(gatewayRequest, objectMapper);
	}

	@Override
	@NonNull
	public HttpMethod getMethod() {
		return HttpMethod.PATCH;
	}
}
