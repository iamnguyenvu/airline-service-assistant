package io.github.nguyenvu.backend.policy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fare_rule_norm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRuleNorm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_code", length = 3, nullable = false)
    private String airlineCode;

    @Column(name = "cabin", length = 20)
    private String cabin;

    @Column(name = "fare_family", length = 50)
    private String fareFamily;

    @Column(name = "change_fee_cents")
    private Long changeFeeCents;

    @Column(name = "refund_fee_cents")
    private Long refundFeeCents;

    @Column(name = "refund_allowed")
    private Boolean refundAllowed;

    @Column(name = "change_deadline_hours")
    private Integer changeDeadlineHours;

    @Column(name = "no_show_fee_cents")
    private Long noShowFeeCents;

    @Column(name = "carry_on_kg", precision = 5, scale = 2)
    private BigDecimal carryOnKg;

    @Column(name = "checked_bag_kg", precision = 5, scale = 2)
    private BigDecimal checkedBagKg;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

