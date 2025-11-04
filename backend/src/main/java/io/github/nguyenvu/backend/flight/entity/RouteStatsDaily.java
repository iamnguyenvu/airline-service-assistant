package io.github.nguyenvu.backend.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "route_stats_daily", uniqueConstraints = @UniqueConstraint(columnNames = {"route_key", "date"}))
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStatsDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_key", nullable = false, length = 10)
    private String routeKey;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "min_price_cents")
    private Long minPriceCents;

    @Column(name = "p50_price_cents")
    private Long p50PriceCents;

    @Column(name = "p90_price_cents")
    private Long p90PriceCents;

    @Column(name = "avg_duration_min")
    private Integer avgDurationMin;

    @Column(name = "avg_co2_kg", precision = 10, scale = 2)
    private BigDecimal avgCo2Kg;

    @Column(name = "flight_count")
    private Integer flightCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        RouteStatsDaily that = (RouteStatsDaily) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
