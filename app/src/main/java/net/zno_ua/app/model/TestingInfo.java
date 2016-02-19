package net.zno_ua.app.model;

import static net.zno_ua.app.provider.ZNOContract.Testing.NO_TIME;

/**
 * @author vojkovladimir.
 */
public class TestingInfo {
    private long mTestingId;
    private long mTestId;
    private long mSubjectId;
    private String mSubjectName;
    private String mSubjectNameGenitive;
    private String mLink;
    private long mTime = NO_TIME;
    private volatile long mElapsedTime;
    private boolean mIsPassed = false;

    public String getSubjectName() {
        return mSubjectName;
    }

    public void setSubjectName(String name) {
        mSubjectName = name;
    }

    public String getSubjectNameGenitive() {
        return mSubjectNameGenitive;
    }

    public void setSubjectNameGenitive(String name) {
        mSubjectNameGenitive = name;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public long getTestingId() {
        return mTestingId;
    }

    public void setTestingId(long testingId) {
        mTestingId = testingId;
    }

    public long getTestId() {
        return mTestId;
    }

    public void setTestId(long testId) {
        mTestId = testId;
    }

    public long getSubjectId() {
        return mSubjectId;
    }

    public void setSubjectId(long subjectId) {
        mSubjectId = subjectId;
    }

    public boolean isPassed() {
        return mIsPassed;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public boolean withTimer() {
        return mTime != NO_TIME;
    }

    public long getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        mElapsedTime = elapsedTime;
    }

    public void setIsPassed(boolean isPassed) {
        mIsPassed = isPassed;
    }
}
