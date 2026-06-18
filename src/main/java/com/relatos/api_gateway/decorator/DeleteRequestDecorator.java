package com.relatos.api_gateway.decorator;

import com.relatos.api_gateway.model.GatewayRequest;
import lombok.NonNull;
import org.springframework.http.HttpMethod;

public class DeleteRequestDecorator extends GetRequestDecorator {

	public DeleteRequestDecorator(GatewayRequest gatewayRequest) {
		super(gatewayRequest);
	}

	@Override
	@NonNull
	public HttpMethod getMethod() {
		return HttpMethod.DELETE;
	}
}
