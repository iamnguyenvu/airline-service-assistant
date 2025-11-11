package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MockFlightIngestionProvider implements FlightIngestionProvider {
    @Override
    public String name() {
        return "mock";
    }

    @Override
    public List<FlightSnapshot> fetchDaily(LocalDate date) {
        List<FlightSnapshot> list = new ArrayList<>();
        list.add(sample(date, "SGN", "HAN", "VN", "VN210", 130, 0, 1950000L));
        list.add(sample(date, "SGN", "DAD", "VJ", "VJ636", 85, 0, 950000L));
        list.add(sample(date, "HAN", "DAD", "QH", "QH103", 80, 0, 890000L));
        return list;
    }

    private FlightSnapshot sample(LocalDate date, String dep, String arr,
                                  String carrier, String flightNo, int durationMin, int stops, long price) {
        LocalDateTime depTime = date.atTime(8, 30);
        LocalDateTime arrTime = depTime.plusMinutes(durationMin);
        return FlightSnapshot.builder()
                .snapshotDate(date)
                .depIata(dep)
                .arrIata(arr)
                .depTime(depTime)
                .arrTime(arrTime)
                .carrier(carrier)
                .flightNo(flightNo)
                .durationMin(durationMin)
                .stops((short) stops)
                .fareFamily("Eco")
                .baggageKg(BigDecimal.valueOf(20))
                .priceCents(price)
                .currency("VND")
                .source("mock")
                .build();
    }
}


