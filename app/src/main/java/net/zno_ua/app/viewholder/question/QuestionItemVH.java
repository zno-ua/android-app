package net.zno_ua.app.viewholder.question;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.zno_ua.app.model.question.QuestionItem;

/**
 * @author vojkovladimir.
 */
public abstract class QuestionItemVH<T extends QuestionItem> extends RecyclerView.ViewHolder {

    public QuestionItemVH(View itemView) {
        super(itemView);
    }

    public abstract void bind(T item);
}
