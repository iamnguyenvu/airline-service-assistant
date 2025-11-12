package io.github.nguyenvu.backend.ai.service;

import io.github.nguyenvu.backend.ai.dto.ChatAskRequest;
import io.github.nguyenvu.backend.ai.entity.Conversation;
import io.github.nguyenvu.backend.ai.entity.ConversationMessage;
import io.github.nguyenvu.backend.ai.repository.ConversationMessageRepository;
import io.github.nguyenvu.backend.ai.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;

    /**
     * Get or create conversation by session ID
     */
    @Transactional
    public Conversation getOrCreateConversation(String sessionId) {
        return conversationRepository.findBySessionId(sessionId)
            .orElseGet(() -> {
                Conversation conv = Conversation.builder()
                    .sessionId(sessionId)
                    .title("New Conversation")
                    .build();
                return conversationRepository.save(conv);
            });
    }

    /**
     * Save user message
     */
    @Transactional
    public ConversationMessage saveUserMessage(Conversation conversation, String content) {
        ConversationMessage message = ConversationMessage.builder()
            .conversation(conversation)
            .role(ConversationMessage.MessageRole.USER)
            .content(content)
            .build();
        return messageRepository.save(message);
    }

    /**
     * Save assistant message
     */
    @Transactional
    public ConversationMessage saveAssistantMessage(
            Conversation conversation, 
            String content, 
            Boolean usedTools,
            String model) {
        ConversationMessage message = ConversationMessage.builder()
            .conversation(conversation)
            .role(ConversationMessage.MessageRole.ASSISTANT)
            .content(content)
            .usedTools(usedTools != null ? usedTools : false)
            .model(model)
            .build();
        return messageRepository.save(message);
    }

    /**
     * Get conversation history (last N messages)
     */
    @Transactional(readOnly = true)
    public List<ConversationMessage> getConversationHistory(String sessionId, int limit) {
        List<ConversationMessage> allMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (allMessages.size() <= limit) {
            return allMessages;
        }
        // Return last N messages
        return allMessages.subList(Math.max(0, allMessages.size() - limit), allMessages.size());
    }

    /**
     * Convert conversation history to ChatAskRequest.ChatMessage format
     */
    public List<ChatAskRequest.ChatMessage> toChatMessages(List<ConversationMessage> messages) {
        return messages.stream()
            .map(msg -> {
                ChatAskRequest.ChatMessage chatMsg = new ChatAskRequest.ChatMessage();
                chatMsg.setRole(msg.getRole().name().toLowerCase());
                chatMsg.setContent(msg.getContent());
                return chatMsg;
            })
            .collect(Collectors.toList());
    }

    /**
     * Update conversation title from first user message
     */
    @Transactional
    public void updateConversationTitle(Conversation conversation, String title) {
        if (title != null && !title.trim().isEmpty() && title.length() <= 500) {
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }
    }
}

