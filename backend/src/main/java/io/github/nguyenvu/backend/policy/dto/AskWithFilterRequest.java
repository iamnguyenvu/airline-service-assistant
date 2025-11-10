package io.github.nguyenvu.backend.policy.dto;

import lombok.Data;

@Data
public class AskWithFilterRequest {
    private String question;
    private String airlineCode;
    private String docType;
    private int topK;
}

