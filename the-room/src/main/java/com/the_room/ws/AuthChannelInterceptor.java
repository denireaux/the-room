package com.the_room.ws;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final String requiredKey;

    public AuthChannelInterceptor(@Value("${chat.key}") String requiredKey) {
        this.requiredKey = requiredKey;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String provided = firstNativeHeader(accessor, "X-CHAT-KEY");

            if (provided == null || !provided.equals(requiredKey)) {
                // hard reject
                throw new IllegalArgumentException("Unauthorized");
            }

            // Needed for /user/queue/history to work (a "user identity" per connection)
            accessor.setUser(new StompPrincipal("u-" + UUID.randomUUID()));
        }

        return message;
    }

    private @Nullable String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values == null || values.isEmpty()) return null;
        return values.get(0);
    }

    private static final class StompPrincipal implements Principal {
        private final String name;
        private StompPrincipal(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
