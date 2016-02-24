package net.zno_ua.app.viewholder.question;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.StatementAnswer;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * @author vojkovladimir.
 */
public class StatementAnswerVH extends QuestionItemVH<StatementAnswer>
        implements DiscreteSeekBar.OnProgressChangeListener {
    private final String mChosenPointFormat;
    private final DiscreteSeekBar mSeekBar;
    private final TextView mChosenPoint;
    @Nullable
    private final OnAnswerChangeListener mOnAnswerChangeListener;

    public StatementAnswerVH(LayoutInflater layoutInflater, ViewGroup parent,
                             @Nullable OnAnswerChangeListener listener) {
        super(layoutInflater.inflate(R.layout.view_statement_answer_item, parent, false));
        mSeekBar = (DiscreteSeekBar) itemView.findViewById(R.id.seek_bar);
        mChosenPoint = (TextView) itemView.findViewById(R.id.chosen_point);
        mChosenPointFormat = itemView.getContext().getString(R.string.chosen_point_format);
        mSeekBar.setOnProgressChangeListener(this);
        mOnAnswerChangeListener = listener;
    }

    public void bind(@NonNull StatementAnswer item) {
        mSeekBar.setMax(item.getMaxPoint());
        mSeekBar.setProgress(item.getPoint());
    }

    private void setChosenPoint(Object value) {
        mChosenPoint.setText(format(mChosenPointFormat, valueOf(value)));
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
        if (mOnAnswerChangeListener != null) {
            final String answer = String.valueOf(seekBar.getProgress());
            mOnAnswerChangeListener.onAnswerChanged(getAdapterPosition(), answer);
        }
    }

}
