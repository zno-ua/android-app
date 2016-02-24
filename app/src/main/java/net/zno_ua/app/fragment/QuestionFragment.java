package net.zno_ua.app.fragment;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.R;
import net.zno_ua.app.activity.TestingActivity;
import net.zno_ua.app.adapter.QuestionAdapter;
import net.zno_ua.app.model.TestingInfo;
import net.zno_ua.app.model.question.AnswerItem;
import net.zno_ua.app.model.question.ChoicesAnswers;
import net.zno_ua.app.model.question.EditableAnswer;
import net.zno_ua.app.model.question.EditableAnswerVm;
import net.zno_ua.app.model.question.OneAnswer;
import net.zno_ua.app.model.question.QuestionItem;
import net.zno_ua.app.model.question.QuestionText;
import net.zno_ua.app.model.question.StatementAnswer;
import net.zno_ua.app.model.question.StatementAnswerVm;
import net.zno_ua.app.model.question.UnAnsweredQuestionPrompt;
import net.zno_ua.app.provider.Query;
import net.zno_ua.app.util.Utils;
import net.zno_ua.app.viewholder.question.ChoicesAnswersVH;
import net.zno_ua.app.viewholder.question.OnAnswerChangeListener;
import net.zno_ua.app.viewholder.question.OneAnswerVH;
import net.zno_ua.app.widget.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static net.zno_ua.app.provider.Query.selectionArgs;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.Subject.ENGLISH;

