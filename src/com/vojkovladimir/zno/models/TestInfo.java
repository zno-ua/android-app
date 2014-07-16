package com.vojkovladimir.zno.models;

public class TestInfo {
	public String name;
	public String dbName;
	public int year;
	public int tasksNum;
	public boolean loaded;

	public TestInfo( String dbName,String name, int year, int taskNum,boolean loaded) {
		this.name = name;
		this.year = year;
		this.tasksNum = taskNum;
		this.dbName = dbName;
		this.loaded = loaded;
	}
}
