package com.smartcharge.station.entity;

/**
 * Overall operational status of a charging station.
 */
public enum StationStatus {

    /** Station is fully operational and accepting sessions */
    ACTIVE,

    /** Station is offline (decommissioned or temporarily shut down) */
    INACTIVE,

    /** Station is undergoing scheduled or unscheduled maintenance */
    MAINTENANCE,

    /** Station is planned/under installation, not yet accepting sessions */
    COMING_SOON
}
