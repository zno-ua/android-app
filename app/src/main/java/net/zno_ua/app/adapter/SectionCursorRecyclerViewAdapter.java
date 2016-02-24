package net.zno_ua.app.adapter;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vojko Vladimir
 */
public abstract class SectionCursorRecyclerViewAdapter<S>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String ROW_ID_COLUMN_NAME = BaseColumns._ID;

    public static final int VIEW_TYPE_ITEM = 0x0;
    public static final int VIEW_TYPE_SECTION_ITEM = 0x1;
    private static final int NO_POSITION = -1;

    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;
    private DataSetObserver mDataSetObserver;

    private final Map<Integer, S> mSections;
    private final ItemsAdapter mItemsAdapter;

    public SectionCursorRecyclerViewAdapter() {
        mCursor = null;
        mDataValid = false;
        mRowIDColumn = -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        mSections = new HashMap<>();
        mItemsAdapter = new ItemsAdapter();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;

        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor == null) {
            mRowIDColumn = -1;
            mDataValid = false;
        } else {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow(ROW_ID_COLUMN_NAME);
            mDataValid = true;
        }
        mSections.clear();
        if (mDataValid) {
            final Map<Integer, S> sections = createSections(mCursor);
            if (sections != null) {
                mSections.putAll(sections);
            }
            mItemsAdapter.setData(mCursor, mSections);
        }
        notifyDataSetChanged();
        return oldCursor;
    }

    @Nullable
    protected abstract HashMap<Integer, S> createSections(@NonNull Cursor cursor);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            if (mDataValid && mCursor.moveToPosition(mItemsAdapter.getItemPosition(position))) {
                onBindItemViewHolder(holder, mCursor, position);
            }
        } else {
            final int sectionPosition = mItemsAdapter.getSectionPosition(position);
            onBindSectionViewHolder(holder, mSections.get(sectionPosition), position);
        }
    }

    public abstract void onBindItemViewHolder(RecyclerView.ViewHolder holder, Cursor cursor, int position);

    public abstract void onBindSectionViewHolder(RecyclerView.ViewHolder holder, S section, int position);

    @Override
    public int getItemCount() {
        return mItemsAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            if (mDataValid && mCursor.moveToPosition(mItemsAdapter.getItemPosition(position))) {
                return mCursor.getLong(mRowIDColumn);
            }
        }

        return super.getItemId(position);
    }

    public int getItemPosition(int itemLayoutPosition) {
        return mItemsAdapter.getItemPosition(itemLayoutPosition);
    }

    @Override
    public int getItemViewType(int position) {
        return mItemsAdapter.getItemType(position);
    }

    private class ItemsAdapter {
        private final List<Integer> mItemsTypes;
        private final List<Integer> mSectionItemsPositions;
        private final SparseIntArray mItemsPositions;

        private ItemsAdapter() {
            mItemsTypes = new ArrayList<>();
            mSectionItemsPositions = new ArrayList<>();
            mItemsPositions = new SparseIntArray();
        }

        public int getItemCount() {
            return mItemsTypes.size();
        }

        public int getItemType(int position) {
            return mItemsTypes.get(position);
        }


        public int getItemPosition(int position) {
            if (hasItem(position)) {
                return mItemsPositions.get(position);
            }

            return NO_POSITION;
        }

        public int getSectionPosition(int position) {
            return mSectionItemsPositions.get(position);
        }

        public boolean hasItem(int position) {
            return position >= 0 || position < getItemCount();
        }

        public void setData(@NonNull Cursor cursor, @NonNull Map<Integer, S> sections) {
            mItemsTypes.clear();
            mItemsPositions.clear();
            mSectionItemsPositions.clear();
            final int itemsListSize = cursor.getCount() + sections.size();
            for (int sectionPosition : sections.keySet()) {
                if (sectionPosition < 0 || sectionPosition > itemsListSize - 1) {
                    throw new IllegalArgumentException("The sections shouldn't be placed outside the items list.");
                }
            }
            int currentSectionPosition = 0;
            for (int position = 0, itemPosition = 0; position < itemsListSize; position++) {
                if (sections.containsKey(position)) {
                    mItemsTypes.add(position, VIEW_TYPE_SECTION_ITEM);
                    currentSectionPosition = position;
                } else {
                    mItemsTypes.add(position, VIEW_TYPE_ITEM);
                    mItemsPositions.put(position, itemPosition);
                    itemPosition++;
                }
                mSectionItemsPositions.add(position, currentSectionPosition);
            }
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            notifyItemRangeRemoved(0, getItemCount());
        }
    }
}
