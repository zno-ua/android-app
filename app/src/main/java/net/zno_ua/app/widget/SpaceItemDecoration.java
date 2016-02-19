package net.zno_ua.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.zno_ua.app.R;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private static final int NO_SPACE = 0;
    private static final int UNDEFINED = -1;
    private static final int GRID_LAYOUT_MANAGER = 0x1;
    private static final int LINEAR_LAYOUT_MANAGER = 0x2;
    private final int mSpace;
    private boolean mShowFirstDivider = false;
    private boolean mShowLastDivider = false;
    private int mLastItemExtraSpace = NO_SPACE;
    private int mOrientation = UNDEFINED;
    private int mSpanCount = UNDEFINED;
    private int mLayoutManager = UNDEFINED;

    private static final int[] ATTRS = {
            R.attr.cardSpacing
    };

    public SpaceItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mSpace = a.getDimensionPixelOffset(0, 0);
        a.recycle();
    }

    public SpaceItemDecoration(Context context, boolean showFirstDivider, boolean showLastDivider) {
        this(context);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public SpaceItemDecoration(int spaceInPx) {
        mSpace = spaceInPx;
    }

    public SpaceItemDecoration(int spaceInPx, boolean showFirstDivider, boolean showLastDivider) {
        this(spaceInPx);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public SpaceItemDecoration(Context ctx, @DimenRes int resId) {
        mSpace = ctx.getResources().getDimensionPixelSize(resId);
    }

    public SpaceItemDecoration(Context ctx, @DimenRes int resId, boolean showFirstDivider,
                               boolean showLastDivider) {
        this(ctx, resId);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public SpaceItemDecoration(Context ctx, boolean showFirstDivider,
                               boolean showLastDivider, @DimenRes int extraSpaceResId) {
        this(ctx, showFirstDivider, showLastDivider);
        mLastItemExtraSpace = ctx.getResources().getDimensionPixelSize(extraSpaceResId);
    }

    public SpaceItemDecoration(Context ctx, @DimenRes int resId, boolean showFirstDivider,
                               boolean showLastDivider, @DimenRes int extraSpaceResId) {
        this(ctx, resId, showFirstDivider, showLastDivider);
        setLastItemExtraSpace(ctx, extraSpaceResId);
    }

    public void setLastItemExtraSpacePx(int extraSpaceInPx) {
        mLastItemExtraSpace = extraSpaceInPx;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (mSpace == 0) {
            return;
        }
        if (mLayoutManager == UNDEFINED) {
            mLayoutManager = getLayoutManager(parent);
        }
        if (mLayoutManager == GRID_LAYOUT_MANAGER) {
            if (mOrientation == -1) {
                getOrientation(parent);
            }
            if (mSpanCount == -1) {
                mSpanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            }
            final int position = parent.getChildLayoutPosition(view);
            final int count = parent.getAdapter().getItemCount();
            final int halfSpace = mSpace / 2;
            final int mod = position % mSpanCount;
            if (mShowFirstDivider && position < mSpanCount || position >= mSpanCount) {
                outRect.top = mSpace;
            }
            if (mod == 0) {
                outRect.left = mSpace;
                outRect.right = halfSpace;
            } else if (mod == mSpanCount - 1) {
                outRect.left = halfSpace;
                outRect.right = mSpace;
            } else {
                outRect.left = halfSpace;
                outRect.right = halfSpace;
            }
            if (mShowLastDivider && (count % mSpanCount == 0 && position >= count - mSpanCount
                    || position >= count - count % mSpanCount)) {
                outRect.bottom = mLastItemExtraSpace == NO_SPACE ? mSpace : mLastItemExtraSpace;
            }
        } else if (mLayoutManager == LINEAR_LAYOUT_MANAGER) {
            if (mOrientation == -1) {
                getOrientation(parent);
            }
            final int position = parent.getChildLayoutPosition(view);
            if (position == RecyclerView.NO_POSITION || (position == 0 && !mShowFirstDivider)) {
                return;
            }
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                outRect.top = mSpace;
                if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                    outRect.bottom = mLastItemExtraSpace == NO_SPACE ? mSpace : mLastItemExtraSpace;
                }
            } else {
                outRect.left = mSpace;
                if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                    outRect.right = outRect.left;
                }
            }
        }
    }

    private int getLayoutManager(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            return GRID_LAYOUT_MANAGER;
        } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            return LINEAR_LAYOUT_MANAGER;
        } else {
            throw new IllegalStateException(
                    "DividerItemDecoration can only be used with a LinearLayoutManager.");
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (mOrientation == -1) {
            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else {
                throw new IllegalStateException(
                        "DividerItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
        return mOrientation;
    }

    public void setLastItemExtraSpace(Context ctx, @DimenRes int resId) {
        mLastItemExtraSpace = ctx.getResources().getDimensionPixelSize(resId);
    }
}

