package net.zno_ua.app.viewholder;

import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import net.zno_ua.app.R;
import net.zno_ua.app.picasso.Rounded;
import net.zno_ua.app.util.Utils;

import static android.support.v4.content.ContextCompat.getColor;

public class SubjectViewHolder extends CursorViewHolder {
    private final CardView mCardView;
    private final ImageView mIvImage;
    private final TextView mTvName;
    private final Transformation mRoundedTransformation;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnSubjectClickListener.onSubjectClicked(getItemId());
        }
    };
    private final OnSubjectClickListener mOnSubjectClickListener;

    public SubjectViewHolder(LayoutInflater inflater, ViewGroup parent,
                             @NonNull OnSubjectClickListener listener) {
        super(inflater.inflate(R.layout.view_subject_item, parent, false));
        itemView.setOnClickListener(mOnClickListener);
        mCardView = (CardView) itemView.findViewById(R.id.card_view);
        mIvImage = (ImageView) itemView.findViewById(R.id.image);
        mTvName = (TextView) itemView.findViewById(R.id.name);
        final int radius = itemView.getContext().getResources()
                .getDimensionPixelOffset(R.dimen.card_view_corner_radius);
        mRoundedTransformation = new Rounded(radius, Rounded.Corners.TOP);
        mOnSubjectClickListener = listener;
    }

    @Override
    public void bind(Cursor cursor) {
        final int id = cursor.getInt(0);
        final int bgColor = getColor(itemView.getContext(), Utils.SUBJECT_COLOR_RES_ID[id]);

        mTvName.setText(cursor.getString(1));
        mCardView.setCardBackgroundColor(bgColor);
        final RequestCreator requestCreator = Picasso.with(itemView.getContext())
                .load(Utils.SUBJECT_IMAGE_RES_ID[id]).fit().centerCrop();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            requestCreator.transform(mRoundedTransformation);
        }
        requestCreator.into(mIvImage);
    }

    public interface OnSubjectClickListener {
        void onSubjectClicked(long id);
    }
}