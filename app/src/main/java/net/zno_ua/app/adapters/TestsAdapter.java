package net.zno_ua.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.models.TestInfo;

import java.util.ArrayList;

public class TestsAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private ArrayList<TestInfo> testsList;

    static class ViewHolder {
        public TextView testName;
        public TextView testProperties;
        public View downloadFrame;
    }

    public TestsAdapter(Context context, ArrayList<TestInfo> testsList) {
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.testsList = testsList;
    }

    @Override
    public int getCount() {
        return testsList.size();
    }

    @Override
    public Object getItem(int position) {
        return testsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View testItem = convertView;

        if (testItem == null) {
            testItem = lInflater.inflate(R.layout.test, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.testName = (TextView) testItem.findViewById(R.id.test_name);
            viewHolder.testProperties = (TextView) testItem.findViewById(R.id.year_and_session);
            viewHolder.downloadFrame = testItem.findViewById(R.id.test_download_image);
            testItem.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) testItem.getTag();

        TestInfo testInfo = testsList.get(position);

        if (testInfo.loaded) {
            viewHolder.downloadFrame.setVisibility(View.GONE);
        } else {
            viewHolder.downloadFrame.setVisibility(View.VISIBLE);
        }

        viewHolder.testName.setText(testInfo.nameShort);
        viewHolder.testProperties.setText(testInfo.properties);
        viewHolder.testName.setSelected(true);
        viewHolder.testProperties.setSelected(true);

        return testItem;
    }

    public void setTestsList(ArrayList<TestInfo> testsList) {
        this.testsList = testsList;
    }

    public int getTestPosition(int testId) {
        for (int i = 0; i < testsList.size(); i++) {
            if (testsList.get(i).id == testId) {
                return i;
            }
        }
        return -1;
    }

}
