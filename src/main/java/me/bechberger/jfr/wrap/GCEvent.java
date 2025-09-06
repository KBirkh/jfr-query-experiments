package me.bechberger.jfr.wrap;

import java.time.Duration;
import java.time.Instant;
import jdk.jfr.consumer.*;

public class GCEvent {
    public String name;
    public Instant startTime;
    public Duration duration;
    public RecordedThread eventThread;
    public int gcId;
    public String cause;
    public Duration sumOfPauses;
    public Duration longestPause;

    public GCEvent(String name, Instant startTime, Duration duration, RecordedThread eventThread, int gcId, String cause, Duration sumOfPauses, Duration longestPause) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.eventThread = eventThread;
        this.gcId = gcId;
        this.cause = cause;
        this.sumOfPauses = sumOfPauses;
        this.longestPause = longestPause;
    }

    public GCEvent() {

    }

    public String toString() {
        return "" + gcId;
    }
}
