package net.zno_ua.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.R;
import net.zno_ua.app.TestActivity;
import net.zno_ua.app.ViewImageActivity;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.models.Question;
import net.zno_ua.app.models.Record;
import net.zno_ua.app.models.Test;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class QuestionFragment extends Fragment {

    private static final char ENG_LETTER = 65;
    private static final char UKR_LETTER = 1040;
    private static final char ZERO = '0';
    private static final String FIRST_LETTER = "first_letter";
    private static final String ID_ON_TEST = "id_on_test";
    private static final String HTML = "text/html";
    private static final String UTF8 = "utf-8";
    private static final String HTML_FORMAT;
    private static final String PATH;
    private static final String TABLE = "<table";
    private static final String SRC = "src=\"";

    static {
        ZNOApplication app = ZNOApplication.getInstance();
        Resources res = app.getResources();
        float scaledDensity = res.getDisplayMetrics().scaledDensity;
        float textSizeInPX = res.getDimension(R.dimen.question_text_size);
        int textSize = (int) (textSizeInPX / scaledDensity);
        HTML_FORMAT = "<html><head>" +
                "<link href=\"file:///android_asset/style/question.css\" " +
                "rel=\"stylesheet\" type=\"text/css\">" +
                "<style> html, body, table { font-size: " + textSize + "px; }" +
                "</style></head><body>%s</body></html>";
        PATH = SRC + "file://" + app.getFilesDir().getPath();
    }

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int id, String answer, boolean switchToNext);
    }

    int idOnTest;
    int id;
    int taskAll;
    String question;
    String parentQuestion;
    String answers;
    String correctAnswer;
    String userAnswer;
    int type;
    int balls;
    char firstLetter;
    Record results;

    boolean viewMode;
    OnAnswerSelectedListener callBack;
    FileManager fm;
    Resources res;

    public static QuestionFragment newInstance(boolean viewMode, int idOnTest, Question question,
                                               int taskAll, int lessonId) {
        return QuestionFragment.newInstance(viewMode, idOnTest, question, taskAll, lessonId, null);
    }

    public static QuestionFragment newInstance(boolean viewMode, int idOnTest, Question question,
                                               int taskAll, int lessonId, Record results) {
        QuestionFragment f = new QuestionFragment();
        f.viewMode = viewMode;
        f.idOnTest = idOnTest;
        f.id = question.id;
        f.taskAll = taskAll;
        f.firstLetter = (lessonId == 7) ? ENG_LETTER : UKR_LETTER;
        f.question = question.question;
        f.parentQuestion = question.parentQuestion;
        f.answers = question.answers;
        f.correctAnswer = question.correctAnswer;
        f.userAnswer = question.getUserAnswer();
        f.type = question.type;
        f.balls = question.balls;
        f.results = results;
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fm = new FileManager(activity);
        callBack = (OnAnswerSelectedListener) activity;
        res = activity.getResources();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            idOnTest = savedInstanceState.getInt(ID_ON_TEST);
            id = savedInstanceState.getInt(Question.ID);
            taskAll = savedInstanceState.getInt(Test.TASK_ALL);
            question = savedInstanceState.getString(Question.QUESTION);
            parentQuestion = savedInstanceState.getString(Question.PARENT_QUESTION);
            answers = savedInstanceState.getString(Question.ANSWERS);
            userAnswer = savedInstanceState.getString(Question.ANSWER);
            correctAnswer = savedInstanceState.getString(Question.CORRECT_ANSWER);
            type = savedInstanceState.getInt(Question.TYPE);
            balls = savedInstanceState.getInt(Question.BALLS);
            firstLetter = savedInstanceState.getChar(FIRST_LETTER);
            viewMode = savedInstanceState.getBoolean(TestActivity.Extra.VIEW_MODE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ID_ON_TEST, idOnTest);
        outState.putInt(Question.ID, id);
        outState.putInt(Test.TASK_ALL, taskAll);
        outState.putString(Question.QUESTION, question);
        outState.putString(Question.PARENT_QUESTION, parentQuestion);
        outState.putString(Question.ANSWERS, answers);
        outState.putString(Question.ANSWER, userAnswer);
        outState.putString(Question.CORRECT_ANSWER, correctAnswer);
        outState.putInt(Question.TYPE, type);
        outState.putInt(Question.BALLS, balls);
        outState.putChar(FIRST_LETTER, firstLetter);
        outState.putBoolean(TestActivity.Extra.VIEW_MODE, viewMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View parent = inflater.inflate(R.layout.test_question, container, false);

        if (results != null) {
            createResultsView(inflater, parent);
        }

        switch (type) {
            case Question.TYPE_1:
                createType1QuestionView(inflater, parent);
                break;
            case Question.TYPE_2:
                createType2QuestionView(inflater, parent);
                break;
            case Question.TYPE_3:
                createType3QuestionView(inflater, parent);
                break;
            case Question.TYPE_4:
                createType4QuestionView(inflater, parent);
                break;
            case Question.TYPE_5:
                createType5QuestionView(inflater, parent);
                break;
        }

        return parent;
    }

    private void createType1QuestionView(LayoutInflater inflater, View parent) {
        LinearLayout questionContainer =
                (LinearLayout) parent.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        if (viewMode && userAnswer.isEmpty()) {
            inflater.inflate(R.layout.unanswered_question_warning, questionContainer, true);
        }

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);

        questionContainer.addView(answersContainer);


        String[] answers = this.answers.split("\n");
        final View[] answerItems = new View[answers.length];
        Pattern answerPattern = Pattern.compile("(^.*?)(\\.\\s|\\s)(.*?$)");
        Matcher matcher;

        TextView answerItemLetter;
        TextView answerItemText;

        for (int i = 0; i < answers.length; i++) {
            answerItems[i] = inflater.inflate(R.layout.answer, answersContainer, false);

            answerItemLetter = (TextView) answerItems[i].findViewById(R.id.letter);
            answerItemText = (TextView) answerItems[i].findViewById(R.id.text);

            matcher = answerPattern.matcher(answers[i]);

            if (matcher.find()) {
                answerItemLetter.setText(Html.fromHtml(matcher.group(1), imgGetter, null));
                answerItemText.setText(Html.fromHtml(matcher.group(3), imgGetter, null));

                final int num = i;
                if (viewMode) {
                    if (correctAnswer.equals(String.valueOf(i + 1))) {
                        answerItems[i].setBackgroundResource(R.drawable.bg_green);
                        answerItemLetter.setTextColor(res.getColor(R.color.text_color_white_gray));
                        answerItemText.setTextColor(res.getColor(R.color.text_color_white_gray));
                        ImageView circle =
                                (ImageView) answerItems[i].findViewById(R.id.answer_letter_circle);
                        circle.setImageResource(R.drawable.circle_white);
                    } else if (userAnswer.equals(String.valueOf(i + 1))) {
                        answerItems[i].setBackgroundResource(R.drawable.bg_orange);
                        answerItemLetter.setTextColor(res.getColor(R.color.text_color_white_gray));
                        answerItemText.setTextColor(res.getColor(R.color.text_color_white_gray));
                        ImageView circle =
                                (ImageView) answerItems[i].findViewById(R.id.answer_letter_circle);
                        circle.setImageResource(R.drawable.circle_white);
                    }
                    answerItems[i].setClickable(false);

                } else {
                    answerItems[i].setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (!userAnswer.isEmpty()) {
                                int oldNum = Integer.valueOf(userAnswer) - 1;
                                answerItems[oldNum].setSelected(false);
                            }
                            answerItems[num].setSelected(true);
                            userAnswer = String.valueOf((num + 1));
                            callBack.onAnswerSelected(idOnTest, userAnswer, true);
                        }
                    });
                }

                answersContainer.addView(answerItems[i]);
            }
        }

        if (!viewMode && !userAnswer.isEmpty()) {
            answerItems[Integer.valueOf(userAnswer) - 1].setSelected(true);
        }

    }

    private void createType2QuestionView(LayoutInflater inflater, View parent) {
        LinearLayout questionContainer =
                (LinearLayout) parent.findViewById(R.id.test_question_container);

        if (balls == 0) {
            questionContainer.addView(createQuestionText(inflater, questionContainer));
        } else {
            questionContainer.addView(createStatementQuestionText(inflater, questionContainer));
        }

    }

    private void createType3QuestionView(LayoutInflater inflater, View parent) {
        LinearLayout questionContainer =
                (LinearLayout) parent.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
        questionContainer.addView(answersContainer);

        int numCounts = Integer.parseInt(answers.split("-")[0]);
        int varCounts = Integer.parseInt(answers.split("-")[1]);

        final View[] answerItems = new View[numCounts];
        final View[][] answerItemLetters = new View[numCounts][varCounts];

        LinearLayout answerLettersContainer;
        TextView answerCoupleNum;
        TextView answerItemLetter;

        int correctNum;
        int userNum;

        for (int i = 0; i < answerItems.length; i++) {
            answerItems[i] =
                    inflater.inflate(R.layout.answers_connections, answersContainer, false);

            answerCoupleNum = (TextView) answerItems[i].findViewById(R.id.answer_couple_num);
            answerCoupleNum.setText(String.valueOf((i + 1)));
            answerLettersContainer = (LinearLayout) answerItems[i]
                    .findViewById(R.id.answer_couple_letters_container);

            final int num = i;
            char userAnswerChar = userAnswer.charAt(i);
            char correctAnswerChar = correctAnswer.charAt(i);
            for (int j = 0; j < answerItemLetters[0].length; j++) {
                answerItemLetters[i][j] =
                        inflater.inflate(R.layout.answer_letter, answerLettersContainer, false);
                answerItemLetter = (TextView) answerItemLetters[i][j]
                        .findViewById(R.id.letter);
                answerItemLetter.setText(String.valueOf((char) (firstLetter + j)));

                final int letterNum = j;

                if (viewMode) {
                    answerItemLetters[i][j].setClickable(false);
                    if (userAnswerChar == ZERO) {
                        answerItems[i].setBackgroundResource(R.drawable.bg_orange);
                        answerCoupleNum.setTextColor(res.getColor(R.color.text_color_white_gray));
                        ImageView circle = (ImageView) answerItemLetters[i][j]
                                .findViewById(R.id.answer_letter_circle);
                        if (j == correctAnswerChar - ZERO - 1) {
                            circle.setImageResource(R.drawable.circle_green);
                            answerItemLetter
                                    .setTextColor(res.getColor(R.color.text_color_white_gray));
                        } else {
                            circle.setImageResource(R.drawable.circle_white);
                            answerItemLetter
                                    .setTextColor(res.getColor(R.color.text_color_white_gray));
                        }
                    } else {
                        correctNum = correctAnswer.charAt(i) - ZERO - 1;
                        userNum = userAnswer.charAt(i) - ZERO - 1;

                        ImageView circle = (ImageView) answerItemLetters[i][j]
                                .findViewById(R.id.answer_letter_circle);
                        if (j == correctNum) {
                            circle.setImageResource(R.drawable.circle_green);
                            answerItemLetter
                                    .setTextColor(res.getColor(R.color.text_color_white_gray));
                        } else if (j == userNum) {
                            circle.setImageResource(R.drawable.circle_orange);
                            answerItemLetter
                                    .setTextColor(res.getColor(R.color.text_color_white_gray));
                        }

                    }
                } else {
                    answerItemLetters[i][j].setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            StringBuilder sb = new StringBuilder(userAnswer);
                            char oldNumLetter = sb.charAt(num);
                            char newNumLetter = (char) (ZERO + letterNum + 1);
                            if (oldNumLetter != ZERO) {
                                answerItemLetters[num][oldNumLetter - ZERO - 1].setSelected(false);
                            }
                            int usedNumLetter = sb.indexOf(String.valueOf(newNumLetter));
                            if (usedNumLetter != -1) {
                                answerItemLetters[usedNumLetter][letterNum].setSelected(false);
                                sb.setCharAt(usedNumLetter, ZERO);
                            }

                            sb.setCharAt(num, newNumLetter);
                            answerItemLetters[num][letterNum].setSelected(true);
                            userAnswer = sb.toString();
                            if (!userAnswer.contains("0")) {
                                callBack.onAnswerSelected(idOnTest, userAnswer, true);
                            } else {
                                callBack.onAnswerSelected(idOnTest, userAnswer, false);
                            }
                        }
                    });
                }

                answerLettersContainer.addView(answerItemLetters[i][j]);
            }

            if (!viewMode) {
                char oldNumLetter = userAnswer.charAt(i);
                if (oldNumLetter != ZERO) {
                    answerItemLetters[i][oldNumLetter - ZERO - 1].setSelected(true);
                }
            }
            answersContainer.addView(answerItems[i]);
        }

    }

    private void createType4QuestionView(LayoutInflater inflater, View parent) {
        LinearLayout questionContainer =
                (LinearLayout) parent.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
        questionContainer.addView(answersContainer);

        View answerItem;

        if (viewMode) {
            answerItem =
                    inflater.inflate(
                            R.layout.answer_only_correct_view_mode, answersContainer, false);
            TextView userAnswerText = (TextView) answerItem.findViewById(R.id.vm_answer_short_user);
            TextView correctAnswerText =
                    (TextView) answerItem.findViewById(R.id.vm_answer_short_correct);

            String correctAnswerCombination = "";
            for (int i = 0; i < correctAnswer.length(); i++) {
                correctAnswerCombination += correctAnswer.charAt(i) + " ";
            }
            correctAnswerText.setText(correctAnswerCombination);

            if (userAnswer.isEmpty()) {
                userAnswerText.setText(R.string.unanswered_question_warning);
                userAnswerText.setTextColor(res.getColor(R.color.orange));
            } else {
                SpannableStringBuilder userAnswerCombination = new SpannableStringBuilder();
                for (int i = 0; i < userAnswer.length(); i++) {
                    char userAnswerChar = userAnswer.charAt(i);
                    userAnswerCombination.insert(i * 2, userAnswerChar + " ");

                    if (correctAnswer.indexOf(userAnswerChar) == -1) {
                        userAnswerCombination.setSpan(
                                new ForegroundColorSpan(
                                        res.getColor(R.color.orange)), i * 2, i * 2 + 2, 0);
                    } else {
                        userAnswerCombination.setSpan(
                                new ForegroundColorSpan(
                                        res.getColor(R.color.green)), i * 2, i * 2 + 2, 0);
                    }
                }
                userAnswerText.setText(userAnswerCombination);
            }
        } else {
            answerItem = inflater.inflate(R.layout.answer_only_correct, answersContainer, false);
            EditText answerItemInput =
                    (EditText) answerItem.findViewById(R.id.answer_only_correct_input);

            if (!userAnswer.isEmpty()) {
                answerItemInput.setText(userAnswer);
            }

            answerItemInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    userAnswer = s.toString();
                    callBack.onAnswerSelected(idOnTest, userAnswer, false);
                }
            });

            answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || actionId == EditorInfo.IME_ACTION_NEXT) {
                        ZNOApplication.hideKeyboard(getActivity());
                        if (userAnswer.length() == 3) {
                            callBack.onAnswerSelected(idOnTest, userAnswer, true);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        answersContainer.addView(answerItem);

    }

    private void createType5QuestionView(LayoutInflater inflater, View parent) {
        LinearLayout questionContainer =
                (LinearLayout) parent.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
        questionContainer.addView(answersContainer);

        if (viewMode) {
            View answerItem =
                    inflater.inflate(R.layout.answer_short_view_mode, answersContainer, false);
            TextView userAnswerText =
                    (TextView) answerItem.findViewById(R.id.vm_answer_short_user);
            TextView correctAnswerText =
                    (TextView) answerItem.findViewById(R.id.vm_answer_short_correct);

            correctAnswerText.setText(correctAnswer);
            if (userAnswer.isEmpty()) {
                userAnswerText.setText(R.string.unanswered_question_warning);
            } else {
                userAnswerText.setText(userAnswer);
            }

            if (correctAnswer.equals(userAnswer)) {
                userAnswerText.setTextColor(res.getColor(R.color.green));
            } else {
                userAnswerText.setTextColor(res.getColor(R.color.orange));
            }

            answersContainer.addView(answerItem);
        } else {
            View answerItem = inflater.inflate(R.layout.answer_short, answersContainer, false);
            EditText answerItemInput = (EditText) answerItem.findViewById(R.id.short_answer_input);

            if (!userAnswer.isEmpty()) {
                answerItemInput.setText(userAnswer);
            }

            answerItemInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    userAnswer = s.toString();
                    callBack.onAnswerSelected(idOnTest, userAnswer, false);
                }
            });

            answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || actionId == EditorInfo.IME_ACTION_NEXT) {
                        ZNOApplication.hideKeyboard(getActivity());
                        if (!userAnswer.isEmpty()) {
                            callBack.onAnswerSelected(idOnTest, userAnswer, true);
                        }
                        return true;
                    }
                    return false;
                }
            });

            answersContainer.addView(answerItem);
        }

    }

    private View createQuestionHeaderView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.question_header, container, false);

        TextView questionId = (TextView) v.findViewById(R.id.test_question_id);
        TextView questionTaskAll = (TextView) v.findViewById(R.id.test_question_task_all);

        int num = idOnTest + 1;
        questionId.setText(res.getString(R.string.question) + " " + num);
        questionTaskAll.setText(num + "/" + taskAll);

        return v;
    }

    private View createQuestionText(LayoutInflater inflater, ViewGroup container) {
        if (question.contains(TABLE)) {
            View view = inflater.inflate(R.layout.question_text_web, container, false);
            String body = question.replace(SRC, PATH);
            WebView webText = (WebView) view.findViewById(R.id.question_text);
            webText.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(ViewImageActivity.DATA_SCHEMA)) {
                        Intent viewImg = new Intent(Intent.ACTION_VIEW);
                        viewImg.setData(Uri.parse(url));
                        getActivity().startActivity(viewImg);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }

            });
            webText.setFocusable(false);
            webText.setFocusableInTouchMode(false);
            String questionText = String.format(HTML_FORMAT, body);
            webText.loadDataWithBaseURL(null, questionText, HTML, UTF8, null);
            return view;
        } else {
            TextView questionText =
                    (TextView) inflater.inflate(R.layout.question_text, container, false);
            questionText.setText(Html.fromHtml(question, imgGetter, null));
            questionText.setMovementMethod(LinkMovementMethod.getInstance());
            return questionText;
        }
    }

    private View createStatementQuestionText(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.question_statement, container, false);

        TextView questionText = (TextView) v.findViewById(R.id.question_statement_text);
        questionText.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView ballsText = (TextView) v.findViewById(R.id.question_statement_chosen_balls);
        SeekBar ballsSeekBar = (SeekBar) v.findViewById(R.id.question_statement_balls);

        if (viewMode) {
            questionText.setText(Html.fromHtml(question, imgGetter, null));
            ballsText.setText(res.getString(R.string.chosen_point) + " " + userAnswer);
            ballsSeekBar.setVisibility(View.GONE);
        } else {
            questionText.setText(
                    Html.fromHtml(question + res.getString(R.string.choose_point), imgGetter, null));
            ballsText.setText(
                    res.getString(R.string.chosen_point) + " " + String.valueOf(balls / 2));
            ballsSeekBar.setMax(balls);
            ballsSeekBar.setProgress(balls / 2);
            ballsSeekBar.setSecondaryProgress(0);
            ballsSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    userAnswer = String.valueOf(seekBar.getProgress());
                    callBack.onAnswerSelected(idOnTest, userAnswer, true);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String chosenBall =
                            res.getString(R.string.chosen_point) + " " + String.valueOf(progress);
                    ballsText.setText(chosenBall);
                }
            });
        }

        return v;
    }

    private View createParentQuestionText(LayoutInflater inflater, ViewGroup container) {
        final TextView questionText =
                (TextView) inflater.inflate(R.layout.question_text, container, false);
        final Spanned parentQuestionText =
                Html.fromHtml(res.getString(R.string.parent_question_text_show), imgGetter, null);
        questionText.setText(parentQuestionText);
        questionText.setMovementMethod(LinkMovementMethod.getInstance());
        questionText.setSelected(true);
        questionText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                    questionText.setText(Html.fromHtml(parentQuestion, imgGetter, null));
                } else {
                    v.setSelected(true);
                    questionText.setText(parentQuestionText);
                }
            }
        });

        return questionText;
    }

    private LinearLayout createAnswersContainer(LayoutInflater inflater, ViewGroup container) {
        return (LinearLayout)
                inflater.inflate(R.layout.question_answers_container, container, false);
    }

    private ImageGetter imgGetter = new ImageGetter() {

        public Drawable getDrawable(String source) {
            Drawable drawable = null;

            try {
                drawable = fm.openDrawable(source);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (drawable == null) {
                drawable = res.getDrawable(R.drawable.emo_im_crying);
            }

            DisplayMetrics displayMetrics = res.getDisplayMetrics();

            int width = (int) (drawable.getIntrinsicWidth() * displayMetrics.scaledDensity);
            int height = (int) (drawable.getIntrinsicHeight() * displayMetrics.scaledDensity);
            int maxWidth = (int) (displayMetrics.widthPixels * 0.75f);

            if (width > maxWidth) {
                float scale = (float) maxWidth / (float) width;
                width = maxWidth;
                height = (int) (height * scale);
            }

            drawable.setBounds(0, 0, width, height);

            return drawable;
        }
    };

    private void createResultsView(LayoutInflater inflater, View parent) {
        LinearLayout container = (LinearLayout) parent.findViewById(R.id.test_question_container);
        View resultsView = inflater.inflate(R.layout.test_result, container, false);

        TextView lessonName = (TextView) resultsView.findViewById(R.id.lesson_name);
        TextView testBall = (TextView) resultsView.findViewById(R.id.test_ball);
        TextView ratingBall = (TextView) resultsView.findViewById(R.id.rating_ball);
        TextView elapsedTime = (TextView) resultsView.findViewById(R.id.elapsed_time);

        lessonName.setText(results.lessonName);
        int ballType = (results.znoBall >= 190f) ? Record.GOOD_BALL :
                (results.znoBall < 124f) ? Record.BAD_BALL : 0;

        testBall.setText(ZNOApplication.buildBall(results.testBall, false, ballType));
        ratingBall.setText(ZNOApplication.buildBall(results.znoBall, true, ballType));

        if (results.elapsedTime / 60000 > 0) {
            int minutes = (int) (results.elapsedTime / 60000);
            String time;
            if ((minutes < 10) || (minutes > 20 && minutes < 110) || minutes > 120) {
                switch (minutes % 10) {
                    case 1:
                        time = getString(R.string.one_minute_rod);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        time = getString(R.string.two_four_minutes);
                        break;
                    default:
                        time = getString(R.string.minutes);
                }
            } else {
                time = getString(R.string.minutes);
            }
            time = String.format("%d %s", minutes, time);
            elapsedTime.setText(time);
        } else {
            resultsView.findViewById(R.id.result_time_block_separator)
                    .setVisibility(View.INVISIBLE);
            resultsView.findViewById(R.id.result_time_block)
                    .setVisibility(View.INVISIBLE);
        }

        container.addView(resultsView, 0);
    }

}