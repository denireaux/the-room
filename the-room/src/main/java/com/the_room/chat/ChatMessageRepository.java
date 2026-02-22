package com.the_room.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    @Query("select m from ChatMessageEntity m order by m.createdAt desc")
    List<ChatMessageEntity> findRecent(Pageable pageable);
}
