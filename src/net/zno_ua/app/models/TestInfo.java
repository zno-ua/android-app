package net.zno_ua.app.models;

public class TestInfo {

    public static final String TASK_ALL = "task_all";
    public static final String TEST_ID = "test_id";

    public int id;
    public int lessonId;
    public String name;
    public int taskAll;
    public int time;
    public int year;
    public boolean loaded;

    public TestInfo() { }

    public TestInfo(TestInfo testInfo) {
        id = testInfo.id;
        lessonId = testInfo.lessonId;
        name = testInfo.name;
        taskAll = testInfo.taskAll;
        time = testInfo.time;
        year = testInfo.year;
        loaded = testInfo.loaded;
    }
}
