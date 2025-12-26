package com.green.energy.tracker.cloud.site_processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationWrite {
    private double latitude;
    private double longitude;
}
