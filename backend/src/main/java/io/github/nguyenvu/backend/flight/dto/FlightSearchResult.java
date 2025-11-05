package io.github.nguyenvu.backend.flight.dto;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Flight search result with pagination metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchResult {
    private List<FlightSnapshot> flights;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private FlightSearchCriteria searchCriteria;
}
