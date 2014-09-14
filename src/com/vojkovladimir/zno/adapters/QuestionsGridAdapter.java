package com.vojkovladimir.zno.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

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
            convertView = inflater.inflate(R.layout.test_questions_gride_item, parent, false);
            holder = new ViewHolder();
            holder.questionNum = (TextView) convertView.findViewById(R.id.test_questions_gride_item_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Question question = test.questions.get(position);

        if (question.userAnswer.isEmpty() || (question.type == Question.TYPE_3 && question.userAnswer.contains("0")) || viewMode) {
            convertView.setBackgroundResource(R.drawable.item_background_unselected);
            holder.questionNum.setTextColor(resources.getColorStateList(R.color.item_text_color));
        } else {
            convertView.setBackgroundResource(R.drawable.item_background_selected);
            holder.questionNum.setTextColor(resources.getColorStateList(R.color.item_text_color_selected));
        }

        holder.questionNum.setText(String.valueOf(position + 1));

        return convertView;
    }

}