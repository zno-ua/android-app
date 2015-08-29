package net.zno_ua.app.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.ui.widget.RecyclerLinearLayout;
import net.zno_ua.app.util.UiUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.text.Html.fromHtml;
import static android.view.LayoutInflater.from;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Question;

public class TestingActivity extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_TEST_ID = "net.zno_ua.app.ui.TEST_ID";
    public static final String EXTRA_TIMER_MODE = "net.zno_ua.app.ui.TIMER_MODE";

    private static final String TABLE = "<table";
    private static final String HTML = "text/html";
    private static final String UTF8 = "utf-8";
    private static final String SRC = "src=\"";

    private static final int QUESTIONS_LOADER = 0;

    private static final String SELECTION = Question.TEST_ID + " = ?";

    private static final String[] PROJECTION = new String[]{
            Question._ID,
            Question.POSITION_ON_TEST,
            Question.TYPE,
            Question.TEXT,
            Question.ADDITIONAL_TEXT,
            Question.ANSWERS,
            Question.MARK,
            Question.CORRECT_ANSWER
    };

    private static final int POSITION_ON_TEST_COLUMN_ID = 1;
    private static final int TYPE_COLUMN_ID = 2;
    private static final int TEXT_COLUMN_ID = 3;
    private static final int ADDITIONAL_TEXT_COLUMN_ID = 4;
    private static final int ANSWERS_COLUMN_ID = 5;
    private static final int MARK_COLUMN_ID = 6;
    private static final int CORRECT_ANSWER_COLUMN_ID = 7;

    private long testId;
    private boolean viewMode;
    private boolean timerMode;

    private CoordinatorLayout mCoordinatorLayout;

    private QuestionsAdapter mAdapter;
    private Context mContext = this;

    private String htmlFormat;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        testId = getIntent().getLongExtra(EXTRA_TEST_ID, -1);
        viewMode = false; /* TODO: getAction view / pass / continue_pass */
        timerMode = getIntent().getBooleanExtra(EXTRA_TIMER_MODE, false);
        /* DEBUG ONLY */
        viewMode = timerMode;

        init();
        initWebViewData();
    }

    private void init() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        initToolbar();
        initQuestionsList();
        initFAB();

        getLoaderManager().initLoader(QUESTIONS_LOADER, null, this);
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
    }

    private void initQuestionsList() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.questions_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new QuestionsAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    private void initFAB() {
        findViewById(R.id.fab_done).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_testing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Question.CONTENT_URI,
                PROJECTION,
                SELECTION,
                new String[]{valueOf(testId)},
                Question.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_done:
                finish();
                break;
        }
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
    }

    private class QuestionType2VH extends QuestionBaseVH {

        DiscreteSeekBar markSeekBar;
        TextView chosenMark;

        public QuestionType2VH(View itemView) {
            super(itemView);
            markSeekBar = (DiscreteSeekBar) itemView.findViewById(R.id.mark_seek_bar);
            chosenMark = (TextView) itemView.findViewById(R.id.chosen_mark);
            header.setVisibility(View.GONE);
        }

        public void setChosenMark(int mark) {
            markSeekBar.setProgress(mark);
            setChosenMarkText(mark);
        }

        public void setChosenMarkText(int mark) {
            chosenMark.setText(fromHtml(getString(R.string.chosen_mark) + " <b>" + mark + "</b>"));
        }

        public void setMaxMark(int maxMark) {
            markSeekBar.setMax(maxMark);
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

        @Override
        public int getItemViewType(int position) {
            if (getCursor().moveToPosition(position))
                return getCursor().getInt(TYPE_COLUMN_ID);

            return super.getItemViewType(position);
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
                    onBindQuestionType4_5ViewHolder((QuestionType4_5VH) viewHolder, cursor);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid viewHolderType " + viewType);
            }

            onBindQuestionBaseViewHolder(viewHolder, cursor);
        }

        public void onBindQuestionType1_3ViewHolder(final QuestionType1_3VH viewHolder, Cursor cursor,
                                                    final int viewType) {
            final String additionalText = cursor.getString(ADDITIONAL_TEXT_COLUMN_ID);
            if (TextUtils.isEmpty(additionalText)) {
                viewHolder.readTextButton.setVisibility(View.GONE);
            } else {
                viewHolder.readTextButton.setVisibility(View.VISIBLE);
                viewHolder.readTextButton.setOnClickListener(new View.OnClickListener() {
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

            final String answers = cursor.getString(ANSWERS_COLUMN_ID);
            String correctAnswer = cursor.getString(CORRECT_ANSWER_COLUMN_ID);
            final String oldUserAnswer = ""/* Get answer from DB */;

            AnswersAdapter answersAdapter = (AnswersAdapter) viewHolder.answers.getAdapter();
            answersAdapter.swapData(viewType, answers, correctAnswer, "0" /*user_answer*/);
            answersAdapter.setOnAnswerSelectedListener(new AnswersAdapter.OnAnswerSelectedListener() {
                @Override
                public void onSimpleAnswerSelected(int letter) {
                    saveUserAnswer(getItemId(viewHolder.getAdapterPosition()), "" + (letter + 1));
                }

                @Override
                public void onComplexAnswerSelected(int number, int letter) {
                    StringBuilder currentUserAnswer = new StringBuilder(oldUserAnswer.isEmpty() ?
                            new String(new char[parseInt(answers.split("-")[0])]).replace("\0", "0")
                            : oldUserAnswer
                    );
                    int index = currentUserAnswer.indexOf("" + letter);
                    if (index != -1 || index != number) {
                        currentUserAnswer.setCharAt(number, (char) ('0' + letter));
                    }
                    saveUserAnswer(getItemId(viewHolder.getAdapterPosition()), currentUserAnswer.toString());
                }
            });
        }

        public void onBindQuestionType2ViewHolder(final QuestionType2VH viewHolder, Cursor cursor) {
            viewHolder.markSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int progress,
                                              boolean fromUser) {
                    viewHolder.setChosenMarkText(progress);
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                    saveUserAnswer(getItemId(viewHolder.getAdapterPosition()),
                            "" + discreteSeekBar.getProgress());
                }
            });
            viewHolder.setMaxMark(cursor.getInt(MARK_COLUMN_ID));
//            if (has_user_input)
//                  viewHolder.setChosenMarkText(user_input);
//            else
            viewHolder.setChosenMark(cursor.getInt(MARK_COLUMN_ID) / 2);
        }

        public void onBindQuestionType4_5ViewHolder(final QuestionType4_5VH viewHolder, Cursor cursor) {
//            if (has_user_answer)
//            viewHolder.setUserAnswer(/*user_answer*/);
            viewHolder.answerInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    saveUserAnswer(getItemId(viewHolder.getAdapterPosition()), s.toString());
                }
            });
        }

        public void onBindQuestionBaseViewHolder(QuestionBaseVH viewHolder, Cursor cursor) {
            viewHolder.setNumber(cursor.getInt(POSITION_ON_TEST_COLUMN_ID) + 1, cursor.getCount());
            String text = cursor.getString(TEXT_COLUMN_ID);
            viewHolder.setText(text, text.contains(TABLE) /*TODO: change html detecting*/);
        }

        private void saveUserAnswer(long id, String answer) {
            /*
            * TODO: save user answer to the DB
            * */
//            Toast.makeText(mContext, id + ", " + answer, Toast.LENGTH_SHORT).show();
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

        public ComplexAnswerVH(View itemView) {
            super(itemView);
            number = (TextView) itemView.findViewById(R.id.number);
            lettersSpinner = (Spinner) itemView.findViewById(R.id.letters_spinner);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    private static class AnswersAdapter
            extends RecyclerLinearLayout.Adapter<RecyclerLinearLayout.ViewHolder>
            implements View.OnClickListener {

        private static final int TYPE_SIMPLE = 0;
        private static final int TYPE_COMPLEX = 1;

        private int mAnswersType = -1;
        private int mAnswersCount = 0;
        private String[] mAnswers = {};
        private String mCorrectAnswer;
        private String mAnswer;
        private Context mContext;
        private boolean viewMode;

        private RecyclerLinearLayout mLayout = null;

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
                    viewHolder = new ComplexAnswerVH(view);
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
            int color = Color.BLACK;

            if (viewMode) {
                if (parseInt(mCorrectAnswer) - 1 == position)
                    color = ContextCompat.getColor(mContext, R.color.green_700);
                else if (parseInt(mAnswer) - 1 == position)
                    color = ContextCompat.getColor(mContext, R.color.red_700);
            } else {
                if (position == parseInt(mAnswer))
                    color = ContextCompat.getColor(mContext, R.color.indigo_500);
            }

            viewHolder.setTextColor(color);
        }

        private void onBindComplexAnswerViewHolder(ComplexAnswerVH viewHolder, final int position) {
            viewHolder.number.setText(fromHtml("<b>" + (position + 1) + ". \u2014 </b>"));
            viewHolder.lettersSpinner.setAdapter(
                    new AnswerLettersAdapter(mContext, parseInt(mAnswers[1]))
            );
            viewHolder.lettersSpinner.setSelection(0/*user_answer*/, false);
            viewHolder.lettersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int letter, long id) {
                    mOnAnswerSelectedListener.onComplexAnswerSelected(position, letter + 1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mOnAnswerSelectedListener.onComplexAnswerSelected(position, 0);
                }
            });
        }

        public void swapData(int answersType, String answers, String correctAnswer, String answer) {
            if (answersType == Question.TYPE_1) {
                mAnswersType = TYPE_SIMPLE;
                mAnswers = answers.split("\n");
                mAnswersCount = mAnswers.length;
            } else if (answersType == Question.TYPE_3) {
                mAnswersType = TYPE_COMPLEX;
                mAnswers = answers.split("-");
                mAnswersCount = parseInt(mAnswers[0]);
            }
            mCorrectAnswer = correctAnswer;
            mAnswer = answer;
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
                mOnAnswerSelectedListener
                        .onSimpleAnswerSelected(mLayout.getChildAdapterPosition(v));
            }
        }

        public void setOnAnswerSelectedListener(OnAnswerSelectedListener mAnswerSelectedListener) {
            this.mOnAnswerSelectedListener = mAnswerSelectedListener;
        }

        public interface OnAnswerSelectedListener {
            void onSimpleAnswerSelected(int letter);

            void onComplexAnswerSelected(int number, int letter);
        }
    }

    private static class AnswerLettersAdapter extends BaseAdapter {

        int count;
        private Context mContext;

        public AnswerLettersAdapter(Context context, int count) {
            mContext = context;
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public String getItem(int position) {
            return (char) ('А' + position) + ".";
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
}
