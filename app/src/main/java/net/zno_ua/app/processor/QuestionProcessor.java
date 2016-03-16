package net.zno_ua.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.activity.ViewImageActivity;
import net.zno_ua.app.provider.Query;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.ServiceGenerator;
import net.zno_ua.app.rest.model.Question;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Question.ADDITIONAL_TEXT;
import static net.zno_ua.app.provider.ZNOContract.Question.ANSWERS;
import static net.zno_ua.app.provider.ZNOContract.Question.CONTENT_URI;
import static net.zno_ua.app.provider.ZNOContract.Question.CORRECT_ANSWER;
import static net.zno_ua.app.provider.ZNOContract.Question.POINT;
import static net.zno_ua.app.provider.ZNOContract.Question.POSITION_ON_TEST;
import static net.zno_ua.app.provider.ZNOContract.Question.TEST_ID;
import static net.zno_ua.app.provider.ZNOContract.Question.TEXT;
import static net.zno_ua.app.provider.ZNOContract.Question.TYPE;
import static net.zno_ua.app.provider.ZNOContract.Question._ID;

import static net.zno_ua.app.provider.ZNOContract.Test.IMAGES_LOADED;
/**
 * @author Vojko Vladimir vojkovladimir@gmail.com
 * @since 16.03.16.
 */
public class QuestionProcessor extends Processor<Question> {
    private static final long NO_ID = Long.MIN_VALUE;

    private static final String HREF = "<a href=\"";
    private static final String HREF_REPLACEMENT = "<a href=\"" + ViewImageActivity.DATA_SCHEMA
            + "://?src=";
    private static final String FORMULAS_PATH = "/formulas";
    private static final String IMAGE = "IMAGE";
    private static final String IMAGE_REGEX = "\\$" + IMAGE + "\\$";
    private static final String IMAGE_SRC_FORMAT = "<img src=\"/%d" + FORMULAS_PATH + "/%s\"/>";
    private static final String HR = "<hr>";
    private static final String BR = "<br>";

    private long mTestId = NO_ID;
    private boolean mDownloadImages = false;
    private boolean mUpdateQuestions = false;
    private boolean mIsImagesDownloadsSuccessfully = false;
    private final APIClient mApiClient;
    private final FileManager mFileManager;

    public QuestionProcessor(@NonNull Context context) {
        super(context);
        mApiClient = ServiceGenerator.create();
        mFileManager = new FileManager(context);
    }

    public void prepare(long testId, boolean updateQuestions, boolean downloadImages) {
        mTestId = testId;
        mUpdateQuestions = updateQuestions;
        mDownloadImages = downloadImages;
    }

    @Override
    public void process(@Nullable List<Question> data) {
        if (mTestId == NO_ID) {
            throw new IllegalArgumentException("Test id didn't specified");
        }
        if (mDownloadImages) mIsImagesDownloadsSuccessfully = true;
        super.process(data);
        if (mDownloadImages && mIsImagesDownloadsSuccessfully) {
            TestProcessor.updateTestResult(getContentResolver(), mTestId, IMAGES_LOADED, true);
        }
        mTestId = NO_ID;
        mDownloadImages = false;
        mIsImagesDownloadsSuccessfully = false;
        mUpdateQuestions = false;
    }

    @Override
    protected void processItem(Question question) {
        if (mDownloadImages) {
            try {
                mIsImagesDownloadsSuccessfully &= downloadQuestionImages(question);
            } catch (IOException e) {
                mIsImagesDownloadsSuccessfully = false;
            }
        }
        prepareQuestion(mTestId, question);
        super.processItem(question);
    }

    private static void prepareQuestion(long testId, @NonNull Question question) {
        final String localPath = "/" + testId;
        if (question.getImagesRelativeUrl() != null) {
            question.setText(question.getText().replace(question.getImagesRelativeUrl(), localPath));
            question.setAnswers(question.getAnswers().replace(question.getImagesRelativeUrl(), localPath));
        }

        String name;
        String src;
        if (question.getImagesFormulasUrl() != null && question.getImagesFormulas() != null) {
            for (int i = 0; i < question.getImagesFormulas().length; i++) {
                name = question.getImagesFormulas()[i];
                src = String.format(Locale.US, IMAGE_SRC_FORMAT, testId, name);
                if (question.getText().contains(IMAGE)) {
                    question.setText(question.getText().replaceFirst(IMAGE_REGEX, src));
                } else if (question.getAnswers().contains(IMAGE)) {
                    question.setAnswers(question.getAnswers().replaceFirst(IMAGE_REGEX, src));
                }
            }
        }
        question.setText(question.getText().replace(HREF, HREF_REPLACEMENT).replace(HR, BR));
        question.setAnswers(cleanAnswers(question.getAnswers()));
    }

