package com.relatos.api_gateway.config;

import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	/**
	 * WebClient con balanceo de carga reactivo usando Eureka.
	 * Se usa ReactorLoadBalancerExchangeFilterFunction de forma explicita
	 * para evitar conflictos con el WebClient interno de Spring Cloud Gateway.
	 */
	@Bean
	public WebClient loadBalancedWebClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
		return WebClient.builder()
				.filter(lbFunction)
				.build();
	}
}
