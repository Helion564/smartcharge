package com.smartcharge.station.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcmImportResponse {
    private int importedCount;
    private int failedCount;
    private int skippedCount;
    private String message;
}
