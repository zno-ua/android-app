package net.zno_ua.app.viewholder;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author vojkovladimir.
 */
public abstract class CursorViewHolder extends RecyclerView.ViewHolder {

    public CursorViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(Cursor cursor);
}
