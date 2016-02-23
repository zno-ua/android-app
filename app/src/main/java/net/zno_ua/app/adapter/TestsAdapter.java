package net.zno_ua.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.view.SectionViewHolder;
import net.zno_ua.app.view.TestItemVewHolder;
import net.zno_ua.app.widget.SelectableItemDecoration;

import java.util.HashMap;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.Query.Test.Column;

public class TestsAdapter extends SectionCursorRecyclerViewAdapter<String>
        implements SelectableItemDecoration.DecoratorSelector {

    private final TestItemVewHolder.OnTestItemClickListener mOnTestItemClickListener;
    private final LayoutInflater mLayoutInflater;

    public TestsAdapter(Context context, @NonNull TestItemVewHolder.OnTestItemClickListener listener) {
        super(null, null);
        mLayoutInflater = LayoutInflater.from(context);
        mOnTestItemClickListener = listener;
    }

    @Override
    protected HashMap<Integer, String> createSections(Cursor cursor) {
        final HashMap<Integer, String> sections = new HashMap<>();
        String year;
        int position = 0;

        if (cursor.moveToFirst()) {
            do {
                year = valueOf(cursor.getInt(Column.YEAR));
                if (!sections.containsValue(year)) {
                    sections.put(position, year);
                    position++;
                }
                position++;
            } while (cursor.moveToNext());
        }

        return sections;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, Cursor cursor, int position) {
        ((TestItemVewHolder) holder).bind(cursor);
    }

    @Override
    public void onBindSectionViewHolder(RecyclerView.ViewHolder holder, String section, int position) {
        ((SectionViewHolder) holder).bind(section);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                return new TestItemVewHolder(mLayoutInflater, parent, mOnTestItemClickListener);
            case TYPE_SECTION:
                return new SectionViewHolder(mLayoutInflater, parent);
            default:
                throw new IllegalArgumentException("Invalid viewType " + viewType);
        }
    }

    @Override
    public boolean isDecorated(int position, View view, RecyclerView parent) {
        return isItem(position) && position < getItemCount() - 1 && isSection(position + 1);
    }
}
