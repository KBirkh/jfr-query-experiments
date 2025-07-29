package me.bechberger.jfr.wrap.nodes;

public enum FunctionType {
    SUM, AVG, COUNT,

    MIN, MAX, MEDIAN,

    P50, P90, P95, P99, P999,

    BEFORE_GC, AFTER_GC, NEAR_GC;
}
