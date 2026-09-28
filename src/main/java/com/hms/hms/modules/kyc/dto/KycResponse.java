package com.hms.hms.modules.kyc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KycResponse {

    private String aadhaarUrl;
    private String photoUrl;
    private Boolean verified;
}