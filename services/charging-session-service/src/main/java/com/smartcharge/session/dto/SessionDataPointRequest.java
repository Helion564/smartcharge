package com.smartcharge.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataPointRequest {
    private double currentPowerKw;
    private double energyKwh;
    private double stateOfCharge;
    private double currentAmps;
    private double voltageVolts;
    private double temperature;
}
