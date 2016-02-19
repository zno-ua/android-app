package net.zno_ua.app.view.question;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.StatementAnswer;

import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * @author vojkovladimir.
 */
public class StatementAnswerVmVH extends QuestionItemVH<StatementAnswer> {
    private final String mChosenPointFormat;
    private final TextView mChosenPoint;

    public StatementAnswerVmVH(LayoutInflater layoutInflater, ViewGroup parent) {
        super(layoutInflater.inflate(R.layout.view_statement_answer_vm_item, parent, false));
        mChosenPoint = (TextView) itemView.findViewById(R.id.chosen_point);
        mChosenPointFormat = itemView.getContext().getString(R.string.chosen_point_format);
    }

    public void bind(@NonNull StatementAnswer item) {
        mChosenPoint.setText(format(mChosenPointFormat, valueOf(item.getPoint())));
    }

}
