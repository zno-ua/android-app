package net.zno_ua.app.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Observable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Vojko Vladimir
 */
public class RecyclerLinearLayout extends LinearLayout {

    public static final int NO_POSITION = -1;

    private SparseArray<Stack<ViewHolder>> mCachedViews;
    private ArrayList<ViewHolder> mHolders = new ArrayList<>();
    private Adapter<ViewHolder> mAdapter;
    private RecyclerLinearLayoutDataObserver mObserver = new RecyclerLinearLayoutDataObserver();

    public RecyclerLinearLayout(Context context) {
        super(context);
        init();
    }

    public RecyclerLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecyclerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mCachedViews = new SparseArray<>();
    }

    public void setAdapter(Adapter<ViewHolder> adapter) {
        if (mAdapter == adapter)
            return;
        if (mAdapter != null) {
            mAdapter.onDetachedFromLayout(this);
            mAdapter.unregisterAdapterDataObserver(mObserver);
        }
        mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
            adapter.onAttachedToLayout(this);
        }
        prepareCashedViews();
        recycle();
    }

    private void prepareCashedViews() {
        mCachedViews.clear();
        if (mAdapter != null) {
            Stack<ViewHolder> viewHolders;
            for (int viewType : mAdapter.getItemViewTypes()) {
                viewHolders = new Stack<>();
                for (int j = 0; j < mAdapter.getCachedViewHoldersCapacity(); j++) {
                    viewHolders.add(mAdapter.createViewHolder(this, viewType));
                }
                mCachedViews.put(viewType, viewHolders);
            }
        }
    }

    public Adapter<ViewHolder> getAdapter() {
        return mAdapter;
    }

    private void recycle() {
        if (mAdapter == null) {
            mHolders.clear();
            removeAllViews();
        }
        if (mHolders.size() != mAdapter.getItemCount()) {
            if (mHolders.size() < mAdapter.getItemCount()) {
                for (int i = mHolders.size(); i < mAdapter.getItemCount(); i++) {
                    mHolders.add(getViewHolder(mAdapter.getItemViewType(i)));
                    addView(mHolders.get(i).mItemView, i);
                }
            }

            if (mHolders.size() > mAdapter.getItemCount()) {
                for (int i = mHolders.size() - 1; i > mAdapter.getItemCount() - 1; i--) {
                    cacheViewHolder(mHolders.remove(i));
                    removeViewAt(i);
                }
            }
        }

        int itemViewType;
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            itemViewType = mAdapter.getItemViewType(i);
            if (mHolders.get(i).mItemViewType != itemViewType) {
                cacheViewHolder(mHolders.get(i));
                removeViewAt(i);
                mHolders.set(i, getViewHolder(itemViewType));
                addView(mHolders.get(i).mItemView, i);
            }

            mAdapter.bindViewHolder(mHolders.get(i), i);
        }
    }

    private ViewHolder getViewHolder(int viewType) {
        Stack<ViewHolder> holders = mCachedViews.get(viewType);
        if (holders.size() > 0)
            return holders.pop();

        return mAdapter.createViewHolder(this, viewType);
    }

    private void cacheViewHolder(ViewHolder holder) {
        mCachedViews.get(holder.getItemViewType()).push(holder);
    }

    public ViewHolder getChildViewHolder(int position) {
        if (position < 0 || position >= mHolders.size())
            return null;

        return mHolders.get(position);
    }

    public int getChildAdapterPosition(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        if (holder != null)
            return holder.mPosition;

        return NO_POSITION;
    }

    private class RecyclerLinearLayoutDataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            recycle();
        }
    }

    public static abstract class AdapterDataObserver {

        public void onChanged() {
            // Do nothing
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }

    }

    public static abstract class ViewHolder {
        int mItemViewType;
        public final View mItemView;
        int mPosition = NO_POSITION;

        public ViewHolder(View itemView) {
            mItemView = itemView;
        }

        public int getItemViewType() {
            return mItemViewType;
        }
    }

    public static abstract class Adapter<VH extends RecyclerLinearLayout.ViewHolder> {

        private static final int CACHED_VIEW_HOLDERS_CAPACITY = 0;

        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        public abstract int getItemCount();

        public int getItemViewType(int position) {
            return 0;
        }

        private VH createViewHolder(ViewGroup parent, int viewType) {
            VH holder = onCreateViewHolder(parent, viewType);
            holder.mItemViewType = viewType;
            return holder;
        }

        public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

        public void bindViewHolder(VH holder, int position) {
            holder.mPosition = position;
            holder.mItemView.setTag(holder);
            onBindViewHolder(holder, position);
        }

        public abstract void onBindViewHolder(VH viewHolder, int position);

        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        public void unregisterAdapterDataObserver(RecyclerLinearLayoutDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        public void registerAdapterDataObserver(RecyclerLinearLayoutDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        protected void onAttachedToLayout(RecyclerLinearLayout layout) {
            // noop
        }

        protected void onDetachedFromLayout(RecyclerLinearLayout layout) {
            // noop
        }

        public abstract int[] getItemViewTypes();

        private int getCachedViewHoldersCapacity() {
            return CACHED_VIEW_HOLDERS_CAPACITY;
        }
    }
}