public class QuestionFragment extends Fragment
        implements OneAnswerVH.OnOneAnswerSelectListener,
        ChoicesAnswersVH.OnAnswerChoiceSelectListener, OnAnswerChangeListener {
    private static final String KEY_QUESTION_ID = "KEY_QUESTION_ID";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String KEY_TEXT = "KEY_TEXT";
    private static final String KEY_ADDITIONAL_TEXT = "KEY_ADDITIONAL_TEXT";
    private static final String KEY_ANSWERS = "KEY_ANSWERS";
    private static final String KEY_POINT = "KEY_POINT";
    private static final String KEY_CORRECT_ANSWER = "KEY_CORRECT_ANSWER";

    static final String ANSWERS_COUNT_DIVIDER_REGEX = "-";
    static final String ANSWERS_DIVIDER_REGEX = "\n";

    public static QuestionFragment newInstance(long questionId, int type, String text,
                                               String additionalText, String answers, int point,
                                               String correctAnswer) {
        final Bundle args = new Bundle();
        args.putLong(KEY_QUESTION_ID, questionId);
        args.putInt(KEY_TYPE, type);
        args.putString(KEY_TEXT, text);
        args.putString(KEY_ADDITIONAL_TEXT, additionalText);
        args.putString(KEY_ANSWERS, answers);
        args.putInt(KEY_POINT, point);
        args.putString(KEY_CORRECT_ANSWER, correctAnswer);

        final QuestionFragment fragment = new QuestionFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private long mQuestionId;
    private TestingInfo mTestingInfo;
    private RecyclerView mRecyclerView;
    private QuestionAdapter mQuestionAdapter;
    private OnQuestionAnswerListener mOnQuestionAnswerListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTestingInfo = ((TestingActivity) activity).getTestingInfo();
        mOnQuestionAnswerListener = (OnQuestionAnswerListener) getParentFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuestionId = getArguments().getLong(KEY_QUESTION_ID);
        int type = getArguments().getInt(KEY_TYPE);
        final String answers = getArguments().getString(KEY_ANSWERS, "");
        final int point = getArguments().getInt(KEY_POINT);
        final String correctAnswer = getArguments().getString(KEY_CORRECT_ANSWER, "");
        final String text = getArguments().getString(KEY_TEXT);
        final String additionalText = getArguments().getString(KEY_ADDITIONAL_TEXT);
        String answer = getAnswer();

        final boolean isEnglish = mTestingInfo.getSubjectId() == ENGLISH;
        final char firstLetter = isEnglish ? 'A' : 'А';
        final boolean isEditable = !mTestingInfo.isPassed();
        final ArrayList<QuestionItem> items = new ArrayList<>();
        items.add(new QuestionText(getActivity(), text, additionalText, isEnglish));
        if (type == Question.TYPE_1) {
            if (mTestingInfo.isPassed() && TextUtils.isEmpty(answer)) {
                items.add(new UnAnsweredQuestionPrompt());
            }
            int correct = Integer.parseInt(correctAnswer) - 1;
            int selected = TextUtils.isEmpty(answer) ? -1 : (Integer.parseInt(answer) - 1);
            String[] answersText = answers.split(ANSWERS_DIVIDER_REGEX);
            OneAnswer oneAnswer;
            for (int i = 0; i < answersText.length; i++) {
                oneAnswer = new OneAnswer(answersText[i], i, correct, isEditable, firstLetter);
                oneAnswer.setSelected(i == selected);
                items.add(oneAnswer);
            }
        } else if (type == Question.TYPE_2) {
            if (mTestingInfo.isPassed()) {
                final int selectedPoint = TextUtils.isEmpty(answer) ? point / 2 : Integer.parseInt(answer);
                items.add(new StatementAnswerVm(selectedPoint));
            } else {
                final int selectedPoint = TextUtils.isEmpty(answer) ? -1 : Integer.parseInt(answer);
                items.add(new StatementAnswer(point, selectedPoint));
            }
        } else if (type == Question.TYPE_3) {
            String[] answersArray = answers.split(ANSWERS_COUNT_DIVIDER_REGEX);
            int numbersCount = Integer.parseInt(answersArray[0]);
            int lettersCount = Integer.parseInt(answersArray[1]);
            if (TextUtils.isEmpty(answer)) {
                answer = ChoicesAnswers.emptyAnswer(numbersCount);
            }
            items.add(new ChoicesAnswers(numbersCount, lettersCount, correctAnswer, answer,
                    firstLetter, !mTestingInfo.isPassed()));
        } else if (type == Question.TYPE_4 || type == Question.TYPE_5) {
            if (mTestingInfo.isPassed()) {
                if (TextUtils.isEmpty(answer)) {
                    items.add(new UnAnsweredQuestionPrompt());
                } else {
                    items.add(new EditableAnswerVm(type, correctAnswer, answer));
                }
                items.add(new EditableAnswerVm(type, correctAnswer));
            } else {
                items.add(new EditableAnswer(type, answer));
            }
        }

        mQuestionAdapter = new QuestionAdapter(getActivity(), items);
        if (isEditable) {
            mQuestionAdapter.setChoiceSelectListener(this);
            mQuestionAdapter.setOnOneAnswerSelectListener(this);
            mQuestionAdapter.setOnAnswerChangeListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final SpaceItemDecoration decoration = new SpaceItemDecoration(getActivity(), true, true);
        decoration.setLastItemExtraSpace(getActivity(), R.dimen.fab_space_size);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(mQuestionAdapter);
        Utils.disableSupportsChangeAnimations(mRecyclerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTestingInfo = null;
        mOnQuestionAnswerListener = null;
    }

    private void saveAnswer(String answer) {
        if (TextUtils.isEmpty(answer)) {
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(Answer.QUESTION_ID, mQuestionId);
        values.put(Answer.TESTING_ID, mTestingInfo.getTestingId());
        values.put(Answer.ANSWER, answer);
        int rowsUpdated = getActivity().getContentResolver().update(Answer.CONTENT_URI,
                values,
                Answer.QUESTION_ID + " = ?" + " AND " + Answer.TESTING_ID + " = ?",
                selectionArgs(mQuestionId, mTestingInfo.getTestingId())
        );
        if (rowsUpdated == 0) {
            getActivity().getContentResolver().insert(Answer.CONTENT_URI, values);
        }
    }

    private String getAnswer() {
        String answer = null;
        final Cursor cursor = getActivity().getContentResolver().query(Query.Answer.URI,
                Query.Answer.PROJECTION, Query.Answer.SELECTION,
                selectionArgs(mTestingInfo.getTestingId(), mQuestionId), null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                answer = cursor.getString(Query.Answer.Column.ANSWER);
            }
            cursor.close();
        }
        return answer;
    }

    @Override
    public void onOneAnswerSelected(int adapterPosition) {
        final List<QuestionItem> items = mQuestionAdapter.getItems();
        QuestionItem item;
        OneAnswer oneAnswer;
        boolean switchToNext = true;
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            if (item instanceof OneAnswer) {
                oneAnswer = (OneAnswer) item;
                if (oneAnswer.isSelected()) {
                    switchToNext = false;
                    if (i == adapterPosition) {
                        return;
                    } else {
                        oneAnswer.setSelected(false);
                        mQuestionAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        }
        final OneAnswer selectedAnswer = (OneAnswer) mQuestionAdapter.getItem(adapterPosition);
        selectedAnswer.setSelected(true);
        mQuestionAdapter.notifyItemChanged(adapterPosition);
        saveAnswer(String.valueOf(selectedAnswer.getPosition() + 1));
        if (switchToNext) {
            mOnQuestionAnswerListener.onQuestionAnswered();
        }
    }

    @Override
    public void onAnswerChoiceSelected(int position, int number, int letter) {
        final ChoicesAnswers choicesAnswers = (ChoicesAnswers) mQuestionAdapter.getItem(position);
        final String oldAnswer = choicesAnswers.getAnswer();
        final StringBuilder newAnswer = new StringBuilder(oldAnswer);
        int oldNumberPosition = oldAnswer.indexOf('0' + letter + 1);
        if (oldNumberPosition != -1) {
            newAnswer.setCharAt(oldNumberPosition, '0');
        }
        newAnswer.setCharAt(number, (char) ('0' + letter + 1));
        if (!TextUtils.equals(choicesAnswers.getAnswer(), newAnswer)) {
            if (!choicesAnswers.isAnswered() && !newAnswer.toString().contains("0")) {
                mOnQuestionAnswerListener.onQuestionAnswered();
            }
            choicesAnswers.setAnswer(newAnswer.toString());
            saveAnswer(newAnswer.toString());
        }
    }

    @Override
    public void onAnswerChanged(int position, String answer) {
        final AnswerItem answerItem = (AnswerItem) mQuestionAdapter.getItem(position);
        final String oldAnswer = answerItem.getAnswer();
        if (!TextUtils.equals(oldAnswer, answer)) {
            answerItem.setAnswer(answer);
            saveAnswer(answer);
        }
    }

    public interface OnQuestionAnswerListener {
        void onQuestionAnswered();
    }
}
