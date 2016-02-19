package net.zno_ua.app.provider;

import android.net.Uri;

/**
 * @author vojkovladimir.
 */
public class Query {

    public static class Question {
        public static final Uri URI = ZNOContract.Question.CONTENT_URI;

        public static final String[] PROJECTION = {
                ZNOContract.Question._ID,
                ZNOContract.Question.TYPE,
                ZNOContract.Question.TEXT,
                ZNOContract.Question.ADDITIONAL_TEXT,
                ZNOContract.Question.ANSWERS,
                ZNOContract.Question.POINT,
                ZNOContract.Question.CORRECT_ANSWER,
        };

        public interface Column {
            int ID = 0;
            int TYPE = 1;
            int TEXT = 2;
            int ADDITIONAL_TEXT = 3;
            int ANSWERS = 4;
            int POINT = 5;
            int CORRECT_ANSWER = 6;
        }

        public static final String SELECTION = ZNOContract.Question.TEST_ID + " = ?";

        public static final String SORT_ORDER = ZNOContract.Question.SORT_ORDER;
    }

    public static class Answer {
        public static final Uri URI = ZNOContract.Answer.CONTENT_URI;

        public static final String[] PROJECTION = {
                ZNOContract.Answer.ANSWER
        };

        public interface Column {
            int ANSWER = 0;
        }

        public static final String SELECTION = ZNOContract.Answer.TESTING_ID + " = ? AND "
                + ZNOContract.Answer.QUESTION_ID + " = ?";
    }

    public static class QuestionAndAnswer {
        public static final Uri URI = ZNOContract.QuestionAndAnswer.CONTENT_URI;

        public static final String[] PROJECTION = {
                ZNOContract.QuestionAndAnswer._ID,
                ZNOContract.QuestionAndAnswer.POSITION_ON_TEST,
                ZNOContract.QuestionAndAnswer.ANSWER,
                ZNOContract.QuestionAndAnswer.CORRECT_ANSWER,
                ZNOContract.QuestionAndAnswer.TYPE
        };

        public interface Column {
            int _ID = 0;
            int POSITION_ON_TEST = 1;
            int ANSWER = 2;
            int CORRECT_ANSWER = 3;
            int TYPE = 4;
        }

        public static final String SELECTION = ZNOContract.QuestionAndAnswer.TEST_ID + " = ?";

        public static final String SORT_ORDER = ZNOContract.QuestionAndAnswer.SORT_ORDER;
    }

    public static String[] selectionArgs(Object... args) {
        String[] combination = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            combination[i] = String.valueOf(args[i]);
        }
        return combination;
    }
}
