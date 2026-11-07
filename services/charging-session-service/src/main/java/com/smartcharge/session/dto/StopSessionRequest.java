package com.smartcharge.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopSessionRequest {
    private String stopReason;
    private Double finalEnergyKwh;
}
