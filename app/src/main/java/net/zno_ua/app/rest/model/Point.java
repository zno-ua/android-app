package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author vojkovladimir.
 */
public class Point {
    @JsonProperty("test_ball")
    public int testPoint;
    @JsonProperty("test_id")
    public long testId;
    @JsonProperty("zno_ball")
    public float ratingPoint;
}
