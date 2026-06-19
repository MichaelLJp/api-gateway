package com.relatos.api_gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateTokenResponse {

	private boolean valid;

	private String accessToken;

	private Long userId;

	private String email;

	private String role;
}
