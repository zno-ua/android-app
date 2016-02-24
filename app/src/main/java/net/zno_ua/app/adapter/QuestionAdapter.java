package net.zno_ua.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.zno_ua.app.model.question.ChoicesAnswers;
import net.zno_ua.app.model.question.EditableAnswer;
import net.zno_ua.app.model.question.EditableAnswerVm;
import net.zno_ua.app.model.question.OneAnswer;
import net.zno_ua.app.model.question.QuestionItem;
import net.zno_ua.app.model.question.QuestionText;
import net.zno_ua.app.model.question.StatementAnswer;
import net.zno_ua.app.model.question.StatementAnswerVm;
import net.zno_ua.app.model.question.UnAnsweredQuestionPrompt;
import net.zno_ua.app.viewholder.question.ChoicesAnswersVH;
import net.zno_ua.app.viewholder.question.EditableAnswerVH;
import net.zno_ua.app.viewholder.question.EditableAnswerVmVH;
import net.zno_ua.app.viewholder.question.OnAnswerChangeListener;
import net.zno_ua.app.viewholder.question.OneAnswerVH;
import net.zno_ua.app.viewholder.question.QuestionItemVH;
import net.zno_ua.app.viewholder.question.QuestionTextVH;
import net.zno_ua.app.viewholder.question.StatementAnswerVH;
import net.zno_ua.app.viewholder.question.StatementAnswerVmVH;
import net.zno_ua.app.viewholder.question.UnAnsweredQuestionItemVH;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vojkovladimir.
 */
public class QuestionAdapter extends RecyclerView.Adapter<QuestionItemVH> {
    private static final int VIEW_TYPE_TEXT = 0x0;
    private static final int VIEW_TYPE_ONE_ANSWER = 0x1;
    private static final int VIEW_TYPE_EDITABLE_ANSWER = 0x2;
    private static final int VIEW_TYPE_STATEMENT_ANSWER = 0x5;
    private static final int VIEW_TYPE_CHOICES_ANSWERS = 0x6;
    private static final int VIEW_TYPE_UNANSWERED_QUESTION = 0x7;
    private static final int VIEW_TYPE_EDITABLE_ANSWER_VM = 0x8;
    private static final int VIEW_TYPE_STATEMENT_ANSWER_VM = 0x9;

    private final LayoutInflater mInflater;
    private final List<QuestionItem> mItems;
    private ChoicesAnswersVH.OnAnswerChoiceSelectListener mChoiceSelectListener = null;
    private OneAnswerVH.OnOneAnswerSelectListener mOnOneAnswerSelectListener = null;
    private OnAnswerChangeListener mOnAnswerChangeListener = null;

    public QuestionAdapter(Context context, List<QuestionItem> items) {
        mInflater = LayoutInflater.from(context);
        mItems = new ArrayList<>();
        mItems.addAll(items);
    }

    public void setOnOneAnswerSelectListener(OneAnswerVH.OnOneAnswerSelectListener listener) {
        mOnOneAnswerSelectListener = listener;
    }

    public void setChoiceSelectListener(ChoicesAnswersVH.OnAnswerChoiceSelectListener listener) {
        mChoiceSelectListener = listener;
    }

    public void setOnAnswerChangeListener(OnAnswerChangeListener listener) {
        mOnAnswerChangeListener = listener;
    }

    @Override
    public QuestionItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TEXT:
                return new QuestionTextVH(mInflater, parent);
            case VIEW_TYPE_ONE_ANSWER:
                return new OneAnswerVH(mInflater, parent, mOnOneAnswerSelectListener);
            case VIEW_TYPE_EDITABLE_ANSWER:
                return new EditableAnswerVH(mInflater, parent, mOnAnswerChangeListener);
            case VIEW_TYPE_EDITABLE_ANSWER_VM:
                return new EditableAnswerVmVH(mInflater, parent);
            case VIEW_TYPE_STATEMENT_ANSWER:
                return new StatementAnswerVH(mInflater, parent, mOnAnswerChangeListener);
            case VIEW_TYPE_STATEMENT_ANSWER_VM:
                return new StatementAnswerVmVH(mInflater, parent);
            case VIEW_TYPE_CHOICES_ANSWERS:
                return new ChoicesAnswersVH(mInflater, parent, mChoiceSelectListener);
            case VIEW_TYPE_UNANSWERED_QUESTION:
                return new UnAnsweredQuestionItemVH(mInflater, parent);
            default:
                throw new RuntimeException("Illegal view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(QuestionItemVH holder, int position) {
        //noinspection unchecked
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        final QuestionItem item = mItems.get(position);
        if (item instanceof QuestionText) {
            return VIEW_TYPE_TEXT;
        } else if (item instanceof OneAnswer) {
            return VIEW_TYPE_ONE_ANSWER;
        } else if (item instanceof EditableAnswer) {
            return VIEW_TYPE_EDITABLE_ANSWER;
        } else if (item instanceof StatementAnswerVm) {
            return VIEW_TYPE_STATEMENT_ANSWER_VM;
        } else if (item instanceof StatementAnswer) {
            return VIEW_TYPE_STATEMENT_ANSWER;
        } else if (item instanceof ChoicesAnswers) {
            return VIEW_TYPE_CHOICES_ANSWERS;
        } else if (item instanceof UnAnsweredQuestionPrompt) {
            return VIEW_TYPE_UNANSWERED_QUESTION;
        } else if (item instanceof EditableAnswerVm) {
            return VIEW_TYPE_EDITABLE_ANSWER_VM;
        }
        throw new RuntimeException("Unknown item type.");
    }

    public List<QuestionItem> getItems() {
        return mItems;
    }

    public QuestionItem getItem(int position) {
        return position < 0 || position > mItems.size() - 1 ? null : mItems.get(position);
    }
}
