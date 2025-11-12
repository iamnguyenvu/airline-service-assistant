package io.github.nguyenvu.backend.ai.repository;

import io.github.nguyenvu.backend.ai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findBySessionId(String sessionId);

    @Query("SELECT c FROM Conversation c ORDER BY c.updatedAt DESC")
    List<Conversation> findAllOrderByUpdatedAtDesc();

    @Query("SELECT c FROM Conversation c WHERE c.sessionId = :sessionId")
    Optional<Conversation> findWithMessagesBySessionId(@Param("sessionId") String sessionId);
}

