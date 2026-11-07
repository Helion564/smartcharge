package com.smartcharge.station.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing EV charging power levels.
 * Each level carries its typical power output in kW and a user-friendly display name.
 */
@Getter
@RequiredArgsConstructor
public enum PowerLevel {

    /** Standard household outlet charging – ~1.4 kW */
    LEVEL_1(1.4, "Level 1 (1.4kW)"),

    /** Home/destination charging via dedicated EVSE – ~7.2 kW */
    LEVEL_2(7.2, "Level 2 (7.2kW)"),

    /** Public DC fast charging – ~50 kW */
    DC_FAST(50.0, "DC Fast (50kW)"),

    /** High-power DC ultra-fast charging – ~150 kW */
    ULTRA_FAST(150.0, "Ultra Fast (150kW)"),

    /** Next-gen hyper-fast DC charging – ~350 kW */
    HYPER_FAST(350.0, "Hyper Fast (350kW)");

    /** Nominal power output in kilowatts */
    private final double powerKw;

    /** Human-readable label for UI display */
    private final String displayName;
}
