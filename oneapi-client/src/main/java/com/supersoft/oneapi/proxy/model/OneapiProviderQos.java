package com.supersoft.oneapi.proxy.model;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class OneapiProviderQos extends OneapiBaseObject {
    String provider;
    String model;

    Date time = new Date();

    long count;
    long successCount;

    double oneMinuteTps;
    double oneMinuteSuccessTps;

    double successRate;

    double oneMinuteRt;
}
