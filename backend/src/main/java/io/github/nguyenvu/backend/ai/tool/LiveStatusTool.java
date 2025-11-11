package io.github.nguyenvu.backend.ai.tool;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component("liveStatus")
public class LiveStatusTool implements Function<LiveStatusTool.Request, Map<String, Object>> {

    @Data
    public static class Request {
        private String flightNo;
        private String date;
    }

    @Override
    public Map<String, Object> apply(Request req) {
        return Map.of(
                "flightNo", req.getFlightNo(),
                "date", req.getDate(),
                "status", "ON_TIME",
                "source", "mock"
        );
    }
}


