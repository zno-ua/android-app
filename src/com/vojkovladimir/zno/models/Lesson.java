package com.vojkovladimir.zno.models;

public class Lesson {

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
