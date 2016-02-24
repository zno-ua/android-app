package net.zno_ua.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.zno_ua.app.viewholder.SubjectViewHolder;

public class SubjectsAdapter extends CursorRecyclerViewAdapter<SubjectViewHolder> {

    private final SubjectViewHolder.OnSubjectClickListener mOnSubjectClickListener;
    private final LayoutInflater mLayoutInflater;

    public SubjectsAdapter(Context context,
                           @NonNull SubjectViewHolder.OnSubjectClickListener listener) {
        mLayoutInflater = LayoutInflater.from(context);
        mOnSubjectClickListener = listener;
    }

    @Override
    public void onBindViewHolder(SubjectViewHolder viewHolder, Cursor cursor) {
        viewHolder.bind(cursor);
    }

    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SubjectViewHolder(mLayoutInflater, parent, mOnSubjectClickListener);
    }
}
