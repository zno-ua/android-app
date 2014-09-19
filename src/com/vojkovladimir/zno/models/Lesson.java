package com.vojkovladimir.zno.models;

public class Lesson {

    public static final String LESSON_ID = "lesson_id";
    public static final String LESSON_NAME = "lesson_name";

	public String name;
	public String link;
	public int testsCount;
	public int id;
	
	public Lesson(int id,String name,String link,int testsCount) {
		this.id = id;
		this.name = name;
		this.link = link;
		this.testsCount = testsCount;
	}

}
