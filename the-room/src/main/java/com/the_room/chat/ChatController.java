package com.the_room.chat;

import java.security.Principal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messaging;
    private final ChatMessageRepository repo;
    private final int historyMax;

    public ChatController(
            SimpMessagingTemplate messaging,
            ChatMessageRepository repo,
            @Value("${chat.history.max:50}") int historyMax
    ) {
        this.messaging = messaging;
        this.repo = repo;
        this.historyMax = Math.max(1, historyMax);
    }

    // Client sends to /app/chat.send
    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessage incoming) {
        String from = safe(incoming.from(), "anon");
        String text = safe(incoming.text(), "");
        if (text.isBlank()) return;

        // Basic guardrails (keep it simple)
        if (from.length() > 64) from = from.substring(0, 64);
        if (text.length() > 2000) text = text.substring(0, 2000);

        Instant ts = incoming.ts() == null ? Instant.now() : incoming.ts();

        // Persist
        ChatMessageEntity saved = repo.save(
                new ChatMessageEntity(UUID.randomUUID(), from, text, ts)
        );

        // Broadcast
        messaging.convertAndSend("/topic/public",
                new ChatMessage(saved.getUsername(), saved.getContent(), saved.getCreatedAt())
        );
    }

    // Client sends to /app/chat.hello right after connect to receive recent history
    @SuppressWarnings("null")
    @MessageMapping("/chat.hello")
    public void hello(Principal principal, @Payload(required = false) String ignored) {
        List<ChatMessage> history = repo.findRecent(PageRequest.of(0, historyMax)).stream()
                // DB returns newest-first; flip to oldest-first for nicer display
                .map(m -> new ChatMessage(m.getUsername(), m.getContent(), m.getCreatedAt()))
                .sorted(Comparator.comparing(ChatMessage::ts))
                .toList();

        messaging.convertAndSendToUser(principal.getName(), "/queue/history", history);
    }

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }
}
