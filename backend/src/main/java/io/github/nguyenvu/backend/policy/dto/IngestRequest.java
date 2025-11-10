package io.github.nguyenvu.backend.policy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class IngestRequest {
    @NotBlank
    private String text;
    private Map<String, Object> metadata;
}
