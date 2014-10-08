package com.vojkovladimir.zno.models;

public class Record {

    public String lessonName;
    public int year;
    public int session;
    public long date;
    public long elapsedTime;
    public float ball;

    public Record(String lessonName, int year, int session, long date, long elapsedTime, float ball) {
        this.lessonName = lessonName;
        this.year = year;
        this.session = session;
        this.date = date;
        this.elapsedTime = elapsedTime;
        this.ball = ball;
    }
}
