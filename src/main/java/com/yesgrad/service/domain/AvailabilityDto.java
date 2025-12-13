package com.yesgrad.service.domain;

import lombok.Data;

@Data
public class AvailabilityDto {

    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private Boolean isAvailable;
}
