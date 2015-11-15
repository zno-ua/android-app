package net.zno_ua.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.text.ImageGetter;
import net.zno_ua.app.ui.ViewImageActivity;
import net.zno_ua.app.ui.widget.RecyclerLinearLayout;
import net.zno_ua.app.util.UiUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static android.text.Html.fromHtml;
import static android.text.TextUtils.isEmpty;
import static android.view.LayoutInflater.from;
import static android.view.View.OnClickListener;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.zno_ua.app.adapter.QuestionsAdapter.OnAnswerChangeListener;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.QuestionAndAnswer;

/**
 * @author vojkovladimir.
 */
abstract class QuestionBaseVH extends RecyclerView.ViewHolder implements ViewStub.OnInflateListener {

    final int GREEN_700;
    final int RED_700;
    final int DEFAULT;

    View header;
    TextView number;
    TextView sequenceNumber;
    TextView text;
    ViewStub textTaggedStub;
    WebView textTagged;

    OnAnswerChangeListener onAnswerChangeListener;


    public QuestionBaseVH(View itemView, boolean hasHeader) {
        super(itemView);
        Context context = itemView.getContext();
        GREEN_700 = ContextCompat.getColor(context, R.color.green_700);
        RED_700 = ContextCompat.getColor(context, R.color.red_700);
        DEFAULT = ContextCompat.getColor(context, R.color.primary_text_default_material_light);
        if (hasHeader) {
            header = ((ViewStub) itemView.findViewById(R.id.header_view_stub)).inflate();
            number = (TextView) itemView.findViewById(R.id.number);
            sequenceNumber = (TextView) itemView.findViewById(R.id.sequence_number);
        }
        text = (TextView) itemView.findViewById(R.id.text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        textTaggedStub = (ViewStub) itemView.findViewById(R.id.text_tagged_view_stub);
        textTaggedStub.setOnInflateListener(this);
        textTagged = null;
    }

    public boolean isViewMode() {
        return onAnswerChangeListener == null;
    }

    public void setOnAnswerChangeListener(OnAnswerChangeListener onAnswerChangeListener) {
        this.onAnswerChangeListener = onAnswerChangeListener;
    }

    public void onAnswerChanged(String answer) {
        if (!isViewMode())
            onAnswerChangeListener.onAnswerChanged(getItemId(), answer);
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        textTagged = (WebView) inflated;
        textTagged.setFocusable(false);
        textTagged.setFocusableInTouchMode(false);
        textTagged.setWebViewClient(WEB_VIEW_CLIENT);
    }

    static final WebViewClient WEB_VIEW_CLIENT = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(ViewImageActivity.DATA_SCHEMA)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };

    public boolean hasHeader() {
        return header != null;
    }
}

class QuestionChoicesVH extends QuestionBaseVH {

    private static final int NO_NOTIFY_POSITION = -1;
    private static final String ANSWERS_COUNT_DIVIDER_REGEX = "-";
    private static final java.lang.String ANSWERS_DIVIDER_REGEX = "\n";

    Button additionalText;
    RecyclerLinearLayout answers;
    AnswersAdapter adapter;

    ViewStub warning;

    public QuestionChoicesVH(View itemView, char firstLetter, String readText,
                             OnClickListener onClickListener) {
        super(itemView, true);
        ViewStub stub = (ViewStub) itemView.findViewById(R.id.additional_text_view_stub);
        additionalText = (Button) stub.inflate();
        additionalText.setOnClickListener(onClickListener);
        additionalText.setText(readText);
        stub = (ViewStub) itemView.findViewById(R.id.answers_view_stub);
        stub.setLayoutResource(isViewMode() ? R.layout.answers_view_vm : R.layout.answers_view);
        View inflated = stub.inflate();
        answers = (RecyclerLinearLayout) inflated.findViewById(R.id.answers);
        adapter = new AnswersAdapter(firstLetter);
        answers.setAdapter(adapter);
        if (isViewMode()) {
            warning = (ViewStub) inflated.findViewById(R.id.unanswered_warning_stub);
        }

    }

    static class OneAnswerVH extends RecyclerLinearLayout.ViewHolder {

