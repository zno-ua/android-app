package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author vojkovladimir.
 */
public class Point {
    private static final String TEST_POINT = "test_ball";
    private static final String TEST_ID = "test_id";
    private static final String RATING_POINT = "zno_ball";
    
    private int mTestPoint;
    private long mTestId;
    private float mRatingPoint;

    public int getTestPoint() {
        return mTestPoint;
    }

    @JsonSetter(TEST_POINT)
    public void setTestPoint(int testPoint) {
        mTestPoint = testPoint;
    }

    public long getTestId() {
        return mTestId;
    }

    @JsonSetter(TEST_ID)
    public void setTestId(long testId) {
        mTestId = testId;
    }

    public float getRatingPoint() {
        return mRatingPoint;
    }

    @JsonSetter(RATING_POINT)
    public void setRatingPoint(float ratingPoint) {
        mRatingPoint = ratingPoint;
    }
}
