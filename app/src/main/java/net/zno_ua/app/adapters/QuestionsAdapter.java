package net.zno_ua.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.zno_ua.app.fragments.QuestionFragment;
import net.zno_ua.app.models.Record;
import net.zno_ua.app.models.Test;

public class QuestionsAdapter extends FragmentStatePagerAdapter {

    Context context;
    Test test;
    Record results;
    boolean viewMode;

    public QuestionsAdapter(Context context, FragmentManager fm, Test test) {
        super(fm);
        this.context = context;
        this.test = test;
        this.viewMode = false;
    }

    public QuestionsAdapter(Context context, FragmentManager fm, Test test, Record results) {
        super(fm);
        this.context = context;
        this.test = test;
        this.viewMode = true;
        this.results = results;
    }

    @Override
    public Fragment getItem(int position) {
        if (viewMode && position == 0) {
            return  QuestionFragment.newInstance(
                    true,
                    position,
                    test.questions.get(position),
                    test.taskAll, test.lessonId,
                    results);
        } else {
            return  QuestionFragment.newInstance(
                    viewMode,
                    position,
                    test.questions.get(position),
                    test.taskAll, test.lessonId);
        }
    }

    @Override
    public int getCount() {
        return test.questions.size();
    }

}