package com.relatos.api_gateway.decorator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relatos.api_gateway.model.GatewayRequest;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

public class PostRequestDecorator extends ServerHttpRequestDecorator {

	private final GatewayRequest gatewayRequest;
	private final ObjectMapper objectMapper;

	public PostRequestDecorator(GatewayRequest gatewayRequest, ObjectMapper objectMapper) {
		super(gatewayRequest.getExchange().getRequest());
		this.gatewayRequest = gatewayRequest;
		this.objectMapper = objectMapper;
	}

	@Override
	@NonNull
	public HttpMethod getMethod() {
		return HttpMethod.POST;
	}

	@Override
	@NonNull
	public URI getURI() {
		URI routeUri = (URI) gatewayRequest.getExchange()
				.getAttributes()
				.get(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
		return UriComponentsBuilder.fromUri(routeUri).build().toUri();
	}

	@Override
	@NonNull
	public HttpHeaders getHeaders() {
		return gatewayRequest.getHeaders();
	}

	@Override
	@NonNull
	@SneakyThrows
	public Flux<DataBuffer> getBody() {
		if (gatewayRequest.getBody() == null) {
			return Flux.empty();
		}
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		byte[] bodyData = objectMapper.writeValueAsBytes(gatewayRequest.getBody());
		DataBuffer buffer = bufferFactory.allocateBuffer(bodyData.length);
		buffer.write(bodyData);
		return Flux.just(buffer);
	}
}