        TextView letter;
        TextView text;

        public OneAnswerVH(View itemView) {
            super(itemView);
            letter = (TextView) itemView.findViewById(R.id.letter);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        void setTextColor(int color) {
            letter.setTextColor(color);
            text.setTextColor(color);
        }

    }

    class MultipleAnswersVH extends RecyclerLinearLayout.ViewHolder {

        TextView number;
        Spinner letters;
        TextView userAnswer;

        public MultipleAnswersVH(View iteView) {
            this(iteView, null, null);
        }

        public MultipleAnswersVH(View itemView, AnswersAdapter.LettersAdapter lettersAdapter,
                                 AdapterView.OnItemSelectedListener onItemSelectedListener) {
            super(itemView);
            number = (TextView) itemView.findViewById(R.id.number);
            if (lettersAdapter == null) {
                userAnswer = (TextView) itemView.findViewById(R.id.user_answer);
            } else {
                letters = (Spinner) itemView.findViewById(R.id.letters);
                letters.setAdapter(lettersAdapter);
                letters.setOnItemSelectedListener(onItemSelectedListener);
            }
        }

    }

    class AnswersAdapter extends RecyclerLinearLayout.Adapter<RecyclerLinearLayout.ViewHolder>
            implements OnClickListener, AdapterView.OnItemSelectedListener {

        static final String LETTER_FORMAT = "%c.";
        static final String NUMBER_FORMAT = "%d. — ";

        private final char FIRST_LETTER;
        private int mAnswersType = -1;
        private int mAnswersCount = 0;
        private String[] mAnswers = {};
        private String mCorrectAnswer;
        private String mAnswer;
        private RecyclerLinearLayout mLayout;
        private LayoutInflater inflater = from(itemView.getContext());

        public AnswersAdapter(char firstLetter) {
            FIRST_LETTER = firstLetter;
        }

        @Override
        public int getItemCount() {
            return mAnswersCount;
        }

        @Override
        public RecyclerLinearLayout.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case Question.TYPE_1:
                    view = inflater.inflate(R.layout.one_answer_item, parent, false);
                    if (isViewMode())
                        view.setBackgroundResource(0);
                    else
                        view.setOnClickListener(this);
                    return new OneAnswerVH(view);
                case Question.TYPE_3:
                    if (isViewMode()) {
                        view = inflater.inflate(R.layout.multiple_answer_item_vm, parent, false);
                        return new MultipleAnswersVH(view);
                    } else {
                        view = inflater.inflate(R.layout.multiple_answer_item, parent, false);
                        return new MultipleAnswersVH(view, new LettersAdapter(), this);
                    }
                default:
                    throw new IllegalArgumentException("Invalid viewType " + viewType + "for "
                            + AnswersAdapter.class);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerLinearLayout.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case Question.TYPE_1:
                    onBindOneAnswerVH((OneAnswerVH) holder, position);
                    break;
                case Question.TYPE_3:
                    onBindMultipleAnswersVH((MultipleAnswersVH) holder, position);
                    break;
            }
        }

        private void onBindOneAnswerVH(OneAnswerVH holder, int position) {
            holder.letter.setText(format(LETTER_FORMAT, (char) (FIRST_LETTER + position)));
            holder.text.setText(fromHtml(mAnswers[position], new ImageGetter(holder.text), null));

            if (isViewMode()) {
                int correctAnswer = parseInt(mCorrectAnswer) - 1;
                int userAnswer = mAnswer == null ? -1 : (parseInt(mAnswer) - 1);
                if (position == correctAnswer)
                    holder.setTextColor(GREEN_700);
                else if (position == userAnswer)
                    holder.setTextColor(RED_700);
                else
                    holder.setTextColor(DEFAULT);
            }
        }

