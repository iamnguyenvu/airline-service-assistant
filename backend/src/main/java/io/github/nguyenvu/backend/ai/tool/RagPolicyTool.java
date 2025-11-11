package io.github.nguyenvu.backend.ai.tool;

import io.github.nguyenvu.backend.policy.service.PolicyQAService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component("ragPolicy")
@RequiredArgsConstructor
public class RagPolicyTool implements Function<RagPolicyTool.Request, String> {

    private final PolicyQAService policyQAService;

    @Data
    public static class Request {
        private String query;
        private String airline;
        private String docType;
        private Integer topK;
    }

    @Override
    public String apply(Request req) {
        log.info("[Tool] ragPolicy {}", req);
        if (req.getAirline() == null && req.getDocType() == null) {
            return policyQAService.ask(req.getQuery());
        }
        return policyQAService.askWithFilter(
                req.getQuery(), req.getAirline(), req.getDocType(),
                req.getTopK() != null ? req.getTopK() : 8
        );
    }
}


