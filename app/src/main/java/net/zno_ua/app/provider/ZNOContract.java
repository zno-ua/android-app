package net.zno_ua.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Vojko Vladimir
 */
public class ZNOContract {

    interface RESTColumns {
        /**
         * Timestamp of the last update date.
         * <P>Type: INTEGER (long)</P>
         */
        String LAST_UPDATE = "last_update";
        /**
         * Flag of the status of the resource state.
         * <P>Type: INTEGER</P>
         */
        String STATUS = "status";
        /**
         * The bit flag, which are the result of the last executed REST method.
         * <P>Type: INTEGER</P>
         */
        String RESULT = "result";
    }

    interface SubjectColumns {
        /**
         * Position of the subject in the list.
         * <P>Type: INTEGER</P>
         */
        String POSITION = "position";
        /**
         * Link to the root directory of additional resources of the subject (images) in the
         * internal storage.
         * <P>Type: TEXT</P>
         */
        String LINK = "link";
        /**
         * Name of the subject.
         * <P>Type: TEXT</P>
         */
        String NAME = "name";
        /**
         * Name of the subject in Genitive form.
         * <P>Type: TEXT</P>
         */
        String NAME_GENITIVE = "name_genitive";
    }

    interface TestColumns {
        /**
         * The unique ID for the subject of the test.
         * <P>Type: INTEGER (long)</P>
         */
        String SUBJECT_ID = "subject_id";
        /**
         * Questions count in the test.
         * <P>Type: INTEGER</P>
         */
        String QUESTIONS_COUNT = "questions_count";
        /**
         * Type of the test (official, experimental).
         * <P>Type: INTEGER</P>
         */
        String TYPE = "type";
        /**
         * Session if the official test or variant of the experimental test.
         * <P>Type: INTEGER</P>
         */
        String SESSION = "session";
        /**
         * The time allotted for passing the test.
         * <P>Type: INTEGER</P>
         */
        String TIME = "time";
        /**
         * The year of the test.
         * <P>Type: INTEGER</P>
         */
        String YEAR = "year";
    }

    interface QuestionColumns {
        /**
         * The ID of the test of the question.
         * <P>Type: INTEGER (long)</P>
         */
        String TEST_ID = "test_id";
        /**
         * The position of the question on the test (ordering).
         * <P>Type: INTEGER</P>
         */
        String POSITION_ON_TEST = "position_on_test";
        /**
         * Type of the question.
         * <P>Type: INTEGER</P>
         */
        String TYPE = "type";
        /**
         * The text of the question.
         * <P>Type: TEXT</P>
         */
        String TEXT = "text";
        /**
         * The additional text of the question e.g. text to read.
         * <P>Type: TEXT</P>
         */
        String ADDITIONAL_TEXT = "additional_text";
        /**
         * The list of answers.
         * <P>Type: TEXT</P>
         */
        String ANSWERS = "answers";
        /**
         * The correct answer.
         * <P>Type: TEXT</P>
         */
        String CORRECT_ANSWER = "correct_answer";
        /**
         * The mark of the question if answer is correct.
         * <P>Type: TEXT</P>
         */
        String MARK = "mark";
        /**
         * The list of images (may be null).
         * <P>Type: TEXT</P>
         */
        String IMAGES = "images";
        /**
         * The relative url of the images (may be null).
         * <P>Type: TEXT</P>
         */
        String IMAGES_RELATIVE_URL = "images_relative_url";
    }

    /*
    /**
     * Authority for the provider of the content of the {@link ZNODataBase}
     */
    public static final String CONTENT_AUTHORITY = "net.zno_ua.app.provider.ZNOProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_SUBJECT = "subject";
    private static final String PATH_TEST = "test";
    private static final String PATH_QUESTION = "question";

    private static final String DOT = ".";
    private static final String VND = "/vnd.";

    public static final String ASC = " ASC";
    public static final String DESC = " DESC";

    public static class Subject implements SubjectColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_SUBJECT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_SUBJECT;

        public static final String[] PROJECTION = {
                _ID,
                POSITION,
                LINK,
                NAME,
                NAME_GENITIVE,
        };

        public interface PROJECTION_ID {
            int _ID = 0;
            int POSITION = 1;
            int LINK = 2;
            int NAME = 3;
            int NAME_GENITIVE = 4;
        }

        public static Uri buildSubjectUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static class Test implements TestColumns, BaseColumns, RESTColumns {
        /**
         * RESULT value that indicates that resources data didn't loaded
         */
        public static final int NO_LOADED_DATA = 0x0;
        /**
         * RESULT bit value that indicates that questions are loaded
         */
        public static final int QUESTIONS_LOADED = 0x1;
        /**
         * RESULT bit value that indicates that marks are loaded
         */
        public static final int MARKS_LOADED = 0x2;
        /**
         * RESULT bit value that indicates that images are loaded
         */
        public static final int IMAGES_LOADED = 0x4;

        public static final int TEST_LOADED = QUESTIONS_LOADED | MARKS_LOADED | IMAGES_LOADED;

        /**
         * STATUS value that indicates that this resource is in idle state.
         * */
        public static final int STATUS_IDLE = 0x0;

        /**
         * Official test type.
         */
        public static final int OFFICIAL = 0x0;
        /**
         * Experimental test type.
         */
        public static final int EXPERIMENTAL = 0x1;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_TEST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_TEST;

        public static final String[] PROJECTION = {
                _ID,
                SUBJECT_ID,
                QUESTIONS_COUNT,
                TYPE,
                SESSION,
                TIME,
                YEAR,
                LAST_UPDATE,
                STATUS,
                RESULT
        };

        public interface PROJECTION_ID {
            int _ID = 0;
            int SUBJECT_ID = 1;
            int QUESTIONS_COUNT = 2;
            int TYPE = 3;
            int SESSION = 4;
            int TIME = 5;
            int YEAR = 6;
            int LAST_UPDATE = 7;
            int STATUS = 8;
            int RESULT = 9;
        }

        public static final String SORT_ORDER = YEAR + DESC;
    }

    public static class Question implements QuestionColumns, BaseColumns {
        /**
         * Question with one correct answer.
         */
        public static final int TYPE_1 = 0x1;
        /**
         * Text to read or own statement question.
         */
        public static final int TYPE_2 = 0x2;
        /**
         * Question with connections.
         */
        public static final int TYPE_3 = 0x3;
        /**
         * Question with three correct answers.
         */
        public static final int TYPE_4 = 0x4;
        /**
         * Question with one short answer.
         */
        public static final int TYPE_5 = 0x5;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUESTION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_QUESTION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + VND + CONTENT_AUTHORITY + DOT + PATH_QUESTION;

        public static final String SORT_ORDER = POSITION_ON_TEST + ASC;
    }
}
