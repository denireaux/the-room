package com.the_room.chat;

import java.time.Instant;

public record ChatMessage(
        String from,
        String text,
        Instant ts
) {}
