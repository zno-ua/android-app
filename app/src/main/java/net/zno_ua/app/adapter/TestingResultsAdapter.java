package net.zno_ua.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.view.SectionViewHolder;
import net.zno_ua.app.view.TestingResultItemVewHolder;
import net.zno_ua.app.widget.SelectableItemDecoration;

import java.util.HashMap;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.Query.TestingResult.Column;

/**
 * @author vojkovaldimir
 */
public class TestingResultsAdapter extends SectionCursorRecyclerViewAdapter<String>
        implements SelectableItemDecoration.DecoratorSelector {

    private final LayoutInflater mInflater;
    private final TestingResultItemVewHolder.OnTestingItemClickListener mClickListener;

    public TestingResultsAdapter(Context context,
                                 TestingResultItemVewHolder.OnTestingItemClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mClickListener = listener;
    }

    @Override
    protected HashMap<Integer, String> createSections(@NonNull Cursor cursor) {
        final HashMap<Integer, String> sections = new HashMap<>();
        String year;
        int position = 0;

        if (cursor.moveToFirst()) {
            do {
                year = valueOf(cursor.getString(Column.SUBJECT_NAME));
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
        ((TestingResultItemVewHolder) holder).bind(cursor);
    }

    @Override
    public void onBindSectionViewHolder(RecyclerView.ViewHolder holder, String section, int position) {
        ((SectionViewHolder) holder).bind(section);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                return new TestingResultItemVewHolder(mInflater, parent, mClickListener);
            case VIEW_TYPE_SECTION_ITEM:
                return new SectionViewHolder(mInflater, parent);
            default:
                throw new IllegalArgumentException("Invalid viewType " + viewType);
        }
    }

    @Override
    public boolean isDecorated(int position, View view, RecyclerView parent) {
        return getItemViewType(position) == VIEW_TYPE_ITEM && position < getItemCount() - 1
                && getItemViewType(position + 1) == VIEW_TYPE_SECTION_ITEM;
    }
}
