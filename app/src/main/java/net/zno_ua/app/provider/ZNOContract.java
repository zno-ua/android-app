package net.zno_ua.app.provider;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;

import static net.zno_ua.app.provider.ZNODatabase.Tables;

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
         * Number of the session if the official test or variant of the experimental test.
         * <P>Type: INTEGER</P>
         */
        String SESSION = "session";
        /**
         * Level of the test session.
         * <P>Type: INTEGER</P>
         */
        String LEVEL = "level";
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
         * The point of the question if answer is correct.
         * <P>Type: TEXT</P>
         */
        String POINT = "point";
        /**
         * The list of images (may be null).
         * <P>Type: TEXT</P>
         */
    }

    public interface QuestionAndAnswerColumns extends QuestionColumns, AnswerColumns {
        String _ID = Tables.QUESTION + DOT + BaseColumns._ID + AS + BaseColumns._ID;
        String _ID_ANSWER = Tables.ANSWER + DOT + BaseColumns._ID + AS + Tables.ANSWER + BaseColumns._ID;
    }

    public interface TestingColumns {
        /**
         * The ID of the test of the question.
         * <P>Type: INTEGER (long)</P>
         */
        String TEST_ID = "test_id";
        /**
         * The date of the passing of the test.
         * <P>Type: INTEGER (long)</P>
         */
        String DATE = "date";
        /**
         * Time elapsed for passing the test.
         * <P>Type: INTEGER (long)</P>
         */
        String ELAPSED_TIME = "elapsed_time";
        /**
         * The point of the testing.
         * <P>Type: INTEGER (long)</P>
         */
        String TEST_POINT = "test_point";
        /**
         * The rating point of the test.
         * <P>Type: INTEGER (long)</P>
         */
        String RATING_POINT = "rating_point";
        /**
         * The status of the testing.
         * <P>Type: INTEGER (long)</P>
         */
        String STATUS = "status";
    }

    public interface AnswerColumns {
        /**
         * The ID of the question of the answer.
         * <P>Type: INTEGER (long)</P>
         */
        String QUESTION_ID = "question_id";
        /**
         * The ID of the testing of the answer.
         * <P>Type: INTEGER (long)</P>
         */
        String TESTING_ID = "testing_id";
        /**
         * Answer of the user.
         * <P>Type: TEXT</P>
         */
        String ANSWER = "answer";
    }

    public interface PointColumns {
        /**
         * The ID of the test of the question.
         * <P>Type: INTEGER (long)</P>
         */
        String TEST_ID = "test_id";
        /**
         * The point of the test.
         * <P>Type: INTEGER (long)</P>
         */
        String TEST_POINT = "test_point";
        /**
         * The rating point of the test.
         * <P>Type: INTEGER (long)</P>
         */
        String RATING_POINT = "rating_point";
    }

    public interface TestUpdateColumns {
        String _ID = BaseColumns._ID;
        String TEST_ID = "test_id";
    }

    public interface TestingResultColumns {
        String _ID = Tables.TESTING + DOT + BaseColumns._ID + AS + BaseColumns._ID;
        String TEST_ID = Tables.TEST + DOT + BaseColumns._ID;
        String SUBJECT_ID = Tables.TEST + DOT + TestColumns.SUBJECT_ID;
        String TEST_STATUS = Tables.TEST + DOT + RESTColumns.STATUS;
        String TEST_RESULT = Tables.TEST + DOT + RESTColumns.RESULT;
        String TEST_TYPE = Tables.TEST + DOT + TestColumns.TYPE;
        String TEST_SESSION = Tables.TEST + DOT + TestColumns.SESSION;
        String TEST_LEVEL = Tables.TEST + DOT + TestColumns.LEVEL;
        String TEST_YEAR = Tables.TEST + DOT + TestColumns.YEAR;
        String SUBJECT_NAME = Tables.SUBJECT + DOT + SubjectColumns.NAME;
        String DATE = Tables.TESTING + DOT + TestingColumns.DATE;
        String ELAPSED_TIME = Tables.TESTING + DOT + TestingColumns.ELAPSED_TIME;
        String TEST_POINT = Tables.TESTING + DOT + TestingColumns.TEST_POINT;
        String RATING_POINT = Tables.TESTING + DOT + TestingColumns.RATING_POINT;
        String STATUS = Tables.TESTING + DOT + TestingColumns.STATUS;
    }

    /*
    /**
     * Authority for the provider of the content of the {@link ZNODataBase}
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".ZNOProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_SUBJECT = Tables.SUBJECT;
    static final String PATH_TEST = Tables.TEST;
    static final String PATH_QUESTION = Tables.QUESTION;
    static final String PATH_QUESTION_AND_ANSWER = "question_and_answer";
    static final String PATH_ANSWER = Tables.ANSWER;
    static final String PATH_TESTING = Tables.TESTING;
    static final String PATH_POINT = Tables.POINT;
    static final String PATH_TESTING_RESULT = "testing_result";
    static final String PATH_TEST_UPDATE = Tables.TEST_UPDATE;

    private static final String DOT = ".";
    public static final String AS = " AS ";
    public static final String ASC = " ASC";
    public static final String DESC = " DESC";

    public static class Subject implements SubjectColumns, BaseColumns {
        public static final long ENGLISH = 7;
        public static final long UKRAINIAN = 1;
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();

        public static Uri buildSubjectUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }

    public static class Test implements TestColumns, BaseColumns, RESTColumns {
        /**
         * RESULT value that indicates that resources data didn't loaded.
         */
        public static final int NO_LOADED_DATA = 0x0;
        /**
         * RESULT bit value that indicates that questions are loaded.
         */
        public static final int QUESTIONS_LOADED = 0x1;
        /**
         * RESULT bit value that indicates that points are loaded.
         */
        public static final int POINTS_LOADED = 0x2;
        /**
         * RESULT bit value that indicates that images are loaded.
         */
        public static final int IMAGES_LOADED = 0x4;
        /**
         * RESULT value that indicates that resources data were loaded.
         */
        public static final int TEST_LOADED = QUESTIONS_LOADED | POINTS_LOADED | IMAGES_LOADED;

        /**
         * STATUS value that indicates that this resource is in idle state.
         */
        public static final int STATUS_IDLE = 0x0;

        /**
         * STATUS value that indicates that processor downloading this resource.
         */
        public static final int STATUS_DOWNLOADING = 0x1;
        /**
         * STATUS value that indicates that processor deleting this resource.
         */
        public static final int STATUS_DELETING = 0x2;
        /**
         * STATUS value that indicates that processor updating this resource.
         */
        public static final int STATUS_UPDATING = 0x3;

        /**
         * Official test type.
         */
        public static final int TYPE_OFFICIAL = 0x0;
        /**
         * Experimental test type.
         */
        public static final int TYPE_EXPERIMENTAL = 0x1;

        public static final int LEVEL_BASIC = 0x1;
        public static final int LEVEL_SPECIALIZED = 0x2;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEST).build();

        public static Uri buildTestItemUri(long id) {
            return buildItemUri(CONTENT_URI, id);
        }

        public static boolean testResult(int result, int mask) {
            return (result & mask) == mask;
        }

        public static final String SORT_ORDER = YEAR + DESC + "," + TYPE + ASC
                + "," + SESSION + ASC + "," + LEVEL + ASC;

        @NonNull
        public static String getTestType(@NonNull Context context, int type) {
            if (type == ZNOContract.Test.TYPE_OFFICIAL) {
                return context.getString(R.string.official_test);
            } else {
                return context.getString(R.string.experimental_test);
            }
        }

        @NonNull
        public static String getTestSession(@NonNull Context context, int type, int session) {
            if (session != 0) {
                if (type == TYPE_OFFICIAL) {
                    return (session == 1 ? "I " : "II ") + context.getString(R.string.session);
                }
                if (type == TYPE_EXPERIMENTAL) {
                    return session + " " + context.getString(R.string.variant);
                }
            }
            return "";
        }

        @NonNull
        public static String getTestLevel(@NonNull Context context, int level) {
            if (level == LEVEL_BASIC) {
                return context.getString(R.string.level_basic);
            } else if (level == LEVEL_SPECIALIZED) {
                return context.getString(R.string.level_specialized);
            }
            return "";
        }
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

        public static final String SORT_ORDER = POSITION_ON_TEST + ASC;
    }

    public static class QuestionAndAnswer implements QuestionAndAnswerColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUESTION_AND_ANSWER).build();

        public static final String SORT_ORDER = POSITION_ON_TEST + ASC;
    }

    public static class Answer implements BaseColumns, AnswerColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ANSWER).build();

    }

    public static class Testing implements BaseColumns, TestingColumns {
        /**
         * Status indicates that testing was finished.
         */
        public static final int FINISHED = 0x2;
        /**
         * Status indicates that testing is in progress.
         */
        public static final int IN_PROGRESS = 0x1;
        /**
         * Elapsed time for testing without timer.
         */
        public static final long NO_TIME = -1;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TESTING).build();

        public static final String[] PROJECTION = {
                _ID,
                TEST_ID,
                ELAPSED_TIME
        };

        public interface COLUMN_ID {
            int ID = 0;
            int TEST_ID = 1;
            int ELAPSED_TIME = 2;
        }

        public static Uri buildTestingItemUri(long id) {
            return buildItemUri(CONTENT_URI, id);
        }

        public static final String SORT_ORDER = DATE + DESC;
    }

    public static class Point implements BaseColumns, PointColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POINT).build();

        public static final String SORT_ORDER = TEST_POINT + ASC;
    }

    public static class TestingResult implements TestingResultColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TESTING_RESULT).build();

        public static final int RESULT_VERY_DISSATISFIED = 0x0;
        public static final int RESULT_DISSATISFIED = 0x1;
        public static final int RESULT_NEUTRAL = 0x2;
        public static final int RESULT_SATISFIED = 0x3;
        public static final int RESULT_VERY_SATISFIED = 0x4;

        public static int getSentimentOfRatingPoint(float ratingPoint) {
            if (ratingPoint >= 180.0f) {
                return RESULT_VERY_SATISFIED;
            } else if (ratingPoint >= 160.f) {
                return RESULT_SATISFIED;
            } else if (ratingPoint >= 140.0f) {
                return RESULT_NEUTRAL;
            } else if (ratingPoint >= 130.f) {
                return RESULT_DISSATISFIED;
            }

            return RESULT_VERY_DISSATISFIED;
        }

        @DrawableRes
        private static final int[] SENTIMENT_DRAWABLE_RESOURCES = {
                R.drawable.vec_sentiment_very_dissatisfied_white_18px,
                R.drawable.vec_sentiment_dissatisfied_white_18px,
                R.drawable.vec_sentiment_neutral_white_18px,
                R.drawable.vec_sentiment_satisfied_white_18px,
                R.drawable.vec_sentiment_very_satisfied_white_18px
        };

        @ColorRes
        private static final int[] SENTIMENT_COLOR_RESOURCES = {
                R.color.red_500,
                R.color.orange_500,
                R.color.amber_500,
                R.color.yellow_500,
                R.color.green_500,
        };

        @DrawableRes
        public static int getSentimentResourceId(int sentimentResult) {
            return SENTIMENT_DRAWABLE_RESOURCES[correctSentimentResult(sentimentResult)];
        }

        @ColorRes
        public static int getSentimentColorRes(int sentimentResult) {
            return SENTIMENT_COLOR_RESOURCES[sentimentResult];
        }

        @ColorInt
        public static int getSentimentColor(@NonNull Context context, int sentimentReslut) {
            return ContextCompat.getColor(context, getSentimentColorRes(sentimentReslut));
        }

        private static int correctSentimentResult(int sentimentResult) {
            if (sentimentResult < RESULT_VERY_DISSATISFIED) {
                sentimentResult = RESULT_VERY_DISSATISFIED;
            } else if (sentimentResult > RESULT_VERY_SATISFIED) {
                sentimentResult = RESULT_VERY_SATISFIED;
            }

            return sentimentResult;
        }

    }

    public static class TestUpdate implements TestUpdateColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEST_UPDATE).build();
    }

    private static Uri buildItemUri(Uri contentUri, Object item) {
        return contentUri.buildUpon().appendPath(String.valueOf(item)).build();
    }
}
