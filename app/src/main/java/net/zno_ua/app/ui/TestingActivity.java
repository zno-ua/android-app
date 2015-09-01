package net.zno_ua.app.ui;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.ui.widget.RecyclerLinearLayout;
import net.zno_ua.app.util.UiUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.content.ContentUris.parseId;
import static android.text.Html.fromHtml;
import static android.text.TextUtils.isEmpty;
import static android.view.LayoutInflater.from;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.QuestionAndAnswer;
import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Subject.buildSubjectUri;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;
import static net.zno_ua.app.provider.ZNOContract.Testing;
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;

/*
* TODO: add view mode for all types of questions
* */
public class TestingActivity extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public interface Action {
        String VIEW_TEST = "net.zno_ua.app.VIEW_TEST";
        String PASS_TEST = "net.zno_ua.app.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "net.zno_ua.app.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String TEST_ID = "net.zno_ua.app.ui.TEST_ID";
        String TESTING_ID = "net.zno_ua.app.ui.TESTING_ID";
        String TIMER_MODE = "net.zno_ua.app.ui.TIMER_MODE";
        String VIEW_MODE = "net.zno_ua.app.ui.VIEW_MODE";
    }

    private static final String TABLE = "<table";
    private static final String HTML = "text/html";
    private static final String UTF8 = "utf-8";
    private static final String SRC = "src=\"";

    private static final int QUESTIONS_LOADER = 0;
    private static final int ANSWERS_LOADER = 1;

    private static final String[] QUESTIONS_PROJECTION = new String[]{
            Question._ID,
            Question.POSITION_ON_TEST,
            Question.TYPE,
            Question.TEXT,
            Question.ADDITIONAL_TEXT,
            Question.ANSWERS,
            Question.POINT
    };

    private static final String[] ANSWERS_PROJECTION = new String[]{
            QuestionAndAnswer.ANSWER,
            QuestionAndAnswer.CORRECT_ANSWER
    };

    private static final int POSITION_ON_TEST_COLUMN_ID = 1;
    private static final int TYPE_COLUMN_ID = 2;
    private static final int TEXT_COLUMN_ID = 3;
    private static final int ADDITIONAL_TEXT_COLUMN_ID = 4;
    private static final int ANSWERS_COLUMN_ID = 5;
    private static final int POINT_COLUMN_ID = 6;
    private static final int ID_ANSWER_COLUMN_ID = 1;

    private static final int ANSWER_COLUMN_ID = 0;
    private static final int CORRECT_ANSWER_COLUMN_ID = 1;

    private long testId;
    private long testingId;
    private boolean viewMode;
    private long time;
    private volatile long elapsedTime;
    private boolean timerMode;
    private Timer mTimer = null;

    private Snackbar mTimerSnackbar;
    private CoordinatorLayout mCoordinatorLayout;

    private QuestionsAdapter mQuestionsAdapter;
    private Context mContext = this;

    private String htmlFormat;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        if (savedInstanceState == null) {
            testId = getIntent().getLongExtra(Extra.TEST_ID, -1);
            timerMode = getIntent().getBooleanExtra(Extra.TIMER_MODE, false);
            switch (getIntent().getAction()) {
                case Action.PASS_TEST:
                    ContentValues values = new ContentValues();
                    values.put(Testing.TEST_ID, testId);
                    values.put(Testing.STATUS, Testing.IN_PROGRESS);
                    testingId = parseId(getContentResolver().insert(Testing.CONTENT_URI, values));
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
            testingId = savedInstanceState.getLong(Extra.TESTING_ID);
            timerMode = savedInstanceState.getBoolean(Extra.TIMER_MODE);
            viewMode = savedInstanceState.getBoolean(Extra.VIEW_MODE);
        }

        if (!viewMode && timerMode) {
            Cursor cursor = getContentResolver()
                    .query(buildTestItemUri(testId), new String[]{Test.TIME}, null, null, null);
            if (cursor.moveToFirst() && cursor.getCount() == 1) {
                time = 60000L * cursor.getLong(0);
            }
            cursor.close();

            cursor = getContentResolver()
                    .query(buildTestingItemUri(testingId), new String[]{Testing.ELAPSED_TIME}, null, null, null);
            if (cursor.moveToFirst() && cursor.getCount() == 1) {
                elapsedTime = cursor.isNull(0) ? 0 : cursor.getLong(0);
            }
            cursor.close();
        }

        init();
        initWebViewData();
    }

    private void init() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        if (timerMode) {
            mTimerSnackbar = Snackbar.make(mCoordinatorLayout,
                    getTimerText((int) (time / 60000), 0),
                    Snackbar.LENGTH_LONG);
        }
        initToolbar();
        initQuestionsList();
        initFAB();

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
        path = SRC + "file://" + getFilesDir().getPath();
    }

    private void initToolbar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initToolbarSubtitle();
    }

    private void initToolbarSubtitle() {
        Cursor cursor = getContentResolver()
                .query(buildTestItemUri(testId), new String[]{Test.SUBJECT_ID}, null, null, null);
        cursor.moveToFirst();
        long subjectId = cursor.getLong(0);
        cursor.close();

        cursor = getContentResolver()
                .query(buildSubjectUri(subjectId), new String[]{Subject.NAME_GENITIVE}, null, null, null);
        cursor.moveToFirst();
        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(getString(R.string.of) + " " + cursor.getString(0));
        cursor.close();
    }

    private void initQuestionsList() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.questions_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mQuestionsAdapter = new QuestionsAdapter();
        recyclerView.setAdapter(mQuestionsAdapter);
    }

    private void initFAB() {
        findViewById(R.id.fab_done).setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(Extra.TEST_ID, testId);
        outState.putLong(Extra.TESTING_ID, testingId);
        outState.putBoolean(Extra.TIMER_MODE, timerMode);
        outState.putBoolean(Extra.VIEW_MODE, viewMode);

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
        if (timerMode)
            menu.findItem(R.id.action_time).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitTestAlert();
                return true;
            case R.id.action_time:
                mTimerSnackbar.show();
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
                data.moveToFirst();
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
            case R.id.fab_done:
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
        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        getContentResolver().delete(buildTestingItemUri(testingId), null, null);
        getContentResolver()
                .delete(Answer.CONTENT_URI, Testing._ID + " = ?", new String[]{valueOf(testingId)});
        finish();
    }

    private void finishTest() {
        if (viewMode)
            mTimer.cancel();
        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        values.put(Testing.DATE, System.currentTimeMillis());
        values.put(Testing.STATUS, Testing.FINISHED);
        /*
        * TODO: calc test points
        * */
        getContentResolver().update(buildTestingItemUri(testingId), values, null, null);
        finish();
        /*
        * TODO: start viewing results
        * */
    }

    private void startTimer() {
        if (mTimer == null)
            mTimer = new Timer(time - elapsedTime);
        mTimer.start();
        mTimerSnackbar.show();
    }

    private void stopTimer() {
        if (mTimer != null)
            mTimer.cancel();
        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        getContentResolver()
                .update(buildTestingItemUri(testingId), values, null, null);
    }

    private String getTimerText(int minutes, int seconds) {
        String text;

        if (minutes > 0) {
            if ((minutes < 10) || (minutes > 20 && minutes < 110) || minutes > 120) {
                switch (minutes % 10) {
                    case 1:
                        text = format(getString(R.string.time_one_left),
                                minutes,
                                getString(R.string.one_minute)
                        );
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = format(getString(R.string.time_two_four_left),
                                minutes,
                                getString(R.string.two_four_minutes)
                        );
                        break;
                    default:
                        text = format(getString(R.string.time_left),
                                minutes,
                                getString(R.string.minutes)
                        );
                }
            } else {
                text = format(getString(R.string.time_left), minutes, getString(R.string.minutes));
            }
        } else {
            if (seconds == 0) {
                text = getString(R.string.time_is_up);
            } else if (seconds >= 20 || (seconds > 0 && seconds < 10)) {
                switch (seconds % 10) {
                    case 1:
                        text = format(getString(R.string.time_one_left),
                                seconds,
                                getString(R.string.one_second)
                        );
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = format(getString(R.string.time_two_four_left),
                                seconds,
                                getString(R.string.two_four_seconds)
                        );
                        break;
                    default:
                        text = format(getString(R.string.time_left),
                                seconds,
                                getString(R.string.seconds)
                        );
                }
            } else {
                text = format(getString(R.string.time_left), seconds, getString(R.string.seconds));
            }
        }

        return text;
    }

    private abstract class QuestionBaseVH extends RecyclerView.ViewHolder {
        View header;
        TextView number1;
        TextView number2;
        TextView text;
        WebView textTagged;

        public QuestionBaseVH(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            number1 = (TextView) itemView.findViewById(R.id.number_1);
            number2 = (TextView) itemView.findViewById(R.id.number_2);
            text = (TextView) itemView.findViewById(R.id.text);
            textTagged = (WebView) itemView.findViewById(R.id.text_tagged);
//            TODO: add viewing pictures
//            textTagged.setWebViewClient(new WebViewClient() {
//
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                        if (url.contains(ViewImageActivity.DATA_SCHEMA)) {
//                            Intent viewImg = new Intent(Intent.ACTION_VIEW);
//                            viewImg.setData(Uri.parse(url));
//                            getActivity().startActivity(viewImg);
//                            return true;
//                        }
//                    return super.shouldOverrideUrlLoading(view, url);
//                }
//
//            });
        }

        public void setText(String textData, boolean isTagged) {
            if (isTagged) {
                text.setVisibility(View.GONE);
                textTagged.setVisibility(View.VISIBLE);
                textTagged.loadDataWithBaseURL(null,
                        format(htmlFormat, textData.replace(SRC, path)), HTML, UTF8, null);
            } else {
                textTagged.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
                text.setText(fromHtml(textData));
            }
        }

        public void setNumber(int number, int count) {
            number1.setText(getString(R.string.question) + " " + number);
            number2.setText(number + "/" + count);
        }
    }

    private class QuestionType1_3VH extends QuestionBaseVH {

        Button readTextButton;
        RecyclerLinearLayout answers;

        public QuestionType1_3VH(View itemView) {
            super(itemView);
            readTextButton = (Button) itemView.findViewById(R.id.read_text_button);
            answers = (RecyclerLinearLayout) itemView.findViewById(R.id.answers);
            answers.setAdapter(new AnswersAdapter(mContext, viewMode));
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
                                .title(R.string.text_for_reading)
                                .content(fromHtml(additionalText))
                                .positiveText(R.string.close)
                                .show();
                    }
                });
            }
        }
    }

    private class QuestionType2VH extends QuestionBaseVH {

        DiscreteSeekBar pointSeekBar;
        TextView chosenPoint;

        public QuestionType2VH(View itemView) {
            super(itemView);
            pointSeekBar = (DiscreteSeekBar) itemView.findViewById(R.id.point_seek_bar);
            chosenPoint = (TextView) itemView.findViewById(R.id.chosen_point);
            header.setVisibility(View.GONE);
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
    }

    private class QuestionType4_5VH extends QuestionBaseVH {

        EditText answerInput;

        public QuestionType4_5VH(View itemView) {
            super(itemView);
            answerInput = (EditText) itemView.findViewById(R.id.answer_input);
        }

        public void setUserAnswer(String answer) {
            answerInput.setText(answer);
        }
    }

    private class QuestionsAdapter extends CursorRecyclerViewAdapter<QuestionBaseVH>
            implements View.OnFocusChangeListener {

        private Cursor answersCursor = null;

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

        private long getAnswerId(int position) {
            moveAnswerCursorToPosition(position);
            return answersCursor.isNull(ID_ANSWER_COLUMN_ID) ? -1
                    : answersCursor.getLong(ID_ANSWER_COLUMN_ID);
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
                    view = from(mContext).inflate(R.layout.question_type_1_3_item, parent, false);
                    viewHolder = new QuestionType1_3VH(view);
                    break;
                case Question.TYPE_2:
                    view = from(mContext).inflate(R.layout.question_type_2_item, parent, false);
                    viewHolder = new QuestionType2VH(view);
                    break;
                case Question.TYPE_4:
                    view = from(mContext).inflate(R.layout.question_type_4_item, parent, false);
                    viewHolder = new QuestionType4_5VH(view);
                    break;
                case Question.TYPE_5:
                    view = from(mContext).inflate(R.layout.question_type_5_item, parent, false);
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
            viewHolder.setNumber(cursor.getInt(POSITION_ON_TEST_COLUMN_ID) + 1, cursor.getCount());
            String text = cursor.getString(TEXT_COLUMN_ID);
            viewHolder.setText(text, text.contains(TABLE) /*TODO: change html detecting*/);
        }

        public void onBindQuestionType1_3ViewHolder(QuestionType1_3VH viewHolder, Cursor cursor,
                                                    int viewType) {
            final int position = viewHolder.getAdapterPosition();
            final String additionalText = cursor.getString(ADDITIONAL_TEXT_COLUMN_ID);
            viewHolder.setAdditionalText(additionalText);
            final AnswersAdapter answersAdapter = (AnswersAdapter) viewHolder.answers.getAdapter();
            answersAdapter.swapData(viewType,
                    cursor.getString(ANSWERS_COLUMN_ID),
                    getCorrectAnswer(position),
                    getAnswer(position));
            answersAdapter.setOnAnswerSelectedListener(new AnswersAdapter.OnAnswerSelectedListener() {
                @Override
                public void onAnswerSelected(String answer) {
                    saveUserAnswer(getItemId(position), answer);
                }
            });
        }

        public void onBindQuestionType2ViewHolder(final QuestionType2VH viewHolder, Cursor cursor) {
            final int position = viewHolder.getAdapterPosition();
            int maxPoint = cursor.getInt(POINT_COLUMN_ID);
            String answer = getAnswer(position);
            viewHolder.setMaxPoint(maxPoint);
            viewHolder.setChosenPoint(answer == null ? maxPoint / 2 : parseInt(answer));
            viewHolder.pointSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
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

        public void onBindQuestionType4_5ViewHolder(QuestionType4_5VH viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            final String answer = getAnswer(position);
            if (!isEmpty(answer))
                viewHolder.setUserAnswer(answer);
            viewHolder.answerInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveUserAnswer(getItemId(position), s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
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

    private static class ComplexAnswerVH extends RecyclerLinearLayout.ViewHolder {
        TextView number;
        Spinner lettersSpinner;
        View divider;

        public ComplexAnswerVH(View itemView, SpinnerAdapter adapter) {
            super(itemView);
            number = (TextView) itemView.findViewById(R.id.number);
            lettersSpinner = (Spinner) itemView.findViewById(R.id.letters_spinner);
            divider = itemView.findViewById(R.id.divider);

            lettersSpinner.setAdapter(adapter);
        }
    }

    private static class AnswersAdapter
            extends RecyclerLinearLayout.Adapter<RecyclerLinearLayout.ViewHolder>
            implements View.OnClickListener {

        private static final int TYPE_SIMPLE = 0;
        private static final int TYPE_COMPLEX = 1;

        private boolean viewMode;
        private int mAnswersType = -1;
        private int mAnswersCount = 0;
        private String[] mAnswers = {};
        private String mCorrectAnswer;
        private String mAnswer;
        private RecyclerLinearLayout mLayout = null;
        private Context mContext;

        private OnAnswerSelectedListener mOnAnswerSelectedListener = null;

        public AnswersAdapter(Context context, boolean viewMode) {
            mContext = context;
            this.viewMode = viewMode;
        }

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
                    view = from(mContext).inflate(R.layout.complex_answer_item, parent, false);
                    viewHolder = new ComplexAnswerVH(view, new AnswerLettersAdapter(mContext));
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

        private void onBindSimpleAnswerViewHolder(SimpleAnswerVH viewHolder, int position) {
            viewHolder.letter.setText(Character.toString((char) ('А' + position)) + ".");
            viewHolder.text.setText(fromHtml(mAnswers[position]));
            /*int color = ContextCompat
                    .getColor(mContext, R.color.primary_text_default_material_light)*/
            ;

            if (viewMode) {
                if (parseInt(mCorrectAnswer) - 1 == position)
                    viewHolder.setTextColor(ContextCompat.getColor(mContext, R.color.green_700));
                else {
                    if (parseInt(mAnswer) - 1 == position)
                        viewHolder.setTextColor(ContextCompat.getColor(mContext, R.color.red_700));
                }
            }
        }

        private void onBindComplexAnswerViewHolder(ComplexAnswerVH viewHolder, final int position) {
            viewHolder.number.setText(fromHtml("<b>" + (position + 1) + ". \u2014 </b>"));
            AnswerLettersAdapter adapter =
                    (AnswerLettersAdapter) viewHolder.lettersSpinner.getAdapter();
            adapter.setCount(parseInt(mAnswers[1]));
            viewHolder.lettersSpinner.setSelection('0' - mAnswer.charAt(position), false);
            viewHolder.lettersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int letter, long id) {
                    selectComplexAnswer(position, letter, true);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
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
                mAnswer = answer == null ?
                        new String(new char[mAnswersCount]).replace("\0", "0") : answer;
            }
            mCorrectAnswer = correctAnswer;
            notifyDataSetChanged();
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

        public interface OnAnswerSelectedListener {
            void onAnswerSelected(String answer);
        }
    }

    private static class AnswerLettersAdapter extends BaseAdapter {

        int count;
        private Context mContext;

        public AnswerLettersAdapter(Context context) {
            mContext = context;
            count = 0;
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
            return position == 0 ? "" : (char) ('А' + position - 1) + ".";
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

        public Timer(long millisInFuture) {
            super(millisInFuture, COUNT_DOWN_INTERVAL);
        }

        @Override
        public void onTick(long millisInFuture) {
            elapsedTime = time - millisInFuture;
            int minutes = (int) (millisInFuture / 60000);
            int seconds = (int) (millisInFuture % 60000 / 1000);

            if (minutes == 0) {
                mTimerSnackbar.setText(getTimerText(minutes, seconds));
            }

            if (minutes != 0 && seconds == 0) {
                mTimerSnackbar.setText(getTimerText(minutes, seconds));
                if (!mTimerSnackbar.isShown()) {
                    if (minutes % 30 == 0 || (minutes < 30 && minutes % 10 == 0)
                            || minutes == 5 || minutes <= 3) {
                        mTimerSnackbar.show();
                    }
                }
            }

//            if (minutes <= 10) {
//                timerText.setBackgroundColor(getResources().getColor(R.color.red));
//            }
        }

        @Override
        public void onFinish() {
            elapsedTime = time;
            finishTest();
        }
    }
}
