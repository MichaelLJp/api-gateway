package com.relatos.api_gateway.decorator;

import com.relatos.api_gateway.model.GatewayRequest;
import lombok.NonNull;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

public class GetRequestDecorator extends ServerHttpRequestDecorator {

	private final GatewayRequest gatewayRequest;

	public GetRequestDecorator(GatewayRequest gatewayRequest) {
		super(gatewayRequest.getExchange().getRequest());
		this.gatewayRequest = gatewayRequest;
	}

	@Override
	@NonNull
	public HttpMethod getMethod() {
		return HttpMethod.GET;
	}

	@Override
	@NonNull
	public URI getURI() {
		URI routeUri = (URI) gatewayRequest.getExchange()
				.getAttributes()
				.get(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
		return UriComponentsBuilder.fromUri(routeUri)
				.queryParams(gatewayRequest.getQueryParams())
				.build()
				.toUri();
	}

	@Override
	@NonNull
	public HttpHeaders getHeaders() {
		return gatewayRequest.getHeaders();
	}

	@Override
	@NonNull
	public Flux<DataBuffer> getBody() {
		return Flux.empty();
	}
}
