package com.hms.hms.modules.publicuser.dto;

import com.hms.hms.common.enums.BedStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BedResponse {

    private UUID id;

    private String bedNumber;

    private BedStatus status;
}