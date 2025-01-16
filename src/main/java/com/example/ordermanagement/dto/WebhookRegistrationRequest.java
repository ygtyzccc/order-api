package com.example.ordermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRegistrationRequest {
    @NotBlank
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    @NotBlank
    @Pattern(regexp = "^(ORDER_CREATED|ORDER_CANCELLED)$",
            message = "Event type must be either ORDER_CREATED or ORDER_CANCELLED")
    private String eventType;
} 