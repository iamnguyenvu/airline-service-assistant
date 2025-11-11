package io.github.nguyenvu.backend.ai.tool;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component("estimateCO2")
public class EstimateCO2Tool implements Function<EstimateCO2Tool.Request, Map<String, Object>> {

    @Data
    public static class Request {
        private String fromIata;
        private String toIata;
        private Integer durationMin;
    }

    @Override
    public Map<String, Object> apply(Request req) {
        double hours = (req.getDurationMin() != null && req.getDurationMin() > 0)
                ? req.getDurationMin() / 60.0
                : 1.5;
        double distanceKm = hours * 800.0;
        double litersPerKm = 0.04;
        double kgPerLiter = 2.52;
        double estimateKg = distanceKm * litersPerKm * kgPerLiter;
        return Map.of(
                "from", req.getFromIata(),
                "to", req.getToIata(),
                "co2Kg", Math.round(estimateKg * 10.0) / 10.0
        );
    }
}