        private void onBindMultipleAnswersVH(MultipleAnswersVH holder, int position) {
            holder.number.setText(format(NUMBER_FORMAT, position + 1));
            if (isViewMode()) {
                int correctAnswer = mCorrectAnswer.charAt(position) - '0' - 1;
                String answer = format(LETTER_FORMAT, (char) (FIRST_LETTER + correctAnswer));
                Object colorSpan = new ForegroundColorSpan(GREEN_700);

                SpannableString spannable = new SpannableString(answer);
                spannable.setSpan(colorSpan, 0, spannable.length(), Spannable.SPAN_POINT_MARK);

                holder.number.append(spannable);

                int userAnswer = mAnswer.charAt(position) - '0' - 1;

                if (userAnswer == -1) {
                    holder.userAnswer.setText("—");
                    holder.userAnswer.setTextColor(RED_700);
                } else if (userAnswer != correctAnswer) {
                    holder.userAnswer.setText(valueOf((char) (FIRST_LETTER + userAnswer)));
                    holder.userAnswer.setTextColor(RED_700);
                } else {
                    holder.userAnswer.setText("+");
                    holder.userAnswer.setTextColor(GREEN_700);
                }
            } else {
                holder.letters.setTag(NO_NOTIFY_POSITION);
                holder.letters.setSelection(mAnswer.charAt(position) - '0', false);
                holder.letters.setTag(position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mAnswersType;
        }

        @Override
        public int[] getItemViewTypes() {
            return new int[]{Question.TYPE_1, Question.TYPE_3};
        }

        public void swapData(int type, String answers, String correctAnswer, String answer) {
            mAnswersType = type;
            switch (type) {
                case Question.TYPE_1:
                    mAnswers = answers.split(ANSWERS_DIVIDER_REGEX);
                    mAnswersCount = mAnswers.length;
                    mCorrectAnswer = correctAnswer;
                    if (isViewMode()) {
                        mAnswer = answer;
                        notifyDataSetChanged();
                    } else
                        selectOneAnswer(answer == null ? -1 : (parseInt(answer) - 1), false);
                    break;
                case Question.TYPE_3:
                    mAnswers = answers.split(ANSWERS_COUNT_DIVIDER_REGEX);
                    mAnswersCount = parseInt(mAnswers[0]);
                    if (answer == null)
                        mAnswer = new String(new char[mAnswersCount]).replace("\0", "0");
                    else
                        mAnswer = answer;
                    mCorrectAnswer = correctAnswer;
                    notifyDataSetChanged();
                    break;
            }
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
        public void onClick(View view) {
            switch (mAnswersType) {
                case Question.TYPE_1:
                    if (mLayout != null)
                        selectOneAnswer(mLayout.getChildAdapterPosition(view), true);
                    break;
            }
        }

        private void selectOneAnswer(int newAnswerPosition, boolean fromUser) {
            int selectedAnswerPosition = mAnswer == null ? -1 : parseInt(mAnswer) - 1;
            RecyclerLinearLayout.ViewHolder viewHolder;
            viewHolder = mLayout.getChildViewHolder(selectedAnswerPosition);
            if (viewHolder != null) {
                viewHolder.mItemView.setSelected(false);
            }

            if (!fromUser)
                notifyDataSetChanged();

            viewHolder = mLayout.getChildViewHolder(newAnswerPosition);
            if (viewHolder != null) {
                viewHolder.mItemView.setSelected(true);
            }

            mAnswer = String.valueOf(newAnswerPosition + 1);
            if (fromUser)
                onAnswerChanged(mAnswer);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int letter, long id) {
            int position = (int) parent.getTag();
            if (position != NO_NOTIFY_POSITION) {
                final String OLD_ANSWER = mAnswer;
                StringBuilder newAnswer = new StringBuilder(mAnswer);
                int oldPosition = OLD_ANSWER.indexOf('0' + letter);
                if (oldPosition != -1) {
                    newAnswer.setCharAt(oldPosition, '0');
                    MultipleAnswersVH viewHolder = getMultipleAnswerVH(oldPosition);
                    if (viewHolder != null) {
                        viewHolder.letters.setTag(NO_NOTIFY_POSITION);
                        viewHolder.letters.setSelection(0, false);
                        viewHolder.letters.setTag(oldPosition);
                    }
                }
                newAnswer.setCharAt(position, (char) ('0' + letter));
                mAnswer = newAnswer.toString();
                onAnswerChanged(mAnswer);
            }
        }

        private MultipleAnswersVH getMultipleAnswerVH(int position) {
            if (mLayout != null)
                return (MultipleAnswersVH) mLayout.getChildViewHolder(position);

            return null;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        class LettersAdapter extends BaseAdapter {

            LayoutInflater inflater = from(itemView.getContext());

            @Override
            public int getCount() {
                return parseInt(mAnswers[1]) + 1;
            }

            @Override
            public String getItem(int position) {
                return position == 0 ? "" : (char) (FIRST_LETTER + position - 1) + ".";
            }

            public String getDropDownItem(int position) {
                return position == 0 ? "" : String.valueOf((char) (FIRST_LETTER + position - 1));
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.drop_down_letter, parent, false);
                }

                ((TextView) convertView.findViewById(R.id.letter))
                        .setText(getDropDownItem(position));

                return convertView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.letter, parent, false);
                }

                ((TextView) convertView.findViewById(R.id.letter))
                        .setText(getItem(position));

                return convertView;
            }
        }
    }

}