    @Override
    protected void insert(@NonNull Question question) {
        if (question.getPoint() == 0) return;
        getContentResolver().insert(Query.Question.URI, createContentValuesForInsert(question));
    }

    @Override
    protected void update(@NonNull Question question, @NonNull Cursor cursor) {
        final String selection = Query.Question.SELECTION_ID;
        final String[] selectionArgs = Query.selectionArgs(question.getId());
        final ContentValues values = createContentValuesForUpdate(question);
        getContentResolver().update(Query.Question.URI, values, selection, selectionArgs);
    }

    @Override
    protected Cursor query(@NonNull Question question) {
        final String[] projection = {_ID};
        final String selection = _ID + " = " + question.getId();
        return getContentResolver().query(CONTENT_URI, projection, selection, null, null);
    }

    @Override
    protected void cleanUp(@Nullable List<Question> data) {

    }

    @Override
    protected boolean shouldUpdate(@NonNull Question question, @NonNull Cursor cursor) {
        return mUpdateQuestions;
    }

    @Override
    public ContentValues createContentValuesForInsert(Question question) {
        final ContentValues values = createContentValuesForUpdate(question);
        values.put(_ID, question.getId());
        return values;
    }

    @Override
    public ContentValues createContentValuesForUpdate(Question question) {
        final ContentValues values = new ContentValues();
        values.put(TEST_ID, mTestId);
        values.put(ANSWERS, question.getAnswers());
        values.put(POINT, question.getPoint());
        values.put(CORRECT_ANSWER, question.getCorrectAnswer());
        values.put(POSITION_ON_TEST, question.getPositionOnTest());
        values.put(TEXT, question.getText());
        values.put(ADDITIONAL_TEXT, question.getAdditionalText());
        values.put(TYPE, question.getType());
        return values;
    }

    private boolean downloadQuestionImages(Question question) throws IOException {
        boolean imagesLoaded = true;
        String localPath = "/" + mTestId;
        if (!TextUtils.isEmpty(question.getImagesRelativeUrl())) {
            imagesLoaded = downloadAndSaveImage(localPath, question.getImagesRelativeUrl(), question.getImages());
        }

        if (!TextUtils.isEmpty(question.getImagesFormulasUrl())) {
            localPath += FORMULAS_PATH;
            imagesLoaded &= downloadAndSaveImage(localPath, question.getImagesRelativeUrl(), question.getImagesFormulas());
        }

        return imagesLoaded;
    }

    private boolean downloadAndSaveImage(String localPath, String relativeUrl, String[] images)
            throws IOException {
        boolean imagesLoaded = true;
        Response<ResponseBody> imageResponse;
        InputStream inputStream;
        for (String name : images) {
            if (!mFileManager.isFileExists(localPath, name)) {
                imageResponse = mApiClient.getImage(relativeUrl, name).execute();
                if (imageResponse.isSuccess()) {
                    inputStream = imageResponse.body().byteStream();
                    imagesLoaded &= mFileManager.saveFile(localPath, name, inputStream);
                }
            }
        }
        return imagesLoaded;
    }

    public void delete(long testId) {
        getContentResolver().delete(CONTENT_URI, TEST_ID + "=?", new String[]{valueOf(testId)});
        mFileManager.deleteTestDirectory(testId);
    }

    @Override
    protected String createSelectionArg(@NonNull Question question) {
        return String.valueOf(question.getId());
    }

    private static String cleanAnswers(String answers) {
        return answers.replaceFirst("^.*?(\\.\\s|\\s)", "")
                .replaceAll("(\r\n|\n).*?(\\.\\s|\\s)", "\r\n");
    }
}
