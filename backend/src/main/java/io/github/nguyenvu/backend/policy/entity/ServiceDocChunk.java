package io.github.nguyenvu.backend.policy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_doc_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDocChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private ServiceDocs doc;

    @Column(name = "section_title", length = 500)
    private String sectionTitle;

    @Column(name = "chunk", columnDefinition = "text", nullable = false)
    private String chunk;

    @Column(name = "embedding", columnDefinition = "vector(384)")
    private Float[] embedding;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

