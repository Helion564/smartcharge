package com.smartcharge.station.entity;

/**
 * Real-time availability status of an individual connector.
 */
public enum ConnectorStatus {

    /** Connector is free and ready to charge */
    AVAILABLE,

    /** Connector is actively being used by a vehicle */
    OCCUPIED,

    /** Connector is faulty or broken */
    OUT_OF_ORDER,

    /** Connector is reserved for a future session */
    RESERVED
}
