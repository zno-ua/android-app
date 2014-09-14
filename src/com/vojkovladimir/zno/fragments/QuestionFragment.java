package com.vojkovladimir.zno.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.TestActivity;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionFragment extends Fragment {

    private static final char ENG_LETTER = 65;
    private static final char UKR_LETTER = 1040;
    private static final String FIRST_LETTER = "first_letter";
    private static final String ID_ON_TEST = "id_on_test";

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
    boolean viewMode;

    FileManager fm;

    OnAnswerSelectedListener callBack;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int id, String answer);
    }

    public static QuestionFragment newInstance(boolean viewMode, int idOnTest, Question question, int taskAll, int lessonId) {
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
        f.userAnswer = question.answer;
        f.type = question.type;
        f.balls = question.balls;
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fm = new FileManager(activity);
        callBack = (OnAnswerSelectedListener) activity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        switch (type) {
            case Question.TYPE_1:
                return createTypeOneQuestionView(inflater, container);
            case Question.TYPE_2:
                return createTypeTwoQuestionView(inflater, container);
            case Question.TYPE_3:
                return createTypeThreeQuestionView(inflater, container);
            case Question.TYPE_4:
                return createTypeFourQuestionView(inflater, container);
            case Question.TYPE_5:
                return createTypeFiveQuestionView(inflater, container);
            default:
                View v = inflater.inflate(R.layout.test_question, container, false);
                return v;
        }
    }

    private View createTypeOneQuestionView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question, container, false);
        LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

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

            answerItemLetter = (TextView) answerItems[i].findViewById(R.id.answer_letter);
            answerItemText = (TextView) answerItems[i].findViewById(R.id.answer_text);

            matcher = answerPattern.matcher(answers[i]);

            if (matcher.find()) {
                answerItemLetter.setText(Html.fromHtml(matcher.group(1), imgGetter, null));
                answerItemText.setText(Html.fromHtml(matcher.group(3), imgGetter, null));

                final int num = i;
                if (viewMode) {
                    if (correctAnswer.equals(String.valueOf(i + 1))) {
                        answerItems[i].setBackgroundResource(R.drawable.item_bg_green);
                        answerItemLetter.setTextColor(getResources().getColor(R.color.item_text_color_selected));
                        answerItemText.setTextColor(getResources().getColor(R.color.item_text_color_selected));
                        ImageView circle = (ImageView) answerItems[i].findViewById(R.id.answer_letter_circle);
                        circle.setImageResource(R.drawable.letter_circle_pressed);
                    } else if (userAnswer.equals(String.valueOf(i + 1))) {
                        answerItems[i].setBackgroundResource(R.drawable.item_bg_orange);
                        answerItemLetter.setTextColor(getResources().getColor(R.color.item_text_color_selected));
                        answerItemText.setTextColor(getResources().getColor(R.color.item_text_color_selected));
                        ImageView circle = (ImageView) answerItems[i].findViewById(R.id.answer_letter_circle);
                        circle.setImageResource(R.drawable.letter_circle_pressed);
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
                            callBack.onAnswerSelected(idOnTest, userAnswer);
                        }
                    });
                }

                answersContainer.addView(answerItems[i]);
            }
        }

        if (!viewMode && !userAnswer.isEmpty()) {
            answerItems[Integer.valueOf(userAnswer) - 1].setSelected(true);
        }

        return v;
    }

    private View createTypeTwoQuestionView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question, container, false);
        LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

        if (balls == 0) {
            questionContainer.addView(createQuestionText(inflater, questionContainer));
        } else {
            questionContainer.addView(createStatementQuestionText(inflater, questionContainer));
        }

        return v;
    }

    private View createTypeThreeQuestionView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question, container, false);
        LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

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

        for (int i = 0; i < answerItems.length; i++) {
            answerItems[i] = inflater.inflate(R.layout.answers_connections, answersContainer, false);

            answerCoupleNum = (TextView) answerItems[i].findViewById(R.id.answer_couple_num);
            answerCoupleNum.setText(String.valueOf((i + 1)));
            answerLettersContainer = (LinearLayout) answerItems[i].findViewById(R.id.answer_couple_letters_container);

            final int num = i;
            for (int j = 0; j < answerItemLetters[0].length; j++) {
                answerItemLetters[i][j] = inflater.inflate(R.layout.answers_letter,
                        answerLettersContainer, false);
                answerItemLetter = (TextView) answerItemLetters[i][j].findViewById(R.id.answer_letter);
                answerItemLetter.setText(String.valueOf((char) (firstLetter + j)));
                final int letterNum = j;
                answerItemLetters[i][j]
                        .setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                StringBuilder sb = new StringBuilder(userAnswer);
                                char oldNumLetter = sb.charAt(num);
                                char newNumLetter = (char) ('0' + letterNum + 1);
                                if (oldNumLetter != '0') {
                                    answerItemLetters[num][oldNumLetter - '0' - 1].setSelected(false);
                                }
                                int usedNumLetter = sb.indexOf(String.valueOf(newNumLetter));
                                if (usedNumLetter != -1) {
                                    answerItemLetters[usedNumLetter][letterNum].setSelected(false);
                                    sb.setCharAt(usedNumLetter, '0');
                                }

                                sb.setCharAt(num, newNumLetter);
                                answerItemLetters[num][letterNum].setSelected(true);
                                userAnswer = sb.toString();
                                if (!userAnswer.contains("0")) {
                                    callBack.onAnswerSelected(idOnTest, userAnswer);
                                }
                            }
                        });

                answerLettersContainer.addView(answerItemLetters[i][j]);
            }
            char oldNumLetter = userAnswer.charAt(i);
            if (oldNumLetter != '0') {
                answerItemLetters[i][oldNumLetter - '0' - 1].setSelected(true);
            }
            answersContainer.addView(answerItems[i]);
        }

        return v;
    }

    private View createTypeFourQuestionView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question, container, false);
        LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
        questionContainer.addView(answersContainer);

        View answerItem = inflater.inflate(R.layout.answer_only_correct, answersContainer, false);
        EditText answerItemInput = (EditText) answerItem.findViewById(R.id.answer_only_correct_input);

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
            }
        });

        answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard();
                    if (userAnswer.length() == 3) {
                        callBack.onAnswerSelected(idOnTest, userAnswer);
                    }
                    return true;
                }
                return false;
            }
        });


        answersContainer.addView(answerItem);
        return v;
    }

    private View createTypeFiveQuestionView(LayoutInflater inflater,
                                            ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question, container, false);
        LinearLayout questionContainer = (LinearLayout) v.findViewById(R.id.test_question_container);

        if (!parentQuestion.isEmpty()) {
            questionContainer.addView(createParentQuestionText(inflater, questionContainer));
        }
        questionContainer.addView(createQuestionHeaderView(inflater, questionContainer));
        questionContainer.addView(createQuestionText(inflater, questionContainer));

        LinearLayout answersContainer = createAnswersContainer(inflater, questionContainer);
        questionContainer.addView(answersContainer);

        if (viewMode) {
            View answerItem = inflater.inflate(R.layout.answer_short_view_mode, answersContainer, false);
            TextView userAnswer = (TextView) answerItem.findViewById(R.id.vm_answer_short_user);
            TextView correctAnswer = (TextView) answerItem.findViewById(R.id.vm_answer_short_correct);

            correctAnswer.setText(this.correctAnswer);
            if (this.userAnswer.isEmpty()) {
                userAnswer.setText(R.string.unanswered_question_warning);
            } else {
                userAnswer.setText(this.userAnswer);
            }

            if (this.correctAnswer.equals(this.userAnswer)) {
                userAnswer.setTextColor(getResources().getColor(R.color.question_bg_color_green));
            } else {
                userAnswer.setTextColor(getResources().getColor(R.color.question_bg_color_orange));
            }

            answersContainer.addView(answerItem);
        } else {
            View answerItem = inflater.inflate(R.layout.answer_short, answersContainer, false);
            EditText answerItemInput = (EditText) answerItem.findViewById(R.id.answer_input);

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
                }
            });

            answerItemInput.setOnEditorActionListener(new OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        hideKeyboard();
                        if (!userAnswer.isEmpty()) {
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    callBack.onAnswerSelected(idOnTest, userAnswer);
                                }

                            }, 200);
                        }
                        return true;
                    }
                    return false;
                }
            });

            answersContainer.addView(answerItem);
        }
        return v;
    }

    private View createQuestionHeaderView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question_header, container, false);

        TextView questionId = (TextView) v.findViewById(R.id.test_question_id);
        TextView questionTaskAll = (TextView) v.findViewById(R.id.test_question_task_all);

        questionId.setText(getResources().getString(R.string.question) + " " + id);
        questionTaskAll.setText(id + "/" + taskAll);

        return v;
    }

    private View createQuestionText(LayoutInflater inflater, ViewGroup container) {
        TextView questionText = (TextView) inflater.inflate(R.layout.test_question_text, container, false);
        questionText.setText(Html.fromHtml(question, imgGetter, null));
        questionText.setMovementMethod(LinkMovementMethod.getInstance());

        return questionText;
    }

    private View createStatementQuestionText(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.test_question_statement, container, false);

        TextView questionText = (TextView) v.findViewById(R.id.test_question_statement_text);
        questionText.setText(Html.fromHtml(question + getResources().getString(R.string.choose_ball), imgGetter, null));
        questionText.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView ballsText = (TextView) v.findViewById(R.id.test_question_statement_balls);
        ballsText.setText(getResources().getString(R.string.choosed_ball) + " " + String.valueOf(balls / 2));

        SeekBar ballsSeekBar = (SeekBar) v.findViewById(R.id.question_statement_balls_seekbar);
        ballsSeekBar.setMax(balls);
        ballsSeekBar.setProgress(balls / 2);
        ballsSeekBar.setSecondaryProgress(0);
        ballsSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userAnswer = String.valueOf(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ballsText.setText(getResources().getString(R.string.choosed_ball) + " " + String.valueOf(progress));
            }
        });

        return v;
    }

    private View createParentQuestionText(LayoutInflater inflater, ViewGroup container) {
        final TextView questionText = (TextView) inflater.inflate(R.layout.test_question_text, container, false);
        questionText.setText(Html.fromHtml(getResources().getString(R.string.parent_question_text_show), imgGetter, null));
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
                    questionText.setText(Html.fromHtml(getResources().getString(R.string.parent_question_text_show), imgGetter, null));
                }
            }
        });

        return questionText;
    }

    private LinearLayout createAnswersContainer(LayoutInflater inflater, ViewGroup container) {
        return (LinearLayout) inflater.inflate(R.layout.test_question_answers, container, false);
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
                drawable = getResources().getDrawable(R.drawable.emo_im_crying);
            }

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

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

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}