package net.zno_ua.app.processor;

import android.content.ContentResolver;
import android.content.Context;

/**
 * @author Vojko Vladimir
 */
public abstract class Processor {
    public interface Keys {
        String OBJECTS = "objects";
        String IMAGES = "images";
        String ID = "id";
        String ID_ON_TEST = "id_on_test";
        String LESSON_ID = "lesson_id";
        String LINK = "link";
        String NAME = "name";
        String NAME_ROD = "name_rod";
        String TASK_ALL = "task_all";
        String YEAR = "year";
        String TIME = "time";
        String ANSWERS = "answers";
        String BALLS = "balls";
        String CORRECT_ANSWER = "correct_answer";
        String ID_TEST_QUESTION = "id_test_question";
        String QUESTION = "question";
        String TYPE_QUESTION = "type_question";
        String IMAGES_RELATIVE_URL = "images_relative_url";
        String LAST_UPDATE = "last_update";
        String PARENT_QUESTION = "parent_question";
        String ZNO_BALL = "zno_ball";
        String TEST_BALL = "test_ball";
    }

    private Context mContext;

    public Processor(Context context) {
        mContext = context;
    }

    protected ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }
}