class QuestionStatementVH extends QuestionBaseVH
        implements DiscreteSeekBar.OnProgressChangeListener {

    DiscreteSeekBar seekBar;
    TextView chosenPoint;

    public QuestionStatementVH(View itemView, boolean viewMode) {
        super(itemView, false);
        ViewStub stub = (ViewStub) itemView.findViewById(R.id.answers_view_stub);
        int layoutResId = viewMode ? R.layout.statement_answers_vm : R.layout.statement_answers;
        stub.setLayoutResource(layoutResId);
        stub.setOnInflateListener(this);
        stub.inflate();
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        seekBar = (DiscreteSeekBar) inflated.findViewById(R.id.seek_bar);
        chosenPoint = (TextView) inflated.findViewById(R.id.chosen_point);
        if (seekBar != null)
            seekBar.setOnProgressChangeListener(this);
    }

    public void setChosenPoint(Object value) {
        String format = chosenPoint.getContext().getString(R.string.chosen_point_format);
        chosenPoint.setText(format(format, valueOf(value)));
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        setChosenPoint(value);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        onAnswerChanged(valueOf(seekBar.getProgress()));
    }

}

class QuestionEditableVH extends QuestionBaseVH implements TextWatcher {

    EditText input;

    TextView userAnswer;
    TextView correctAnswer;

