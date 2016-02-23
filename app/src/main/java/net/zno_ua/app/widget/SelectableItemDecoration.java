package net.zno_ua.app.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class SelectableItemDecoration extends RecyclerView.ItemDecoration {

    private DecoratorSelector mDecoratorSelector;

    public DecoratorSelector getDecoratorSelector() {
        return mDecoratorSelector;
    }

    public void setDecoratorSelector(DecoratorSelector decoratorSelector) {
        mDecoratorSelector = decoratorSelector;
    }

    public boolean isDecorated(View view, RecyclerView parent) {
        return mDecoratorSelector == null
                || mDecoratorSelector.isDecorated(parent.getChildAdapterPosition(view), view, parent);

    }

    public interface DecoratorSelector {
        boolean isDecorated(int position, View view, RecyclerView parent);
    }
}
