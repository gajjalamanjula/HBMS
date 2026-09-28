package com.hms.hms.modules.room.repository;

import com.hms.hms.common.enums.BedStatus;
import com.hms.hms.modules.room.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BedRepository extends JpaRepository<Bed, UUID> {

    List<Bed> findByRoomId(UUID roomId);

    List<Bed> findByStatusAndRoomIdIn(BedStatus status, List<UUID> roomIds);

    long countByStatusAndRoomIdIn(BedStatus status, List<UUID> roomIds);

    long countByRoomIdIn(List<UUID> roomIds);

    List<Bed> findByIdIn(List<UUID> ids);
}