    public QuestionEditableVH(View itemView, int type, boolean viewMode) {
        super(itemView, true);
        ViewStub stub = (ViewStub) itemView.findViewById(R.id.answers_view_stub);
        if (viewMode) {
            stub.setLayoutResource(R.layout.editable_answers_vm);
            View inflated = stub.inflate();
            userAnswer = (TextView) inflated.findViewById(R.id.user_answer);
            correctAnswer = (TextView) inflated.findViewById(R.id.correct_answer);
        } else {
            stub.setLayoutResource(type == Question.TYPE_4 ? R.layout.editable_answers_combination :
                    R.layout.editable_answers);
            input = (EditText) stub.inflate().findViewById(R.id.input);
            input.addTextChangedListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if ((boolean) input.getTag())
            onAnswerChanged(s.toString());
    }
}

/**
 * Adapter of the questions list in Testing.
 */
public class QuestionsAdapter extends CursorRecyclerViewAdapter<QuestionBaseVH>
        implements OnClickListener, View.OnFocusChangeListener {

    /**
     * LOADER_QUESTIONS projection.
     */
    public static final String[] QUESTIONS_PROJECTION = new String[]{
            Question._ID,
            Question.TYPE,
            Question.TEXT,
            Question.ADDITIONAL_TEXT,
            Question.ANSWERS,
            Question.POINT
    };
    /**
     * LOADER_QUESTIONS projection columns ID`s.
     */
    private static final int TYPE_COLUMN_ID = 1;
    private static final int TEXT_COLUMN_ID = 2;
    private static final int ADDITIONAL_TEXT_COLUMN_ID = 3;
    private static final int ANSWERS_COLUMN_ID = 4;
    private static final int POINT_COLUMN_ID = 5;

    public static final String[] ANSWERS_PROJECTION = new String[]{
            QuestionAndAnswer.ANSWER,
            QuestionAndAnswer.CORRECT_ANSWER,
            QuestionAndAnswer._ID
    };
    /**
     * LOADER_ANSWERS projection columns ID`s.
     */
    private static final int ANSWER_COLUMN_ID = 0;
    private static final int CORRECT_ANSWER_COLUMN_ID = 1;

    /**
     * ViewHolders views types
     */
    private static final int QUESTION_CHOICES = 0;
    private static final int QUESTION_STATEMENT = 1;
    private static final int QUESTION_EDITABLE = 2;

    /**
     * Language subjects ids.
     */
    private static final long UKRAINIAN = 1;
    private static final long ENGLISH = 7;

    private static final String SEQUENCE_NUMBER_FORMAT = "%d/%d";
    private static final CharSequence TABLE = "<table";
    private static final String SRC = "src=\"";

    private final char FIRST_LETTER;
    private final int QUESTIONS_COUNT_BASE;
    private final String QUESTION_FORMAT;
    private final String SRC_REPLACEMENT;
    private final String HTML_FORMAT;
    private final String READ_TEXT;
    private final int TEXT_SIZE;
    private final boolean viewMode;
    private OnAnswerChangeListener mOnAnswerChangeListener;
    private MaterialDialog materialDialog;
    private LayoutInflater mInflater;
    private Activity mActivity;
    private Cursor mAnswersCursor = null;

    public QuestionsAdapter(Activity activity, long subjectId, boolean isPassed) {
        mActivity = activity;
        mInflater = from(activity);
        int textForReadingResId;
        int closeResId;
        if (subjectId == ENGLISH) {
            FIRST_LETTER = 'A';
            textForReadingResId = R.string.text_for_reading_en;
            QUESTION_FORMAT = activity.getString(R.string.question_en_format);
            closeResId = R.string.close_en;
            READ_TEXT = activity.getString(R.string.read_text_en);
        } else {
            FIRST_LETTER = 'А';
            textForReadingResId = R.string.text_for_reading;
            QUESTION_FORMAT = activity.getString(R.string.question_format);
            closeResId = R.string.close;
            READ_TEXT = activity.getString(R.string.read_text);
        }
        QUESTIONS_COUNT_BASE = subjectId == UKRAINIAN || subjectId == ENGLISH ? -1 : 0;
        SRC_REPLACEMENT = SRC + "file://" + activity.getFilesDir().getPath();
        HTML_FORMAT = activity.getString(R.string.html_format);
        TEXT_SIZE = (int) (activity.getResources().getDimension(R.dimen.abc_text_size_small_material)
                / activity.getResources().getDisplayMetrics().scaledDensity);
        viewMode = isPassed;
        materialDialog = new MaterialDialog.Builder(activity)
                .title(textForReadingResId)
                .positiveText(closeResId)
                .build();
    }

    @Override
    public int getItemCount() {
        if (mAnswersCursor == null)
            return 0;
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        moveCursorToPosition(position);
        switch (getCursor().getInt(TYPE_COLUMN_ID)) {
            case Question.TYPE_1:
            case Question.TYPE_3:
                return QUESTION_CHOICES;
            case Question.TYPE_2:
                return QUESTION_STATEMENT;
            default:
                return QUESTION_EDITABLE;
        }
    }

    @Override
    protected void moveCursorToPosition(int position) {
        super.moveCursorToPosition(position);
        moveAnswerCursorToPosition(position);
    }

    private void moveAnswerCursorToPosition(int position) {
        if (mAnswersCursor != null && !mAnswersCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move answers cursor to position " + position);
        }
    }

    public void changeAnswersCursor(Cursor cursor) {
        Cursor old = swapAnswersCursor(cursor);
        if (old != null)
            old.close();
    }

    public Cursor swapAnswersCursor(Cursor newCursor) {
        if (mAnswersCursor == newCursor)
            return null;
        final Cursor oldCursor = mAnswersCursor;
        mAnswersCursor = newCursor;
        if (getCursor() != null && oldCursor == null)
            notifyDataSetChanged();
        return oldCursor;
    }

    private String getUserAnswer() {
        if (!mAnswersCursor.isNull(ANSWER_COLUMN_ID))
            return mAnswersCursor.getString(ANSWER_COLUMN_ID);

        return null;
    }

    private String getCorrectAnswer() {
        return mAnswersCursor.getString(CORRECT_ANSWER_COLUMN_ID);
    }

    @Override
    public QuestionBaseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.question_item, parent, false);
        QuestionBaseVH viewHolder;
        switch (viewType) {
            case QUESTION_CHOICES:
                viewHolder = new QuestionChoicesVH(view, FIRST_LETTER, READ_TEXT, this);
                break;
            case QUESTION_STATEMENT:
                viewHolder = new QuestionStatementVH(view, viewMode);
                break;
            case QUESTION_EDITABLE:
                viewHolder = new QuestionEditableVH(view, viewType, viewMode);
                break;
            default:
                throw new IllegalArgumentException("Illegal viewType " + viewType
                        + " for " + QuestionsAdapter.class);
        }

