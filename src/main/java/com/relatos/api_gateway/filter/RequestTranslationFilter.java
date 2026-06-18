package com.relatos.api_gateway.filter;

import com.relatos.api_gateway.decorator.RequestDecoratorFactory;
import com.relatos.api_gateway.model.GatewayRequest;
import com.relatos.api_gateway.utils.RequestBodyExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestTranslationFilter implements GlobalFilter, Ordered {

	private final RequestBodyExtractor requestBodyExtractor;
	private final RequestDecoratorFactory requestDecoratorFactory;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		if (path.startsWith("/actuator")) {
			return chain.filter(exchange);
		}

		if (exchange.getRequest().getHeaders().getContentType() == null
				|| !HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
			log.info("Request rejected: must be POST with Content-Type");
			exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
			return exchange.getResponse().setComplete();
		}

		return DataBufferUtils.join(exchange.getRequest().getBody())
				.flatMap(dataBuffer -> {
					try {
						GatewayRequest request = requestBodyExtractor.getRequest(exchange, dataBuffer);
						ServerHttpRequest mutatedRequest = requestDecoratorFactory.getDecorator(request);
						exchange.getAttributes().put(
								ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
								mutatedRequest.getURI());
						if (request.getQueryParams() != null) {
							request.getQueryParams().clear();
						}
						log.info("Proxying request: {} {}", mutatedRequest.getMethod(), mutatedRequest.getURI());
						return chain.filter(exchange.mutate().request(mutatedRequest).build());
					} catch (IllegalArgumentException exception) {
						log.info("Request rejected: {}", exception.getMessage());
						exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
						return exchange.getResponse().setComplete();
					}
				});
	}

	@Override
	public int getOrder() {
		return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
	}
}
