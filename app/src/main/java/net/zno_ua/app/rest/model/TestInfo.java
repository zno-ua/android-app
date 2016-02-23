package net.zno_ua.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author vojkovladimir.
 */
public class TestInfo {
    @JsonProperty("id")
    public int id;
    @JsonProperty("last_update")
    public long lastUpdate;
    @JsonProperty("lesson_id")
    public long subjectId;
    @JsonProperty("level")
    public int level;
    @JsonProperty("session_num")
    public int session;
    @JsonProperty("session_type")
    public int type;
    @JsonProperty("task_all")
    public int questionsCount;
    @JsonProperty("time")
    public int time;
    @JsonProperty("year")
    public int year;
}