        if (!viewMode) {
            viewHolder.setOnAnswerChangeListener(mOnAnswerChangeListener);
            view.setOnFocusChangeListener(this);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(QuestionBaseVH viewHolder, int position) {
        moveAnswerCursorToPosition(position);
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public void onBindViewHolder(QuestionBaseVH viewHolder, Cursor cursor) {
        int viewType = getItemViewType(viewHolder.getAdapterPosition());

        if (viewHolder.hasHeader()) {
            int number = cursor.getPosition() + 1;
            int count = QUESTIONS_COUNT_BASE + cursor.getCount();

            viewHolder.number.setText(format(QUESTION_FORMAT, number));
            viewHolder.sequenceNumber.setText(format(SEQUENCE_NUMBER_FORMAT, number, count));
        }

        /*
        * TODO: Change HTML detecting
        * */
        String text = cursor.getString(TEXT_COLUMN_ID);

        if (text.contains(TABLE)) {
            text = text.replace(SRC, SRC_REPLACEMENT);
            text = format(HTML_FORMAT, TEXT_SIZE, text);
            viewHolder.text.setVisibility(View.GONE);
            viewHolder.textTaggedStub.setVisibility(View.VISIBLE);
            viewHolder.textTagged.loadUrl("about:blank");
            viewHolder.textTagged.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
        } else {
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.textTaggedStub.setVisibility(View.GONE);
            viewHolder.text.setText(fromHtml(text, new ImageGetter(viewHolder.text), null));
        }

        switch (viewType) {
            case QUESTION_CHOICES:
                onBindQuestionChoicesVH((QuestionChoicesVH) viewHolder, cursor);
                break;
            case QUESTION_STATEMENT:
                onBindQuestionStatementVH((QuestionStatementVH) viewHolder, cursor);
                break;
            case QUESTION_EDITABLE:
                onBindQuestionEditableVH((QuestionEditableVH) viewHolder, cursor);
                break;
        }
    }

    private void onBindQuestionChoicesVH(QuestionChoicesVH viewHolder, Cursor cursor) {
        if (cursor.isNull(ADDITIONAL_TEXT_COLUMN_ID))
            viewHolder.additionalText.setVisibility(View.GONE);
        else {
            viewHolder.additionalText.setTag(cursor.getPosition());
            viewHolder.additionalText.setVisibility(View.VISIBLE);
        }
        int type = cursor.getInt(TYPE_COLUMN_ID);
        String userAnswer = getUserAnswer();
        String answers = cursor.getString(ANSWERS_COLUMN_ID);
        viewHolder.adapter.swapData(type, answers, getCorrectAnswer(), userAnswer);
        if (viewMode && type == Question.TYPE_1)
            viewHolder.warning.setVisibility(isEmpty(userAnswer) ? View.VISIBLE : View.GONE);
        else
            viewHolder.warning.setVisibility(View.GONE);
    }


    private void onBindQuestionStatementVH(QuestionStatementVH viewHolder, Cursor cursor) {
        final int maxPoint = cursor.getInt(POINT_COLUMN_ID);
        String answer = getUserAnswer();
        if (answer == null) {
            answer = valueOf(maxPoint / 2);
        }
        if (viewMode) {
            viewHolder.setChosenPoint(answer);
        } else {
            viewHolder.seekBar.setTag(viewHolder.getItemId());
            viewHolder.seekBar.setMax(maxPoint);
            viewHolder.seekBar.setProgress(parseInt(answer));
        }
    }

    private void onBindQuestionEditableVH(QuestionEditableVH viewHolder, Cursor cursor) {
        String userAnswer = getUserAnswer();
        String correctAnswer = getCorrectAnswer();
        if (viewMode) {
            int type = cursor.getInt(TYPE_COLUMN_ID);

            if (isEmpty(userAnswer)) {
                viewHolder.userAnswer.setTextColor(viewHolder.RED_700);
                viewHolder.userAnswer.setText(R.string.unanswered_question_warning);
            } else {
                SpannableString answer;
                Object span;

                if (type == Question.TYPE_4) {
                    viewHolder.userAnswer.setText(R.string.user_combination);
                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    char[] chars = correctAnswer.toCharArray();
                    char c;
                    int index;
                    for (int i = 0; i < userAnswer.length(); i++) {
                        c = userAnswer.charAt(i);
                        builder.insert(i * 2, c + " ");
                        index = correctAnswer.indexOf(c);

                        if (index == -1 || chars[index] != c) {
                            span = new ForegroundColorSpan(viewHolder.RED_700);
                        } else {
                            chars[index] = ' ';
                            span = new ForegroundColorSpan(viewHolder.GREEN_700);
                        }

                        builder.setSpan(span, i * 2, i * 2 + 1, Spannable.SPAN_POINT_MARK);
                    }

                    answer = new SpannableString(builder);
                } else {
                    viewHolder.userAnswer.setText(R.string.user_answer);

                    span = correctAnswer.equals(userAnswer) ?
                            new ForegroundColorSpan(viewHolder.GREEN_700)
                            : new ForegroundColorSpan(viewHolder.RED_700);
                    answer = new SpannableString(userAnswer);
                    answer.setSpan(span, 0, answer.length(), Spannable.SPAN_POINT_MARK);
                }

                viewHolder.userAnswer.append(" ");
                viewHolder.userAnswer.append(answer);
            }

            if (type == Question.TYPE_4) {
                viewHolder.correctAnswer.setText(R.string.correct_combination);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < correctAnswer.length(); i++) {
                    builder.insert(i * 2, correctAnswer.charAt(i) + " ");
                }
                correctAnswer = builder.toString();
            } else {
                viewHolder.correctAnswer.setText(R.string.correct_answer);
            }

            Object colorSpan = new ForegroundColorSpan(viewHolder.GREEN_700);
            SpannableString answer = new SpannableString(correctAnswer);
            answer.setSpan(colorSpan, 0, answer.length(), Spannable.SPAN_POINT_MARK);

            viewHolder.correctAnswer.append(" ");
            viewHolder.correctAnswer.append(answer);
        } else {
            viewHolder.input.setTag(false);
            viewHolder.input.setText(userAnswer);
            viewHolder.input.setTag(true);
            viewHolder.input.clearFocus();
        }
    }

    private String getAdditionalText(int position) {
        moveCursorToPosition(position);
        return getCursor().getString(ADDITIONAL_TEXT_COLUMN_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read_text_button:
                ImageGetter imageGetter = new ImageGetter(materialDialog.getContentView());
                Spanned content = fromHtml(getAdditionalText((int) v.getTag()), imageGetter, null);
                materialDialog = materialDialog.getBuilder().content(content).show();
                break;
        }
    }

    public void setOnAnswerChangeListener(OnAnswerChangeListener onAnswerChangeListener) {
        mOnAnswerChangeListener = onAnswerChangeListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus || !(v instanceof EditText)) {
            UiUtils.hideSoftKeyboard(mActivity);
        }
    }

    public interface OnAnswerChangeListener {
        void onAnswerChanged(long questionId, String answer);
    }
}


