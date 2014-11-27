package net.zno_ua.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.models.Question;
import net.zno_ua.app.models.Test;

public class QuestionsGridAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView questionNum;
    }

    private LayoutInflater inflater;
    private Resources resources;

    private Test test;
    private boolean viewMode;

    public QuestionsGridAdapter(Context context, Test test, boolean viewMode) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = context.getResources();
        this.test = test;
        this.viewMode = viewMode;
    }

    @Override
    public int getCount() {
        return test.questions.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.test_grid_question, parent, false);
            holder = new ViewHolder();
            holder.questionNum = (TextView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Question question = test.questions.get(position);

        if (position == test.questions.size() - 1 && question.type == Question.TYPE_2) {
            holder.questionNum.setText(resources.getString(R.string.statement_text_grid_item));
        } else {
            holder.questionNum.setText(String.valueOf(position + 1));
        }

        if (!viewMode && question.isAnswered() || viewMode && question.isCorrect()) {
            convertView.setBackgroundResource(R.drawable.bg_blue_white);
            holder.questionNum.setTextColor(resources.getColorStateList(R.color.text_color_white_gray));
        } else {
            convertView.setBackgroundResource(R.drawable.bg_white_blue);
            holder.questionNum.setTextColor(resources.getColorStateList(R.color.text_color_gray_white));
        }

        return convertView;
    }

}