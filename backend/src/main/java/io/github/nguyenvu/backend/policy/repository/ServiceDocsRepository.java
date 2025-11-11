package io.github.nguyenvu.backend.policy.repository;

import io.github.nguyenvu.backend.policy.entity.ServiceDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceDocsRepository extends JpaRepository<ServiceDocs, Long> {
    
    List<ServiceDocs> findByAirlineCodeOrderByCreatedAtDesc(String airlineCode);
    
    List<ServiceDocs> findByDocTypeOrderByCreatedAtDesc(String docType);
    
    List<ServiceDocs> findByAirlineCodeAndDocTypeOrderByCreatedAtDesc(String airlineCode, String docType);
    
    @Query("SELECT s FROM ServiceDocs s WHERE s.airlineCode IN :airlineCodes ORDER BY s.createdAt DESC")
    List<ServiceDocs> findByAirlineCodesOrderByCreatedAtDesc(@Param("airlineCodes") List<String> airlineCodes);
    
    long countByAirlineCode(String airlineCode);
    
    long countByDocType(String docType);
}