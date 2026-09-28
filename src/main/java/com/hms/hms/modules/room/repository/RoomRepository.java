package com.hms.hms.modules.room.repository;

import com.hms.hms.modules.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByBranchId(UUID branchId);

    boolean existsByRoomNumberAndBranchId(String roomNumber, UUID branchId);

    List<Room> findByBranchIdIn(List<UUID> branchIds);

    List<Room> findByIdIn(List<UUID> ids);
}