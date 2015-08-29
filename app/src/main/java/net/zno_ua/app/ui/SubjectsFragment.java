package net.zno_ua.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import net.zno_ua.app.R;
import net.zno_ua.app.adapter.CursorRecyclerViewAdapter;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.ui.picasso.transformation.Rounded;
import net.zno_ua.app.util.UiUtils;

import static android.view.LayoutInflater.from;

public class SubjectsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SPAN_COUNT = 2;
    private static final String[] PROJECTION = new String[]{
            ZNOContract.Subject._ID,
            ZNOContract.Subject.NAME
    };
    private static final int ID_COLUMN_INDEX = 0;
    private static final int NAME_COLUMN_INDEX = 1;

    private CursorRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnSubjectSelectedListener mSubjectSelectedListener;

    public SubjectsFragment() {
    }

    public static SubjectsFragment newInstance() {
        return new SubjectsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.subjects_list);
        mRecyclerView.setLayoutManager(
                new GridLayoutManager(getActivity(), SPAN_COUNT, GridLayoutManager.VERTICAL, false)
        );

        mAdapter = new SubjectsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSubjectSelectedListener = (OnSubjectSelectedListener)  activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ZNOContract.Subject.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ZNOContract.Subject.POSITION + ZNOContract.ASC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private static class SubjectVH extends RecyclerView.ViewHolder {

        CardView card;
        ImageView image;
        TextView name;

        public SubjectVH(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.card_view);
            image = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private class SubjectsAdapter extends CursorRecyclerViewAdapter<SubjectVH>
            implements View.OnClickListener {
        final Transformation roundedTransformation;

        public SubjectsAdapter() {
            roundedTransformation = new Rounded(
                    getResources().getDimensionPixelOffset(R.dimen.card_view_corner_radius),
                    Rounded.Corners.TOP
            );
        }

        @Override
        public void onBindViewHolder(SubjectVH viewHolder, Cursor cursor) {
            int id = cursor.getInt(ID_COLUMN_INDEX);

            viewHolder.name.setText(cursor.getString(NAME_COLUMN_INDEX));
            viewHolder.card.setCardBackgroundColor(
                    ContextCompat.getColor(getActivity(), UiUtils.SUBJECT_COLOR_RES_ID[id])
            );

            int imageResID = UiUtils.SUBJECT_IMAGE_RES_ID[id];

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.image.setBackgroundResource(imageResID);
            } else {
                viewHolder.card.setPreventCornerOverlap(false);

                Picasso.with(getActivity())
                        .load(imageResID)
                        .fit()
                        .centerCrop()
                        .transform(roundedTransformation)
                        .into(viewHolder.image);
            }

        }

        @Override
        public SubjectVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = from(getActivity()).inflate(R.layout.subject_item, parent, false);
            itemView.setOnClickListener(this);

            return new SubjectVH(itemView);
        }

        @Override
        public void onClick(View v) {
            mSubjectSelectedListener.onSubjectSelected(
                    getItemId(mRecyclerView.getChildAdapterPosition(v))
            );
        }
    }

    /**
     * Interface to be invoked when subject is selected.
     */
    public interface OnSubjectSelectedListener {
        /**
         * @param id unique id od the row of the subject in the database.
         */
        void onSubjectSelected(long id);
    }
}
