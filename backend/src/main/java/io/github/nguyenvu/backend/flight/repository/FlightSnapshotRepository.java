package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FlightSnapshotRepository extends JpaRepository<FlightSnapshot, Long>, JpaSpecificationExecutor<FlightSnapshot> {
    /**
     * Find flight snapshots by departure IATA, arrival IATA, and snapshot date.
     */
    List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDate(String depIata,
                                                                String arrIata,
                                                                LocalDate snapshotDate);

    /**
     * Find flight snapshots by departure IATA, arrival IATA, snapshot date, and carrier.
     */
    List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateAndCarrier(String depIata,
                                                                          String arrIata,
                                                                          LocalDate snapshotDate,
                                                                          String carrier);

    /**
     * Find flight snapshots by departure IATA, arrival IATA, and date range.
     */
    @Query("""
                SELECT f FROM FlightSnapshot f
                WHERE f.depIata = :depIata
                AND f.arrIata = :arrIata
                AND f.snapshotDate BETWEEN :startDate AND :endDate
                ORDER BY f.depTime
            """)
    List<FlightSnapshot> findFLightByDateRange(@Param("depIata") String depIata,
                                               @Param("arrIata") String arrIata,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /*
     * Find the top 5 cheapest flight snapshots by departure IATA, arrival IATA, and snapshot date.
     * Replaced invalid JPQL (LIMIT) with a derived query method.
     */
    List<FlightSnapshot> findTop5ByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
            String depIata, String arrIata, LocalDate date
    );

    /**
     * Find flight snapshots by departure IATA, arrival IATA, and snapshot date, ordered by price ascending.
     */
    List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
            String depIata, String arrIata, LocalDate date
    );

    /**
     * Find flight snapshots by departure IATA, arrival IATA, snapshot date, and maximum price.
     */
    List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateAndPriceCentsLessThanEqual(
            String depIata, String arrIata, LocalDate snapshotDate, Long priceCents
    );

    /**
     * Count flight snapshots by departure IATA, arrival IATA, and snapshot date.
     */
    long countByDepIataAndArrIataAndSnapshotDate(String depIata, String arrIata, LocalDate snapshotDate);

    /**
     * Check existence of flight snapshots by departure IATA, arrival IATA, and snapshot date.
     */
    boolean existsByDepIataAndArrIataAndSnapshotDate(String depIata, String arrIata, LocalDate snapshotDate);

    /*
     * Delete flight snapshots older than the specified cutoff date.
     */
    @Modifying
    @Query("DELETE FROM FlightSnapshot f WHERE f.snapshotDate < :cutoffDate")
    int deleteBySnapshotDateBefore(@Param("cutoffDate") LocalDate cutoffDate);

    /*
     * Find distinct carriers for a given route (departure and arrival IATA codes).
     */
    @Query("""
            SELECT DISTINCT f.carrier FROM FlightSnapshot f
            WHERE f.depIata = :depIata AND f.arrIata = :arrIata
            """)
    List<String> findDistinctCarrierByRoute(
            @Param("depIata") String depIata,
            @Param("arrIata") String arrIata
    );

    /*
     * Get price statistics (min, avg, max) for flights on a given route and snapshot date.
     */
    @Query("""
                SELECT
                   MIN(f.priceCents) AS minPrice,
                   AVG(f.priceCents) AS avgPrice,
                   MAX(f.priceCents) AS maxPrice
               FROM FlightSnapshot f
               WHERE f.depIata = :depIata
               AND f.arrIata = :arrIata
               AND f.snapshotDate = :snapshotDate
            """)
    PriceStatistics getPriceStatistics(
            @Param("depIata") String depIata,
            @Param("arrIata") String arrIata,
            @Param("snapshotDate") LocalDate snapshotDate
    );

    /**
     * Convenience method: build a Specification from `FlightSearchCriteria` and run a paged search.
     */
    default Page<FlightSnapshot> search(FlightSearchCriteria criteria, Pageable pageable) {
        return findAll(FlightSnapshotSpecifications.buildFrom(criteria), pageable);
    }

}

