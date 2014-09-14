package com.vojkovladimir.zno.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Test;

public class QuestionsAdapter extends FragmentStatePagerAdapter {

    Context context;
    Test test;
    boolean viewMode;

    public QuestionsAdapter(Context context, FragmentManager fm, Test test, boolean viewMode) {
        super(fm);
        this.context = context;
        this.test = test;
        this.viewMode = viewMode;
    }

    @Override
    public Fragment getItem(int position) {
        return QuestionFragment.newInstance(viewMode, position, test.questions.get(position), test.taskAll, test.lessonId);
    }

    @Override
    public int getCount() {
        return test.questions.size();
    }

}