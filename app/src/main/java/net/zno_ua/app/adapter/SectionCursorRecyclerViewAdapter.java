package net.zno_ua.app.adapter;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author Vojko Vladimir
 */
public abstract class SectionCursorRecyclerViewAdapter<S>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String ROW_ID_COLUMN_NAME = BaseColumns._ID;

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SECTION = 1;

    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;
    private DataSetObserver mDataSetObserver;

    private ItemsAdapter mItemsAdapter;

    public SectionCursorRecyclerViewAdapter(HashMap<Integer, S> sections, Cursor cursor) {
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIDColumn = mDataValid ? mCursor.getColumnIndex(ROW_ID_COLUMN_NAME) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        mItemsAdapter = new ItemsAdapter(sections, mCursor);
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

        Cursor oldCursor = mCursor;

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

        if (mDataValid)
            changeItemsAdapter(new ItemsAdapter(createSections(mCursor), mCursor));
        else
            changeItemsAdapter(new ItemsAdapter(null, null));

        return oldCursor;
    }

    private void changeItemsAdapter(ItemsAdapter newItemsAdapter) {
        ItemsAdapter oldItemsAdapter = mItemsAdapter;
        mItemsAdapter = newItemsAdapter;

        if (oldItemsAdapter.hasItems()) {
            int min = min(oldItemsAdapter.getCount(), mItemsAdapter.getCount());
            int max = max(oldItemsAdapter.getCount(), mItemsAdapter.getCount());

            for (int i = 0; i < min; i++) {
                if (oldItemsAdapter.isItem(i) == mItemsAdapter.isItem(i)) {
                    notifyItemChanged(i);
                } else {
                    notifyItemRemoved(i);
                    notifyItemInserted(i);
                }
            }

            boolean isGrown = oldItemsAdapter.getCount() < mItemsAdapter.getCount();

            for (int i = min; i < max; i++) {
                if (isGrown) {
                    notifyItemInserted(i);
                } else {
                    notifyItemRemoved(i);
                }
            }

        } else if (mItemsAdapter.hasItems()) {
            notifyItemRangeInserted(0, mItemsAdapter.getCount());
        }
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    protected abstract HashMap<Integer, S> createSections(Cursor cursor);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mItemsAdapter.isItem(position)) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            int itemPosition = mItemsAdapter.getItemPosition(position);
            if (!mCursor.moveToPosition(itemPosition)) {
                throw new IllegalStateException("couldn't move cursor to position " + itemPosition);
            }
            onBindItemViewHolder(holder, mCursor, position);
        } else {
            onBindSectionViewHolder(holder, mItemsAdapter.getSection(position), position);
        }
    }

    public abstract void onBindItemViewHolder(RecyclerView.ViewHolder holder, Cursor cursor, int position);

    public abstract void onBindSectionViewHolder(RecyclerView.ViewHolder holder, S section, int position);

    @Override
    public int getItemCount() {
        return mItemsAdapter.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mItemsAdapter.isItem(position)) {
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
        return (mItemsAdapter.isItem(position)) ? TYPE_ITEM : TYPE_SECTION;
    }

    private class ItemsAdapter {
        private HashMap<Integer, Boolean> mItems;
        private HashMap<Integer, S> mSections;
        private HashMap<Integer, Integer> mItemsPositions;

        public ItemsAdapter(HashMap<Integer, S> sections, Cursor cursor) {
            mItems = new HashMap<>();
            mItemsPositions = new HashMap<>();
            mSections = sections;

            if (mSections == null) {
                mSections = new HashMap<>();
            }

            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount() + sections.size(); i++) {
                    boolean isItem = !mSections.containsKey(i);
                    mItems.put(i, isItem);
                    if (isItem) {
                        mItemsPositions.put(i, cursor.getPosition());
                        cursor.moveToNext();
                    }
                }
            } else {
                for (int i = 0; i < mSections.size(); i++) {
                    mItems.put(i, false);
                }
            }
        }

        public boolean hasItems() {
            return getCount() != 0;
        }

        public int getCount() {
            return mItems.size();
        }

        public boolean isItem(int position) {
            return mItems.get(position);
        }

        public int getItemPosition(int position) {
            if (isItem(position)) {
                return mItemsPositions.get(position);
            }

            return -1;
        }

        public S getSection(int position) {
            return mSections.get(position);
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
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
