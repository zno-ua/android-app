package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author vojkovladimir.
 */
public class TestInfo {
    private static final String ID = "id";
    private static final String LAST_UPDATE = "last_update";
    private static final String SUBJECT_ID = "lesson_id";
    private static final String LEVEL = "level";
    private static final String SESSION = "session_num";
    private static final String TYPE = "session_type";
    private static final String QUESTIONS_COUNT = "task_all";
    private static final String TIME = "time";
    private static final String YEAR = "year";
    
    private int mId;
    private long mLastUpdate;
    private long mSubjectId;
    private int mLevel;
    private int mSession;
    private int mType;
    private int mQuestionsCount;
    private int mTime;
    private int mYear;

    public int getId() {
        return mId;
    }

    @JsonSetter(ID)
    public void setId(int id) {
        mId = id;
    }

    public long getLastUpdate() {
        return mLastUpdate;
    }

    @JsonSetter(LAST_UPDATE)
    public void setLastUpdate(long lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    public long getSubjectId() {
        return mSubjectId;
    }

    @JsonSetter(SUBJECT_ID)
    public void setSubjectId(long subjectId) {
        mSubjectId = subjectId;
    }

    public int getLevel() {
        return mLevel;
    }

    @JsonSetter(LEVEL)
    public void setLevel(int level) {
        mLevel = level;
    }

    public int getSession() {
        return mSession;
    }

    @JsonSetter(SESSION)
    public void setSession(int session) {
        mSession = session;
    }

    public int getType() {
        return mType;
    }

    @JsonSetter(TYPE)
    public void setType(int type) {
        mType = type;
    }

    public int getQuestionsCount() {
        return mQuestionsCount;
    }

    @JsonSetter(QUESTIONS_COUNT)
    public void setQuestionsCount(int questionsCount) {
        mQuestionsCount = questionsCount;
    }

    public int getTime() {
        return mTime;
    }

    @JsonSetter(TIME)
    public void setTime(int time) {
        mTime = time;
    }

    public int getYear() {
        return mYear;
    }

    @JsonSetter(YEAR)
    public void setYear(int year) {
        mYear = year;
    }
}
