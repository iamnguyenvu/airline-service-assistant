package io.github.nguyenvu.backend.ai.repository;

import io.github.nguyenvu.backend.ai.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    @Query("SELECT m FROM ConversationMessage m WHERE m.conversation.sessionId = :sessionId ORDER BY m.createdAt ASC")
    List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") String sessionId);

    @Query("SELECT m FROM ConversationMessage m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<ConversationMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
}

