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

/**
 * Repository for FlightSnapshot entity.
 * Provides queries for flight search, price analysis, and data cleanup.
 */
public interface FlightSnapshotRepository extends JpaRepository<FlightSnapshot, Long>,
                JpaSpecificationExecutor<FlightSnapshot> {

        // === Basic Queries ===

        /** Find flights by route and snapshot date */
        List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDate(
                        String depIata, String arrIata, LocalDate snapshotDate);

        /** Find flights by route, snapshot date, and carrier */
        List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateAndCarrier(
                        String depIata, String arrIata, LocalDate snapshotDate, String carrier);

        /** Check if flights exist for route and date */
        boolean existsByDepIataAndArrIataAndSnapshotDate(
                        String depIata, String arrIata, LocalDate snapshotDate);

        /** Count flights for route and date */
        long countByDepIataAndArrIataAndSnapshotDate(
                        String depIata, String arrIata, LocalDate snapshotDate);

        /** Count all flights for a specific date */
        long countBySnapshotDate(LocalDate snapshotDate);

        // === Price-Based Queries ===

        /** Find top 5 cheapest flights for route and date */
        List<FlightSnapshot> findTop5ByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
                        String depIata, String arrIata, LocalDate date);

        /** Find all flights sorted by price (cheapest first) */
        List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
                        String depIata, String arrIata, LocalDate date);

        /** Find flights under max price */
        List<FlightSnapshot> findByDepIataAndArrIataAndSnapshotDateAndPriceCentsLessThanEqual(
                        String depIata, String arrIata, LocalDate snapshotDate, Long maxPriceCents);

        // === Date Range Queries ===

        /** Find flights within date range */
        @Query("""
                        SELECT f FROM FlightSnapshot f
                        WHERE f.depIata = :depIata AND f.arrIata = :arrIata
                          AND f.snapshotDate BETWEEN :startDate AND :endDate
                        ORDER BY f.depTime
                        """)
        List<FlightSnapshot> findFlightsByDateRange(
                        @Param("depIata") String depIata,
                        @Param("arrIata") String arrIata,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // === Analytics Queries ===

        /** Get distinct carriers for route */
        @Query("SELECT DISTINCT f.carrier FROM FlightSnapshot f WHERE f.depIata = :depIata AND f.arrIata = :arrIata")
        List<String> findDistinctCarriersByRoute(@Param("depIata") String depIata, @Param("arrIata") String arrIata);

        /** Get price statistics (min, avg, max) for route and date */
        @Query("""
                        SELECT MIN(f.priceCents) AS minPrice,
                               AVG(f.priceCents) AS avgPrice,
                               MAX(f.priceCents) AS maxPrice
                        FROM FlightSnapshot f
                        WHERE f.depIata = :depIata AND f.arrIata = :arrIata
                          AND f.snapshotDate = :snapshotDate
                        """)
        PriceStatistics getPriceStatistics(
                        @Param("depIata") String depIata,
                        @Param("arrIata") String arrIata,
                        @Param("snapshotDate") LocalDate snapshotDate);

        // === Data Management ===

        /** Delete old snapshots (for data retention policy) */
        @Modifying
        @Query("DELETE FROM FlightSnapshot f WHERE f.snapshotDate < :cutoffDate")
        int deleteBySnapshotDateBefore(@Param("cutoffDate") LocalDate cutoffDate);

        // === Today's Flights (Prioritized) ===
        // Vietnam airports: major airports in Vietnam (SGN, HAN, DAD, HPH, CXR, DLI,
        // PQC, VCL, VCS, BMV, VKG, VII, VDH, THD, TBB, UIH, VDO, VIN)

        /** Find flights for a specific date - domestic Vietnam flights first */
        @Query("""
                        SELECT f FROM FlightSnapshot f
                        WHERE f.snapshotDate = :date
                          AND f.depIata IN ('SGN','HAN','DAD','HPH','CXR','DLI','PQC','VCL','VCS','BMV','VKG','VII','VDH','THD','TBB','UIH','VDO','VIN')
                          AND f.arrIata IN ('SGN','HAN','DAD','HPH','CXR','DLI','PQC','VCL','VCS','BMV','VKG','VII','VDH','THD','TBB','UIH','VDO','VIN')
                        ORDER BY f.depTime ASC
                        """)
        List<FlightSnapshot> findTodayDomesticFlights(@Param("date") LocalDate date);

        /** Find flights for a specific date - Vietnam to international */
        @Query("""
                        SELECT f FROM FlightSnapshot f
                        WHERE f.snapshotDate = :date
                          AND f.depIata IN ('SGN','HAN','DAD','HPH','CXR','DLI','PQC','VCL','VCS','BMV','VKG','VII','VDH','THD','TBB','UIH','VDO','VIN')
                          AND f.arrIata NOT IN ('SGN','HAN','DAD','HPH','CXR','DLI','PQC','VCL','VCS','BMV','VKG','VII','VDH','THD','TBB','UIH','VDO','VIN')
                        ORDER BY f.depTime ASC
                        """)
        List<FlightSnapshot> findTodayInternationalFlights(@Param("date") LocalDate date);

        /** Count total flights in database (for debugging) */
        @Query("SELECT COUNT(f) FROM FlightSnapshot f")
        long countAllFlights();

        /** Find any flights for a date range (for debugging) - returns first 100 */
        @Query("""
                        SELECT f FROM FlightSnapshot f
                        WHERE f.snapshotDate BETWEEN :startDate AND :endDate
                        ORDER BY f.snapshotDate ASC, f.depTime ASC
                        """)
        List<FlightSnapshot> findFlightsInDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // === Specification-Based Search ===

        /** Search with complex criteria (uses FlightSnapshotSpecifications) */
        default Page<FlightSnapshot> search(FlightSearchCriteria criteria, Pageable pageable) {
                return findAll(FlightSnapshotSpecifications.buildFrom(criteria), pageable);
        }
}
