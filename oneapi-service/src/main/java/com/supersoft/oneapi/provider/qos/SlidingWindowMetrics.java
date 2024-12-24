package com.supersoft.oneapi.provider.qos;

import com.codahale.metrics.*;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class SlidingWindowMetrics {
    private final String provider;
    private final String model;
    private final Timer requestsTimer;
    private final Meter requestsMeter;
    private final Meter requestsSuccessMeter;
    private final Object lock = new Object();


    public SlidingWindowMetrics(String provider, String model) {
        this.provider = provider;
        this.model = model;
        SlidingTimeWindowReservoir slidingWindowReservoir = new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES);
        requestsTimer = new Timer(slidingWindowReservoir);
        MetricRegistry metrics = new MetricRegistry();
        requestsMeter = metrics.meter("requests");
        requestsSuccessMeter = metrics.meter("requests-success");
    }

    public void recordRequest(long duration, TimeUnit timeUnit, Boolean success) {
        requestsTimer.update(duration, timeUnit);
        requestsMeter.mark();
        if (Boolean.TRUE.equals(success)) {
            requestsSuccessMeter.mark();
        }
    }

    public double getRt() {
        Snapshot snapshot = requestsTimer.getSnapshot();
        return snapshot.getMean() / 1_000_000.0;
    }

    public double getOneMinuteRate() {
        return requestsMeter.getOneMinuteRate();
    }

    public double getOneMinuteSuccessRate() {
        return requestsSuccessMeter.getOneMinuteRate();
    }

    public long getTotalCount() {
        return requestsMeter.getCount();
    }

    public long getSuccessCount() {
        return requestsSuccessMeter.getCount();
    }
}

