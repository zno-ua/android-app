package net.zno_ua.app.ui;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.text.ImageGetter;
import net.zno_ua.app.ui.widget.RecyclerLinearLayout;
import net.zno_ua.app.util.UiUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Locale;

import static android.content.ContentUris.parseId;
import static android.text.Html.fromHtml;
import static android.text.TextUtils.isEmpty;
import static android.view.LayoutInflater.from;
import static android.widget.AdapterView.OnItemSelectedListener;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Point;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.QuestionAndAnswer;
import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Subject.buildSubjectUri;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;
import static net.zno_ua.app.provider.ZNOContract.Testing;
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;
import static org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;


public class TestingActivity extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public interface Action {
        String VIEW_TEST = "net.zno_ua.app.VIEW_TEST";
        String PASS_TEST = "net.zno_ua.app.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "net.zno_ua.app.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String TEST_ID = "net.zno_ua.app.ui.TEST_ID";
        String SUBJECT_ID = "net.zno_ua.app.ui.SUBJECT_ID";
        String TESTING_ID = "net.zno_ua.app.ui.TESTING_ID";
        String TIMER_MODE = "net.zno_ua.app.ui.TIMER_MODE";
        String VIEW_MODE = "net.zno_ua.app.ui.VIEW_MODE";
        String CURRENT_QUESTION_POSITION = "net.zno_ua.app.ui.CURRENT_QUESTION_POSITION";
    }

    private static final String SRC = "src=\"";
    private static final String SRC_REPLACEMENT = SRC + "file://" + ZNOApplication.getInstance()
            .getFilesDir().getPath();
    private static final String TABLE = "<table";
    private static final String HTML = "text/html";
    private static final String UTF8 = "utf-8";

    private static final int QUESTIONS_LOADER = 0;
    private static final int ANSWERS_LOADER = 1;

    private static final String[] QUESTIONS_PROJECTION = new String[]{
            Question._ID,
            Question.TYPE,
            Question.TEXT,
            Question.ADDITIONAL_TEXT,
            Question.ANSWERS,
            Question.POINT
    };

    private static final String[] ANSWERS_PROJECTION = new String[]{
            QuestionAndAnswer.ANSWER,
            QuestionAndAnswer.CORRECT_ANSWER,
            QuestionAndAnswer.TYPE,
            QuestionAndAnswer.POINT
    };

    private static final int TYPE_COLUMN_ID = 1;
    private static final int TEXT_COLUMN_ID = 2;
    private static final int ADDITIONAL_TEXT_COLUMN_ID = 3;
    private static final int ANSWERS_COLUMN_ID = 4;
    private static final int POINT_COLUMN_ID = 5;

    private static final int ANSWER_COLUMN_ID = 0;
    private static final int CORRECT_ANSWER_COLUMN_ID = 1;
    private static final int QUESTION_TYPE_COLUMN_ID = 2;
    private static final int QUESTION_POINT_COLUMN_ID = 3;

    private int colorRed700;
    private int colorGreen700;

    private long testId;
    private char firstLetter;
    private long subjectId;
    private long testingId;
    private boolean viewMode;
    private long time;
    private volatile long elapsedTime;
    private boolean timerMode;
    private Timer mTimer = null;
    private ImageGetter mImageGetter;

    private RecyclerView mRecyclerView;
    private CoordinatorLayout mCoordinatorLayout;

    private QuestionsAdapter mQuestionsAdapter;
    private Context mContext = this;

    private String htmlFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        Cursor cursor;

        if (savedInstanceState == null) {
            testId = getIntent().getLongExtra(Extra.TEST_ID, -1);
            cursor = getContentResolver()
                    .query(buildTestItemUri(testId), new String[]{Test.SUBJECT_ID}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                subjectId = cursor.getLong(0);
                cursor.close();
            }
            timerMode = getIntent().getBooleanExtra(Extra.TIMER_MODE, false);
            switch (getIntent().getAction()) {
                case Action.PASS_TEST:
                    ContentValues values = new ContentValues();
                    values.put(Testing.TEST_ID, testId);
                    values.put(Testing.STATUS, Testing.IN_PROGRESS);
                    values.put(Testing.ELAPSED_TIME, timerMode ? 0L : -1L);
                    testingId = parseId(getContentResolver().insert(Testing.CONTENT_URI, values));
                    viewMode = false;
                    break;
                case Action.CONTINUE_PASSAGE_TEST:
                    testingId = getIntent().getLongExtra(Extra.TESTING_ID, -1);
                    viewMode = false;
                    break;
                case Action.VIEW_TEST:
                    testingId = getIntent().getLongExtra(Extra.TESTING_ID, -1);
                    viewMode = true;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal action " + getIntent().getAction()
                            + " for " + this.toString());
            }
        } else {
            testId = savedInstanceState.getLong(Extra.TEST_ID);
            subjectId = savedInstanceState.getLong(Extra.SUBJECT_ID);
            testingId = savedInstanceState.getLong(Extra.TESTING_ID);
            timerMode = savedInstanceState.getBoolean(Extra.TIMER_MODE);
            viewMode = savedInstanceState.getBoolean(Extra.VIEW_MODE);
        }

        firstLetter = subjectId == 7 ? 'A' : 'А';

        if (timerMode) {
            cursor = getContentResolver()
                    .query(buildTestItemUri(testId), new String[]{Test.TIME}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst() && cursor.getCount() == 1) {
                    time = 60000L * cursor.getLong(0);
                }
                cursor.close();
            }

            cursor = getContentResolver()
                    .query(buildTestingItemUri(testingId), new String[]{Testing.ELAPSED_TIME}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst() && cursor.getCount() == 1) {
                    elapsedTime = cursor.isNull(0) ? 0 : cursor.getLong(0);
                }
                cursor.close();
            }
        }
        mImageGetter = new ImageGetter(this);

        init();
        if (savedInstanceState != null)
            mRecyclerView.smoothScrollToPosition(
                    savedInstanceState.getInt(Extra.CURRENT_QUESTION_POSITION, 0)
            );
    }

    private void init() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        initToolbar();
        initQuestionsList();
        initFAB();
        initWebViewData();

        colorRed700 = ContextCompat.getColor(mContext, R.color.red_700);
        colorGreen700 = ContextCompat.getColor(mContext, R.color.green_700);

        getLoaderManager().initLoader(ANSWERS_LOADER, null, this);
    }

    private void initWebViewData() {
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float textSizeInPX = getResources().getDimension(R.dimen.abc_text_size_small_material);
        int textSize = (int) (textSizeInPX / scaledDensity);
        htmlFormat = "<html><head>" +
                "<link href=\"file:///android_asset/style/question.css\" rel=\"stylesheet\" type=\"text/css\">" +
                "<style> html, body, table { font-size: " + textSize + "px; } </style>" +
                "</head><body>%s</body></html>";
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(viewMode ? R.string.testing_result : R.string.testing);
        Cursor cursor = getContentResolver().query(
                buildSubjectUri(subjectId), new String[]{Subject.NAME_GENITIVE}, null, null, null
        );
        if (cursor != null) {
            cursor.moveToFirst();
            //noinspection ConstantConditions
            getSupportActionBar().setSubtitle(getString(R.string.of) + " " + cursor.getString(0));
            cursor.close();
        }
    }

    private void initQuestionsList() {
        mRecyclerView = (RecyclerView) findViewById(R.id.questions_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mQuestionsAdapter = new QuestionsAdapter();
        mRecyclerView.setAdapter(mQuestionsAdapter);
    }

    private void initFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (viewMode)
            fab.setVisibility(View.GONE);
        else {
            fab.setOnClickListener(this);
            fab.setImageResource(R.drawable.ic_done_black_24dp);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(Extra.TEST_ID, testId);
        outState.putLong(Extra.SUBJECT_ID, subjectId);
        outState.putLong(Extra.TESTING_ID, testingId);
        outState.putBoolean(Extra.TIMER_MODE, timerMode);
        outState.putBoolean(Extra.VIEW_MODE, viewMode);
        outState.putInt(Extra.CURRENT_QUESTION_POSITION,
                ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (timerMode)
            startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerMode)
            stopTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_testing, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (timerMode) {
            menu.findItem(R.id.action_time).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitTestAlert();
                return true;
            case R.id.action_time:
                if (mTimer != null)
                    mTimer.showRemainingTime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showExitTestAlert();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case QUESTIONS_LOADER:
                return new CursorLoader(this,
                        Question.CONTENT_URI,
                        QUESTIONS_PROJECTION,
                        Question.TEST_ID + " = ?",
                        new String[]{valueOf(testId)},
                        Question.SORT_ORDER);
            case ANSWERS_LOADER:
                return new CursorLoader(this,
                        QuestionAndAnswer.CONTENT_URI,
                        ANSWERS_PROJECTION,
                        QuestionAndAnswer.TEST_ID + " = ?",
                        new String[]{valueOf(testingId), valueOf(testId)},
                        Question.SORT_ORDER);
            default:
                throw new IllegalArgumentException("Illegal Loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case QUESTIONS_LOADER:
                mQuestionsAdapter.swapCursor(data);
                break;
            case ANSWERS_LOADER:
                mQuestionsAdapter.swapAnswersCursor(data);
                if (getLoaderManager().getLoader(QUESTIONS_LOADER) == null)
                    getLoaderManager().initLoader(QUESTIONS_LOADER, null, this);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mQuestionsAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (!viewMode)
                    new MaterialDialog.Builder(this)
                            .title(R.string.finish_test_question)
                            .content(R.string.finish_test_description)
                            .positiveText(R.string.finish)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    finishTest();
                                }
                            })
                            .show();
                break;
        }
    }

    private void showExitTestAlert() {
        if (viewMode)
            finish();
        else
            new MaterialDialog.Builder(this)
                    .title(R.string.exit_test_question)
                    .content(R.string.exit_test_description)
                    .positiveText(R.string.exit)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            exitTest();
                        }
                    })
                    .show();
    }

    private void exitTest() {
        getContentResolver().delete(buildTestingItemUri(testingId), null, null);
        getContentResolver().delete(
                Answer.CONTENT_URI, Answer.TESTING_ID + " = ?", new String[]{valueOf(testingId)});
        finish();
    }

    private void finishTest() {
        if (viewMode)
            mTimer.cancel();
        int testPoint = mQuestionsAdapter.calculateUserPoints();
        double ratingPoint = getRatingPoint(testPoint);

        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        values.put(Testing.DATE, System.currentTimeMillis());
        values.put(Testing.TEST_POINT, testPoint);
        values.put(Testing.RATING_POINT, ratingPoint);
        values.put(Testing.STATUS, Testing.FINISHED);

        getContentResolver().update(buildTestingItemUri(testingId), values, null, null);

        new MaterialDialog.Builder(this)
                .title(R.string.test_completed)
                .content(format(Locale.US, getString(R.string.your_rating_point_format), ratingPoint))
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onAny(MaterialDialog dialog) {
                        showResultsDialog();
                    }
                })
                .cancelable(false)
                .show();
    }

    private void showResultsDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.view_results_question)
                .content(R.string.view_results_description)
                .positiveText(R.string.view)
                .negativeText(R.string.do_not_view)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onAny(MaterialDialog dialog) {
                        finish();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(mContext, TestingActivity.class);
                        intent.setAction(Action.VIEW_TEST);
                        intent.putExtra(Extra.TEST_ID, testId);
                        intent.putExtra(Extra.TESTING_ID, testingId);
                        startActivity(intent);
                    }
                })
                .cancelable(false)
                .show();
    }

    private double getRatingPoint(int testPoint) {
        double point = 0;

        Cursor cursor = getContentResolver()
                .query(Point.CONTENT_URI,
                        new String[]{Point.RATING_POINT},
                        Point.TEST_ID + " =? AND " + Point.TEST_POINT + " =?",
                        new String[]{valueOf(testId), valueOf(testPoint)},
                        Point.SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (testPoint >= cursor.getCount())
                    cursor.moveToLast();
                else
                    cursor.moveToPosition(testPoint);

                point = cursor.getFloat(0);
            }
            cursor.close();
        }

        return point;
    }

    private void startTimer() {
        if (mTimer == null)
            mTimer = new Timer(time - elapsedTime);
        mTimer.start();
        mTimer.showRemainingTime();
    }

    private void stopTimer() {
        if (mTimer != null)
            mTimer.cancel();
        mTimer = null;
        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        getContentResolver()
                .update(buildTestingItemUri(testingId), values, null, null);
    }

    private abstract class QuestionBaseVH extends RecyclerView.ViewHolder {
        View header;
        TextView number1;
        TextView number2;
        TextView text;
        WebView textTagged;
        private final String QUESTION;

        public QuestionBaseVH(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            number1 = (TextView) itemView.findViewById(R.id.number_1);
            number2 = (TextView) itemView.findViewById(R.id.number_2);
            text = (TextView) itemView.findViewById(R.id.text);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            textTagged = (WebView) itemView.findViewById(R.id.text_tagged);
            textTagged.setFocusable(false);
            textTagged.setFocusableInTouchMode(false);
            textTagged.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(ViewImageActivity.DATA_SCHEMA)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }

            });
            QUESTION = getString(subjectId == 7 ? R.string.question_eng : R.string.question);
            if (mImageGetter.maxWidthIsNotSet())
                header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            //noinspection deprecation
                            header.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        mImageGetter.setMaxWidth(header.getWidth());
                    }
                });
        }

        public void setText(String textData, boolean isTagged) {
            if (isTagged) {
                textData = textData.replace(SRC, SRC_REPLACEMENT);
                text.setVisibility(View.GONE);
                textTagged.setVisibility(View.VISIBLE);
                textTagged.loadDataWithBaseURL(null, format(htmlFormat, textData), HTML, UTF8, null);
            } else {
                textTagged.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
                text.setText(fromHtml(textData, mImageGetter, null));
            }
        }

        @SuppressLint("SetTextI18n")
        public void setNumber(int number, int count) {
            number1.setText(QUESTION + " " + number);
            number2.setText(number + "/" + count);
        }
    }

    private class QuestionType1_3VH extends QuestionBaseVH {

        Button readTextButton;
        RecyclerLinearLayout answers;
        View warning;

        public QuestionType1_3VH(View itemView) {
            super(itemView);
            readTextButton = (Button) itemView.findViewById(R.id.read_text_button);
            if (subjectId == 7) readTextButton.setText(R.string.read_text_eng);
            answers = (RecyclerLinearLayout) itemView.findViewById(R.id.answers);
            answers.setAdapter(new AnswersAdapter());
            if (viewMode) {
                warning = itemView.findViewById(R.id.unanswered_question_warning);
            }
        }

        public void setAdditionalText(final String additionalText) {
            if (isEmpty(additionalText)) {
                readTextButton.setVisibility(View.GONE);
            } else {
                readTextButton.setVisibility(View.VISIBLE);
                readTextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(mContext)
                                .title(subjectId == 7 ? R.string.text_for_reading_eng :
                                        R.string.text_for_reading)
                                .content(fromHtml(additionalText, mImageGetter, null))
                                .positiveText(subjectId == 7 ? R.string.close_eng : R.string.close)
                                .show();
                    }
                });
            }
        }

        public void setUnansweredWarning(boolean unAnswered) {
            if (warning != null) {
                warning.setVisibility(unAnswered ? View.VISIBLE : View.GONE);
            }
        }
    }

    private class QuestionType2VH extends QuestionBaseVH {

        DiscreteSeekBar pointSeekBar;
        TextView chosenPoint;
        View choosePointDescription;

        public QuestionType2VH(View itemView) {
            super(itemView);
            pointSeekBar = (DiscreteSeekBar) itemView.findViewById(R.id.point_seek_bar);
            chosenPoint = (TextView) itemView.findViewById(R.id.chosen_point);
            choosePointDescription = itemView.findViewById(R.id.choose_point_description);
            header.setVisibility(View.GONE);
            if (viewMode) {
                pointSeekBar.setVisibility(View.GONE);
                choosePointDescription.setVisibility(View.GONE);
            }
        }

        public void setChosenPoint(int point) {
            pointSeekBar.setProgress(point);
            setChosenPointText(point);
        }

        public void setChosenPointText(int point) {
            chosenPoint.setText(fromHtml(getString(R.string.chosen_point) + " <b>" + point + "</b>"));
        }

        public void setMaxPoint(int maxPoint) {
            pointSeekBar.setMax(maxPoint);
        }

        public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
            pointSeekBar.setOnProgressChangeListener(onProgressChangeListener);
        }
    }

    private class QuestionType4_5VH extends QuestionBaseVH {
        EditText answerInput;
        TextWatcher mTextWatcher;
        TextView userAnswerText;
        TextView correctAnswerText;

        public QuestionType4_5VH(View itemView) {
            super(itemView);
            if (viewMode) {
                userAnswerText = (TextView) itemView.findViewById(R.id.user_answer);
                correctAnswerText = (TextView) itemView.findViewById(R.id.correct_answer);
            } else
                answerInput = (EditText) itemView.findViewById(R.id.answer_input);
        }

        public void setUserAnswer(String answer, TextWatcher textWatcher) {
            if (mTextWatcher != null)
                answerInput.removeTextChangedListener(mTextWatcher);
            answerInput.setText(answer);
            mTextWatcher = textWatcher;
            answerInput.addTextChangedListener(mTextWatcher);
        }

        public void setAnswers(String userAnswer, String correctAnswer) {
            if (isEmpty(userAnswer)) {
                userAnswerText.setTextColor(colorRed700);
                userAnswerText.setText(getString(R.string.unanswered_question_warning));
            } else {
                SpannableString answer;
                Object span;

                if (getItemViewType() == Question.TYPE_4) {
                    userAnswerText.setText(getString(R.string.user_combination));
                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    char[] chars = correctAnswer.toCharArray();
                    char c;
                    int index;
                    for (int i = 0; i < userAnswer.length(); i++) {
                        c = userAnswer.charAt(i);
                        builder.insert(i * 2, c + " ");
                        index = correctAnswer.indexOf(c);

                        if (index == -1 || chars[index] != c) {
                            span = new ForegroundColorSpan(colorRed700);
                        } else {
                            chars[index] = ' ';
                            span = new ForegroundColorSpan(colorGreen700);
                        }

                        builder.setSpan(span, i * 2, i * 2 + 1, Spannable.SPAN_POINT_MARK);
                    }

                    answer = new SpannableString(builder);
                } else {
                    userAnswerText.setText(getString(R.string.user_answer));

                    span = correctAnswer.equals(userAnswer) ? new ForegroundColorSpan(colorGreen700)
                            : new ForegroundColorSpan(colorRed700);
                    answer = new SpannableString(userAnswer);
                    answer.setSpan(span, 0, answer.length(), Spannable.SPAN_POINT_MARK);
                }

                userAnswerText.append(" ");
                userAnswerText.append(answer);
            }

            if (getItemViewType() == Question.TYPE_4) {
                correctAnswerText.setText(getString(R.string.correct_combination));
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < correctAnswer.length(); i++) {
                    stringBuilder.insert(i * 2, correctAnswer.charAt(i) + " ");
                }
                correctAnswer = stringBuilder.toString();
            } else {
                correctAnswerText.setText(getString(R.string.correct_answer));
            }

            SpannableString answer = new SpannableString(correctAnswer);
            answer.setSpan(new ForegroundColorSpan(colorGreen700), 0, answer.length(), Spannable.SPAN_POINT_MARK);

            correctAnswerText.append(" ");
            correctAnswerText.append(answer);
        }

    }

    private class QuestionsAdapter extends CursorRecyclerViewAdapter<QuestionBaseVH>
            implements View.OnFocusChangeListener {

        private Cursor answersCursor = null;
        private final int QUESTIONS_BASE_COUNT = subjectId == 7 || subjectId == 1 ? -1 : 0;

        @Override
        public int getItemViewType(int position) {
            if (getCursor().moveToPosition(position))
                return getCursor().getInt(TYPE_COLUMN_ID);

            return super.getItemViewType(position);
        }

        @Override
        protected void moveCursorToPosition(int position) {
            super.moveCursorToPosition(position);
            moveAnswerCursorToPosition(position);
        }

        private void moveAnswerCursorToPosition(int position) {
            if (answersCursor != null && !answersCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move answers cursor to position " + position);
            }
        }

        private String getAnswer(int position) {
            moveAnswerCursorToPosition(position);
            return answersCursor.isNull(ANSWER_COLUMN_ID) ? null
                    : answersCursor.getString(ANSWER_COLUMN_ID);
        }

        private String getCorrectAnswer(int position) {
            moveAnswerCursorToPosition(position);
            return answersCursor.getString(CORRECT_ANSWER_COLUMN_ID);
        }

        @Override
        public QuestionBaseVH onCreateViewHolder(ViewGroup parent, int viewType) {
            QuestionBaseVH viewHolder;
            View view;

            switch (viewType) {
                case Question.TYPE_1:
                case Question.TYPE_3:
                    view = from(mContext).inflate(viewMode ? R.layout.question_type_1_3_item_vm :
                            R.layout.question_type_1_3_item, parent, false);
                    viewHolder = new QuestionType1_3VH(view);
                    break;
                case Question.TYPE_2:
                    view = from(mContext).inflate(R.layout.question_type_2_item, parent, false);
                    viewHolder = new QuestionType2VH(view);
                    break;
                case Question.TYPE_4:
                    view = from(mContext).inflate(viewMode ? R.layout.question_type_4_5_item_vm :
                            R.layout.question_type_4_item, parent, false);
                    viewHolder = new QuestionType4_5VH(view);
                    break;
                case Question.TYPE_5:
                    view = from(mContext).inflate(viewMode ? R.layout.question_type_4_5_item_vm :
                            R.layout.question_type_5_item, parent, false);
                    viewHolder = new QuestionType4_5VH(view);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid viewType " + viewType);
            }

            view.setOnFocusChangeListener(this);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(QuestionBaseVH viewHolder, Cursor cursor) {
            int viewType = cursor.getInt(TYPE_COLUMN_ID);
            switch (viewType) {
                case Question.TYPE_1:
                case Question.TYPE_3:
                    onBindQuestionType1_3ViewHolder((QuestionType1_3VH) viewHolder, cursor, viewType);
                    break;
                case Question.TYPE_2:
                    onBindQuestionType2ViewHolder((QuestionType2VH) viewHolder, cursor);
                    break;
                case Question.TYPE_4:
                case Question.TYPE_5:
                    onBindQuestionType4_5ViewHolder((QuestionType4_5VH) viewHolder);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid viewHolderType " + viewType);
            }

            onBindQuestionBaseViewHolder(viewHolder, cursor);
        }

        public void onBindQuestionBaseViewHolder(QuestionBaseVH viewHolder, Cursor cursor) {
            viewHolder.setNumber(cursor.getPosition() + 1, QUESTIONS_BASE_COUNT + cursor.getCount());
            String text = cursor.getString(TEXT_COLUMN_ID);
            viewHolder.setText(text, text.contains(TABLE) /*TODO: change html detecting*/);
        }

        public void onBindQuestionType1_3ViewHolder(QuestionType1_3VH viewHolder, Cursor cursor,
                                                    int viewType) {
            final int position = viewHolder.getAdapterPosition();
            viewHolder.setAdditionalText(cursor.getString(ADDITIONAL_TEXT_COLUMN_ID));
            final AnswersAdapter adapter = (AnswersAdapter) viewHolder.answers.getAdapter();
            String answer = getAnswer(position);
            String correctAnswer = getCorrectAnswer(position);
            adapter.swapData(viewType, cursor.getString(ANSWERS_COLUMN_ID), correctAnswer, answer);
            adapter.setOnAnswerSelectedListener(new OnAnswerSelectedListener() {
                @Override
                public void onAnswerSelected(String answer) {
                    saveUserAnswer(getItemId(position), answer);
                }
            });
            if (viewMode) {
                viewHolder.setUnansweredWarning(answer == null);
            }
        }

        public void onBindQuestionType2ViewHolder(final QuestionType2VH viewHolder, Cursor cursor) {
            final int position = viewHolder.getAdapterPosition();
            final int maxPoint = cursor.getInt(POINT_COLUMN_ID);
            final String answer = getAnswer(position);
            if (viewMode) {
                viewHolder.setChosenPoint(answer == null ? maxPoint / 2 : parseInt(answer));
            } else {
                viewHolder.setMaxPoint(maxPoint);
                viewHolder.setChosenPoint(answer == null ? maxPoint / 2 : parseInt(answer));
                viewHolder.setOnProgressChangeListener(new OnProgressChangeListener() {
                    @Override
                    public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int progress,
                                                  boolean fromUser) {
                        viewHolder.setChosenPointText(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                        saveUserAnswer(getItemId(position), valueOf(discreteSeekBar.getProgress()));
                    }
                });
            }
        }

        public void onBindQuestionType4_5ViewHolder(QuestionType4_5VH viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            if (viewMode) {
                viewHolder.setAnswers(getAnswer(position), getCorrectAnswer(position));
            } else {
                viewHolder.setUserAnswer(getAnswer(position), new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        saveUserAnswer(getItemId(position), s.toString());
                    }
                });
                viewHolder.answerInput.clearFocus();
            }
        }

        private Cursor swapAnswersCursor(Cursor cursor) {
            if (cursor == answersCursor)
                return null;
            Cursor oldAnswersCursor = answersCursor;
            answersCursor = cursor;

            return oldAnswersCursor;
        }

        private void saveUserAnswer(long questionId, String answer) {
            ContentValues values = new ContentValues();
            values.put(Answer.QUESTION_ID, questionId);
            values.put(Answer.TESTING_ID, testingId);
            values.put(Answer.ANSWER, answer);
            int rowsUpdated = getContentResolver().update(Answer.CONTENT_URI,
                    values,
                    Answer.QUESTION_ID + " = ?" + " AND " + Answer.TESTING_ID + " = ?",
                    new String[]{valueOf(questionId), valueOf(testingId)}
            );
            if (rowsUpdated == 0)
                getContentResolver().insert(Answer.CONTENT_URI, values);
        }

        private int calculateUserPoints() {
            int points = 0;

            if (answersCursor != null && answersCursor.moveToFirst()) {
                do {
                    points += calculateQuestionPoint(answersCursor.getInt(QUESTION_TYPE_COLUMN_ID),
                            answersCursor.getString(CORRECT_ANSWER_COLUMN_ID),
                            answersCursor.getString(ANSWER_COLUMN_ID));
                } while (answersCursor.moveToNext());
            }

            return points;
        }

        private int calculateQuestionPoint(int type, String correctAnswer, String userAnswer) {
            if (userAnswer == null && type != Question.TYPE_2) {
                return 0;
            }
            int maxPoint = answersCursor.getInt(QUESTION_POINT_COLUMN_ID);
            switch (type) {
                case Question.TYPE_1:
                case Question.TYPE_5:
                    if (correctAnswer.equals(userAnswer))
                        return maxPoint;
                    break;
                case Question.TYPE_2:
                    return userAnswer == null ? maxPoint / 2 : parseInt(userAnswer);
                case Question.TYPE_3: {
                    int point = 0;

                    for (int i = 0; i < correctAnswer.length() && i < userAnswer.length(); i++) {
                        if (correctAnswer.charAt(i) == userAnswer.charAt(i))
                            point += maxPoint;

                    }

                    return point;
                }
                case Question.TYPE_4: {
                    maxPoint = 1;
                    int point = 0;
                    char[] chars = correctAnswer.toCharArray();

                    char c;
                    int index;
                    for (int i = 0; i < userAnswer.length(); i++) {
                        c = userAnswer.charAt(i);
                        index = correctAnswer.indexOf(c);

                        if (index != -1 && chars[index] == c) {
                            chars[index] = ' ';
                            point += maxPoint;
                        }
                    }

                    return point;
                }
            }

            return 0;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus || !(v instanceof EditText)) {
                UiUtils.hideSoftKeyboard(TestingActivity.this);
            }
        }
    }

    private static class SimpleAnswerVH extends RecyclerLinearLayout.ViewHolder {
        TextView letter;
        TextView text;
        View divider;

        public SimpleAnswerVH(View itemView) {
            super(itemView);
            letter = (TextView) itemView.findViewById(R.id.letter);
            text = (TextView) itemView.findViewById(R.id.text);
            divider = itemView.findViewById(R.id.divider);
        }

        public void setTextColor(int color) {
            letter.setTextColor(color);
            text.setTextColor(color);
        }
    }

    private class ComplexAnswerVH extends RecyclerLinearLayout.ViewHolder {
        TextView numberText;
        TextView userAnswerText;
        Spinner lettersSpinner;
        View divider;

        public ComplexAnswerVH(View itemView, SpinnerAdapter adapter) {
            super(itemView);
            numberText = (TextView) itemView.findViewById(R.id.number);
            lettersSpinner = (Spinner) itemView.findViewById(R.id.letters_spinner);
            lettersSpinner.setAdapter(adapter);
            divider = itemView.findViewById(R.id.divider);
        }

        public ComplexAnswerVH(View itemView) {
            super(itemView);
            numberText = (TextView) itemView.findViewById(R.id.number);
            userAnswerText = (TextView) itemView.findViewById(R.id.user_answer);
            divider = itemView.findViewById(R.id.divider);
        }

        @SuppressLint("SetTextI18n")
        public void setAnswers(int number, char correctAnswer, char userAnswer) {
            if (userAnswer == '0') {
                userAnswerText.setText("—");
                userAnswerText.setTextColor(colorRed700);
            } else {
                userAnswerText.setText("" + (char) (firstLetter + (userAnswer - '0') - 1));
                userAnswerText.setTextColor(userAnswer == correctAnswer ? colorGreen700 : colorRed700);
            }

            setCorrectAnswer(number, correctAnswer - '0');
        }

        public void setNumber(int number) {
            setCorrectAnswer(number, -1);
        }

        @SuppressLint("SetTextI18n")
        private void setCorrectAnswer(int number, int answer) {
            numberText.setText((number + 1) + ". — ");
            if (answer != -1) {
                SpannableString spannable = new SpannableString("" + (char) (firstLetter + answer - 1));
                spannable.setSpan(new ForegroundColorSpan(colorGreen700),
                        0, spannable.length(), Spannable.SPAN_POINT_MARK);
                numberText.append(spannable);
            }
        }

        public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
            lettersSpinner.setOnItemSelectedListener(onItemSelectedListener);
        }

        public void setSelection(int position, boolean fromUser) {
            lettersSpinner.setSelection(position, fromUser);
        }
    }

    private class AnswersAdapter
            extends RecyclerLinearLayout.Adapter<RecyclerLinearLayout.ViewHolder>
            implements View.OnClickListener {

        private static final int TYPE_SIMPLE = 0;
        private static final int TYPE_COMPLEX = 1;

        private int mAnswersType = -1;
        private int mAnswersCount = 0;
        private String[] mAnswers = {};
        private String mCorrectAnswer;
        private String mAnswer;
        private RecyclerLinearLayout mLayout = null;

        private OnAnswerSelectedListener mOnAnswerSelectedListener = null;

        @Override
        public int getItemCount() {
            return mAnswersCount;
        }

        @Override
        public int getItemViewType(int position) {
            return mAnswersType;
        }

        @Override
        public RecyclerLinearLayout.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerLinearLayout.ViewHolder viewHolder;
            switch (viewType) {
                case TYPE_SIMPLE:
                    view = from(mContext).inflate(R.layout.simple_answer_item, parent, false);
                    viewHolder = new SimpleAnswerVH(view);
                    break;
                case TYPE_COMPLEX:
                    if (viewMode) {
                        view = from(mContext).inflate(R.layout.complex_answer_item_vm, parent, false);
                        viewHolder = new ComplexAnswerVH(view);
                    } else {
                        view = from(mContext).inflate(R.layout.complex_answer_item, parent, false);
                        viewHolder = new ComplexAnswerVH(view, new AnswerLettersAdapter(mContext));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal viewType " + viewType);
            }

            if (viewMode || viewType == TYPE_COMPLEX) {
                view.setClickable(false);
                view.setBackgroundColor(0);
            } else
                view.setOnClickListener(this);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerLinearLayout.ViewHolder viewHolder, int position) {
            switch (viewHolder.mItemViewType) {
                case TYPE_SIMPLE:
                    onBindSimpleAnswerViewHolder((SimpleAnswerVH) viewHolder, position);
                    break;
                case TYPE_COMPLEX:
                    onBindComplexAnswerViewHolder((ComplexAnswerVH) viewHolder, position);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal viewHolder viewType " + viewHolder.mItemViewType);
            }
        }

        @SuppressLint("SetTextI18n")
        private void onBindSimpleAnswerViewHolder(SimpleAnswerVH viewHolder, int position) {
            viewHolder.letter.setText(Character.toString((char) (firstLetter + position)) + ".");
            viewHolder.text.setText(fromHtml(mAnswers[position], mImageGetter, null));

            if (viewMode) {
                if (parseInt(mCorrectAnswer) - 1 == position)
                    viewHolder.setTextColor(colorGreen700);
                else if (parseInt(mAnswer) - 1 == position)
                    viewHolder.setTextColor(colorRed700);
                else
                    viewHolder.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text_default_material_light));
            }
        }

        private void onBindComplexAnswerViewHolder(ComplexAnswerVH viewHolder, final int position) {
            if (viewMode) {
                viewHolder.setAnswers(position, mCorrectAnswer.charAt(position), mAnswer.charAt(position));
            } else {
                viewHolder.setNumber(position);
                AnswerLettersAdapter adapter =
                        (AnswerLettersAdapter) viewHolder.lettersSpinner.getAdapter();
                adapter.setCount(parseInt(mAnswers[1]));
                viewHolder.setSelection(mAnswer.charAt(position) - '0', false);
                viewHolder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int letter, long id) {
                        selectComplexAnswer(position, letter, true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }

        public void swapData(int answersType, String answers, String correctAnswer, String answer) {
            if (answersType == Question.TYPE_1) {
                mAnswersType = TYPE_SIMPLE;
                mAnswers = answers.split("\n");
                mAnswersCount = mAnswers.length;
                selectSimpleAnswer(answer == null ? -1 : (parseInt(answer) - 1), false);
            } else if (answersType == Question.TYPE_3) {
                mAnswersType = TYPE_COMPLEX;
                mAnswers = answers.split("-");
                mAnswersCount = parseInt(mAnswers[0]);
                mAnswer = answer == null ? buildComplexAnswer(mAnswersCount) : answer;
            }
            mCorrectAnswer = correctAnswer;
            notifyDataSetChanged();
        }

        private String buildComplexAnswer(int answersCount) {
            return new String(new char[answersCount]).replace("\0", "0");
        }

        @Override
        protected void onAttachedToLayout(RecyclerLinearLayout layout) {
            mLayout = layout;
        }

        @Override
        protected void onDetachedFromLayout(RecyclerLinearLayout layout) {
            mLayout = null;
        }

        @Override
        public void onClick(View v) {
            if (mLayout != null && mOnAnswerSelectedListener != null) {
                selectSimpleAnswer(mLayout.getChildAdapterPosition(v), true);
            }
        }

        private void selectSimpleAnswer(int newAnswerPosition, boolean fromUser) {
            int selectedAnswerPosition = mAnswer == null ? -1 : parseInt(mAnswer) - 1;
            RecyclerLinearLayout.ViewHolder viewHolder;
            viewHolder = mLayout.getChildViewHolder(selectedAnswerPosition);
            if (viewHolder != null) {
                viewHolder.mItemView.setSelected(false);
            }

            viewHolder = mLayout.getChildViewHolder(newAnswerPosition);
            if (viewHolder != null)
                viewHolder.mItemView.setSelected(true);

            mAnswer = "" + (newAnswerPosition + 1);
            if (fromUser)
                mOnAnswerSelectedListener.onAnswerSelected(mAnswer);
        }

        private void selectComplexAnswer(int number, int letter, boolean fromUser) {
            StringBuilder currentUserAnswer = new StringBuilder(mAnswer);
            int index = currentUserAnswer.indexOf(valueOf(letter));
            if (index != -1 || index != number) {
                currentUserAnswer.setCharAt(number, (char) ('0' + letter));
                if (index != number && letter != 0) {
                    ComplexAnswerVH viewHolder =
                            (ComplexAnswerVH) mLayout.getChildViewHolder(index);
                    if (viewHolder != null)
                        viewHolder.lettersSpinner.setSelection(0);
                }
                mAnswer = currentUserAnswer.toString();
                if (fromUser)
                    mOnAnswerSelectedListener.onAnswerSelected(mAnswer);

            }
        }

        public void setOnAnswerSelectedListener(OnAnswerSelectedListener onAnswerSelectedListener) {
            mOnAnswerSelectedListener = onAnswerSelectedListener;
        }

    }

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String answer);
    }

    private class AnswerLettersAdapter extends BaseAdapter {

        int count = 0;
        private Context mContext;

        public AnswerLettersAdapter(Context context) {
            mContext = context;
        }

        public void setCount(int count) {
            this.count = count;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return count + 1;
        }

        @Override
        public String getItem(int position) {
            return position == 0 ? "" : (char) (firstLetter + position - 1) + ".";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = from(mContext)
                        .inflate(R.layout.complex_answer_drop_down_letter, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.letter)).setText(getItem(position));

            return convertView;
        }
    }

    private class Timer extends CountDownTimer {

        private static final long COUNT_DOWN_INTERVAL = 1000;

        private long millisLeft;
        private Snackbar mSnackbar;

        public Timer(long millisInFuture) {
            super(millisInFuture, COUNT_DOWN_INTERVAL);
            millisLeft = millisInFuture;
        }

        @Override
        public void onTick(long millisInFuture) {
            millisLeft = millisInFuture;
            elapsedTime = time - millisInFuture;
            int minutes = (int) (millisInFuture / 60000);
            int seconds = (int) (millisInFuture % 60000 / 1000);

            if (mSnackbar != null)
                mSnackbar.setText(getTimerText(minutes, seconds));

            if (minutes != 0 && seconds == 0) {
                if (minutes % 30 == 0 || (minutes < 30 && minutes % 10 == 0)
                        || minutes == 5 || minutes <= 3) {
                    showRemainingTime(false);
                }
            } else if (minutes == 0 && seconds == 15)
                showRemainingTime(false);
        }

        @Override
        public void onFinish() {
            elapsedTime = time;
            finishTest();
        }

        public void showRemainingTime() {
            showRemainingTime(true);
        }

        private void showRemainingTime(boolean hideIfShown) {
            if (mSnackbar == null || !mSnackbar.isShown()) {
                mSnackbar = Snackbar.make(mCoordinatorLayout,
                        getTimerText((int) millisLeft / 60000, (int) millisLeft % 60000 / 1000),
                        Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        mSnackbar = null;
                    }
                });
                mSnackbar.show();
            } else {
                if (hideIfShown)
                    mSnackbar.dismiss();
            }
        }

        private String getTimerText(int minutes, int seconds) {
            if (minutes > 0) {
                if ((minutes < 10) || (minutes > 20 && minutes < 110) || minutes > 120) {
                    switch (minutes % 10) {
                        case 1:
                            return format(getString(R.string.time_one_left),
                                    minutes,
                                    getString(R.string.one_minute)
                            );
                        case 2:
                        case 3:
                        case 4:
                            return format(getString(R.string.time_two_four_left),
                                    minutes,
                                    getString(R.string.two_four_minutes)
                            );
                    }
                }
                return format(getString(R.string.time_left),
                        minutes,
                        getString(R.string.minutes)
                );
            } else {
                if (seconds == 0) {
                    return getString(R.string.time_is_up);
                } else if (seconds >= 20 || (seconds > 0 && seconds < 10)) {
                    switch (seconds % 10) {
                        case 1:
                            return format(getString(R.string.time_one_left),
                                    seconds,
                                    getString(R.string.one_second)
                            );
                        case 2:
                        case 3:
                        case 4:
                            return format(getString(R.string.time_one_left),
                                    seconds,
                                    getString(R.string.two_four_seconds)
                            );
                    }
                }
                return format(getString(R.string.time_one_left),
                        seconds,
                        getString(R.string.seconds)
                );
            }
        }
    }

}
