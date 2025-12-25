package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.entity.Airport;
import io.github.nguyenvu.backend.flight.entity.Airport.AirportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for Airport master data.
 */
public interface AirportRepository extends JpaRepository<Airport, String> {

    /**
     * Find all active airports.
     */
    List<Airport> findByActiveTrue();

    /**
     * Find all active airports of a specific type.
     */
    List<Airport> findByAirportTypeAndActiveTrue(AirportType type);

    /**
     * Find all active Vietnam domestic airports.
     */
    @Query("SELECT a FROM Airport a WHERE a.country = 'Vietnam' AND a.active = true")
    List<Airport> findVietnamDomesticAirports();

    /**
     * Get all IATA codes for Vietnam domestic airports.
     */
    @Query("SELECT a.iataCode FROM Airport a WHERE a.country = 'Vietnam' AND a.active = true")
    List<String> findVietnamDomesticIataCodes();

    /**
     * Check if airport code is valid (exists and active).
     */
    boolean existsByIataCodeAndActiveTrue(String iataCode);

    /**
     * Find airport by IATA code if active.
     */
    Airport findByIataCodeAndActiveTrue(String iataCode);
}
