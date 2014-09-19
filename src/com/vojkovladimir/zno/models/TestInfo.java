package com.vojkovladimir.zno.models;

public class TestInfo {

    public static final String TASK_ALL = "task_all";
    public static final String TEST_ID = "test_id";

	public int id;
	public int lessonId;
	public String name;
	public int taskAll;
	public int taskMatches;
	public int taskOpenAnswer;
	public int taskTest;
	public int taskVars;
	public int time;
	public int year;
	public boolean loaded;

	public TestInfo(int id, int lessonId, String name, int taskAll,
			int taskMatches, int taskOpenAnswer, int taskTest, int taskVars,
			int time, int year, boolean loaded) {
		this.id = id;
		this.lessonId = lessonId;
		this.name = name;
		this.taskAll = taskAll;
		this.taskMatches = taskMatches;
		this.taskOpenAnswer = taskOpenAnswer;
		this.taskTest = taskTest;
		this.taskVars = taskVars;
		this.time = time;
		this.year = year;
		this.loaded = loaded;
	}

	public TestInfo(TestInfo testInfo) {
		id = testInfo.id;
		lessonId = testInfo.lessonId;
		name = testInfo.name;
		taskAll = testInfo.taskAll;
		taskMatches = testInfo.taskMatches;
		taskOpenAnswer = testInfo.taskOpenAnswer;
		taskTest = testInfo.taskTest;
		taskVars = testInfo.taskVars;
		time = testInfo.time;
		year = testInfo.year;
		loaded = testInfo.loaded;
	}
}
