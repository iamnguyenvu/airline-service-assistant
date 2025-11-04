package io.github.nguyenvu.backend.flight.repository;

/**
 * Projection interface for price statistics returned by a custom JPQL query.
 */
public interface PriceStatistics {
    Long getMinPrice();
    Double getAvgPrice();
    Long getMaxPrice();
}

