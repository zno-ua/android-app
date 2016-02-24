package net.zno_ua.app.view;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    private final TextView mTvText;

    public SectionViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.view_section_item, parent, false));
        mTvText = (TextView) itemView.findViewById(R.id.text);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Logs", getAdapterPosition() + " " + mTvText.getText());
            }
        });
    }

    public void bind(String sectionText) {
        mTvText.setText(sectionText);
    }

}
