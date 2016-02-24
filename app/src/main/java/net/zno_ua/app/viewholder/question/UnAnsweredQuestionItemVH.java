package net.zno_ua.app.viewholder.question;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.QuestionItem;

/**
 * @author vojkovladimir.
 */
public class UnAnsweredQuestionItemVH extends QuestionItemVH {


    public UnAnsweredQuestionItemVH(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.view_unanswered_question_item, parent, false));
    }

    @Override
    public void bind(@NonNull QuestionItem item) {

    }

}