package com.billiontech.bookkeeping.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateClientRequest(
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "SOLE_PROP|LLC|S_CORP|C_CORP")
        String entityType
) {}
