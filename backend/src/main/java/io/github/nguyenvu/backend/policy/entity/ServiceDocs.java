package io.github.nguyenvu.backend.policy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_docs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDocs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_url", columnDefinition = "text")
    private String sourceUrl;

    @Column(name = "airline_code", length = 3)
    private String airlineCode;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "version_tag", length = 100)
    private String versionTag;

    @Column(name = "raw_text", columnDefinition = "text")
    private String rawText;

    @Column(name = "storage_url", columnDefinition = "text")
    private String storageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

