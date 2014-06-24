package com.vojkovladimir.zno.models;

public class TestInfo {
	public String name;
	public String dbName;
	public int year;
	public int tasksNum;

	public TestInfo( String dbName,String name, int year, int taskNum) {
		this.name = name;
		this.year = year;
		this.tasksNum = taskNum;
		this.dbName = dbName;
	}
}
