package com.vojkovladimir.zno.models;

import java.util.ArrayList;

public class Test extends TestInfo{

	public ArrayList<Question> questions;
	
	public Test(String dbName,String name,String lessonName, int year, int taskNum,boolean loaded, ArrayList<Question> questions) {
		super(dbName, name, lessonName, year, taskNum, loaded);
		this.questions = questions;
	}

}
