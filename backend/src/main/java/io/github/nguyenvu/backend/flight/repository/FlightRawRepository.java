package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.entity.FlightRaw;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FlightRaw entity.
 * Stores raw JSON payloads from flight data sources for audit and debugging.
 */
public interface FlightRawRepository extends JpaRepository<FlightRaw, Long> {

    // === Basic Queries ===
    
    /** Find raw payloads by snapshot ID */
    List<FlightRaw> findBySnapshotId(Long snapshotId);

    /** Find raw payloads by snapshot entity */
    List<FlightRaw> findBySnapshot(FlightSnapshot snapshot);

    /** Find raw payloads by snapshot with pagination */
    Page<FlightRaw> findBySnapshot(FlightSnapshot snapshot, Pageable pageable);

    /** Find most recent raw payload for snapshot */
    Optional<FlightRaw> findTopBySnapshotOrderByCreatedAtDesc(FlightSnapshot snapshot);

    /** Find raw payloads created within time range */
    List<FlightRaw> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // === Count & Existence Checks ===
    
    /** Count raw payloads for snapshot */
    long countBySnapshotId(Long snapshotId);

    /** Check if raw payload exists for snapshot */
    boolean existsBySnapshotId(Long snapshotId);

    /** Delete raw payloads for snapshot (returns deleted count) */
    int deleteBySnapshotId(Long snapshotId);

    // === Payload Search (JPQL) ===
    
    /** Search payload text (case-insensitive, portable) */
    @Query("SELECT f FROM FlightRaw f WHERE LOWER(f.payload) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<FlightRaw> searchByPayloadContaining(@Param("term") String term);

    // === Payload Search (Native - PostgreSQL specific) ===
    
    /** Search JSONB payload text (case-insensitive, PostgreSQL) */
    @Query(value = "SELECT * FROM flight_raw WHERE payload::text ILIKE '%' || :term || '%'", nativeQuery = true)
    List<FlightRaw> searchByPayloadContainingNative(@Param("term") String term);

    /** Search by top-level JSON field (PostgreSQL). Example: findByPayloadField("flightNo", "VJ123") */
    @Query(value = "SELECT * FROM flight_raw WHERE payload->>:key = :value", nativeQuery = true)
    List<FlightRaw> findByPayloadField(@Param("key") String key, @Param("value") String value);

    // === Utility Methods ===
    
    /** Fetch N most recent raw payloads for snapshot */
    default List<FlightRaw> findRecentBySnapshot(FlightSnapshot snapshot, int limit) {
        if (snapshot == null || limit <= 0) return List.of();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return findBySnapshot(snapshot, pageable).getContent();
    }
}

}
