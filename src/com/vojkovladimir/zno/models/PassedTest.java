package com.vojkovladimir.zno.models;

public class PassedTest extends Record {

    public int id;
    public int testId;

    public PassedTest(int id, int testId, String lessonName, int year, int session, long date, long elapsedTime, float ball) {
        super(lessonName, year, session, date, elapsedTime, ball);
        this.id = id;
        this.testId = testId;
    }
}
