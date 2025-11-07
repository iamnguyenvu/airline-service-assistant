package io.github.nguyenvu.backend.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UsersPublic user;

    @Column(name = "preferred_seat", length = 10)
    private String preferredSeat;

    @Column(name = "max_stops")
    private Short maxStops;

    // JPA doesn't support basic String[] mapping; use ElementCollection -> separate table
    @ElementCollection
    @CollectionTable(
        name = "user_preferences_preferred_airlines",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    )
    @Column(name = "airline")
    private List<String> preferredAirlines;

    @ElementCollection
    @CollectionTable(
        name = "user_preferences_avoid_airlines",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    )
    @Column(name = "airline")
    private List<String> avoidAirlines;

    @Column(name = "max_duration_hours")
    private Integer maxDurationHours;

    @Column(name = "preferences", columnDefinition = "jsonb")
    private String preferences;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
