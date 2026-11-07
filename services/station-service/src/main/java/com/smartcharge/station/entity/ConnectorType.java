package com.smartcharge.station.entity;

/**
 * Enum representing all supported EV connector types in the SmartCharge platform.
 * Covers global standards as well as India-specific Bharat standards.
 */
public enum ConnectorType {

    /** Combined Charging System – AC+DC, common in European and US markets */
    CCS,

    /** CHAdeMO – DC fast charging, popular in Japanese vehicles */
    CHADEMO,

    /** IEC 62196 Type 2 – Most common AC standard in Europe */
    TYPE2,

    /** SAE J1772 Type 1 – AC standard for North American vehicles */
    TYPE1,

    /** GB/T – Chinese national standard, used in some imported EVs */
    GB_T,

    /** Bharat AC-001 – India-specific 15A AC standard (slow charging) */
    BHARAT_AC001,

    /** Bharat DC-001 – India-specific DC fast-charging standard */
    BHARAT_DC001,

    /** Tesla proprietary connector (NACS) */
    TESLA,

    /** Schuko CEE 7/4 – Standard European household plug, Level 1 charging */
    SCHUKO
}
