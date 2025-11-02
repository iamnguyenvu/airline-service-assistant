package io.github.nguyenvu.backend.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users_public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersPublic {
    @Id
    private UUID id;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "preferred_airline", length = 3)
    private String preferredAirline;

    @Column(name = "preferred_cabin", length = 20)
    private String preferredCabin;

    @Column(name = "max_budget_cents")
    private Long maxBudgetCents;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

