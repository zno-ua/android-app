package net.zno_ua.app.models;

import android.content.res.Resources;

import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;

public class TestInfo {

    public static final String TASK_ALL = "task_all";
    public static final String TEST_ID = "test_id";

    public int id;
    public int lessonId;
    public String name;
    public String nameShort;
    public String properties;
    public int taskAll;
    public int time;
    public int year;
    public boolean loaded;

    public TestInfo() {
    }

    public TestInfo(TestInfo testInfo) {
        id = testInfo.id;
        lessonId = testInfo.lessonId;
        name = testInfo.name;
        taskAll = testInfo.taskAll;
        time = testInfo.time;
        year = testInfo.year;
        loaded = testInfo.loaded;
    }

    public void makeAdditionalInfo() {
        Resources resources = ZNOApplication.getInstance().getResources();
        String ZNO_FULL = resources.getString(R.string.check_zno_full);
        String ZNO_LIGHT = resources.getString(R.string.check_zno_lite);
        String ZNO = resources.getString(R.string.zno);
        String EXP_ZNO = resources.getString(R.string.exp_zno);
        String FOR_YEAR = resources.getString(R.string.for_);
        String YEAR = resources.getString(R.string.year);
        String SESSION = resources.getString(R.string.session);
        String TASK_TEXT = resources.getString(R.string.task_text);
        String TASKS_TEXT = resources.getString(R.string.tasks_text);
        String NEEDED_TO_LOAD = resources.getString(R.string.needed_to_download);

        if (name.contains(ZNO_FULL)
                || name.contains(ZNO_LIGHT)) {
            nameShort = ZNO;
        } else {
            nameShort = EXP_ZNO + " " + ZNO;
        }

        nameShort += " " + FOR_YEAR + " " + year + " " + YEAR;

        properties = "";
        if (name.contains("(I " + SESSION + ")")) {
            properties = "I " + SESSION + ", ";
        } else if (name.contains("(II " + SESSION + ")")) {
            properties = "II " + SESSION + ", ";
        }

        if (loaded) {
            properties += taskAll + " ";
            switch (taskAll % 10) {
                case 1:
                case 2:
                case 3:
                case 4:
                    properties += TASK_TEXT;
                    break;
                default:
                    properties += TASKS_TEXT;
            }
        } else {
            properties += NEEDED_TO_LOAD;
        }
    }
}
