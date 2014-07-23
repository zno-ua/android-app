package com.vojkovladimir.zno.models;

public class Lesson {

	public String name;
	public int testsCount;
	public int id;
	
	public Lesson(int id,String name,int testsCount) {
		this.id = id;
		this.name = name;
		this.testsCount = testsCount;
	}

}
