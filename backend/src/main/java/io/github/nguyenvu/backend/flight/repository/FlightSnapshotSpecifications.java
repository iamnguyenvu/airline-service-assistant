package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

@NoArgsConstructor
public final class FlightSnapshotSpecifications {
    public static Specification<FlightSnapshot> byDeptIata(String depIata) {
        return (root, query, criteriaBuilder) ->
                depIata == null ? null :
                criteriaBuilder.equal(root.get("depIata"), depIata);
    }

    public static Specification<FlightSnapshot> byArrIata(String arrIata) {
        return (root, query, criteriaBuilder) ->
                arrIata == null ? null :
                criteriaBuilder.equal(root.get("arrIata"), arrIata);
    }

    public static Specification<FlightSnapshot> bySnapshotDate(LocalDate snapshotDate) {
        return (root, query, criteriaBuilder) ->
                snapshotDate == null ? null :
                criteriaBuilder.equal(root.get("snapshotDate"), snapshotDate);
    }

    public static Specification<FlightSnapshot> bySnapshotDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null;
            } else if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("snapshotDate"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("snapshotDate"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("snapshotDate"), endDate);
            }
        };
    }

    public static Specification<FlightSnapshot> byCarrier(String carrier) {
        return (root, query, criteriaBuilder) ->
                carrier == null ? null :
                criteriaBuilder.equal(root.get("carrier"), carrier);
    }

    public static Specification<FlightSnapshot> byMaxPriceCents(Long maxPriceCents) {
        return (root, query, criteriaBuilder) ->
                maxPriceCents == null ? null :
                criteriaBuilder.lessThanOrEqualTo(root.get("priceCents"), maxPriceCents);
    }

    public static Specification<FlightSnapshot> byMinPriceCents(Long minPriceCents) {
        return (root, query, criteriaBuilder) ->
                minPriceCents == null ? null :
                criteriaBuilder.greaterThanOrEqualTo(root.get("priceCents"), minPriceCents);
    }

    public static Specification<FlightSnapshot> buildFrom(FlightSearchCriteria criteria) {
        Specification<FlightSnapshot> specification =
                (criteria.getStartDate() != null || criteria.getEndDate() != null)
                        ? bySnapshotDateBetween(criteria.getStartDate(), criteria.getEndDate())
                        : bySnapshotDate(criteria.getSnapshotDate());

        return Specification.allOf(
                byDeptIata(criteria.getDepIata()),
                byArrIata(criteria.getArrIata()),
                specification,
                byCarrier(criteria.getCarrier()),
                byMaxPriceCents(criteria.getMaxPriceCents()),
                byMinPriceCents(criteria.getMinPriceCents())
        );
    }

}
