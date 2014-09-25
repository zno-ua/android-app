package com.vojkovladimir.zno.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.models.TestInfo;

public class TestsListAdapter extends BaseAdapter {

	private final String ZNO_FULL;
	private final String ZNO_LIGHT;
	private final String ZNO;
	private final String EXP_ZNO;
	private final String FOR_YEAR;
	private final String YEAR;
	private final String SESSION;
	private final String TASK_TEXT;
	private final String TASKS_TEXT;
	private final String NEEDED_TO_LOAD;

	private LayoutInflater lInflater;
	private ArrayList<TestInfo> testsList;

	static class ViewHolder {
		public TextView testName;
		public TextView testProperties;
		public View downloadFrame;
	}

	public TestsListAdapter(Context context, ArrayList<TestInfo> testsList) {
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.testsList = testsList;

		ZNO_FULL = context.getResources().getString(R.string.check_zno_full);
		ZNO_LIGHT = context.getResources().getString(R.string.check_zno_lite);
		ZNO = context.getResources().getString(R.string.zno);
		EXP_ZNO = context.getResources().getString(R.string.exp_zno);
		FOR_YEAR = context.getResources().getString(R.string.for_year);
		YEAR = context.getResources().getString(R.string.year);
		SESSION = context.getResources().getString(R.string.session_text);
		TASK_TEXT = context.getResources().getString(R.string.task_text);
		TASKS_TEXT = context.getResources().getString(R.string.tasks_text);
		NEEDED_TO_LOAD = context.getResources().getString(
				R.string.needed_to_load_text);
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
			testItem = lInflater.inflate(R.layout.test, parent,
					false);
			ViewHolder viewHolder = new ViewHolder();

			viewHolder.testName = (TextView) testItem
					.findViewById(R.id.test_name);
			viewHolder.testProperties = (TextView) testItem
					.findViewById(R.id.test_properties);
			viewHolder.downloadFrame = (View) testItem
					.findViewById(R.id.test_download_image);
			testItem.setTag(viewHolder);
		}

		ViewHolder viewHolder = (ViewHolder) testItem.getTag();

		TestInfo testInfo = testsList.get(position);

		String testName = "";
		String testProperties = "";

		if (testInfo.name.contains(ZNO_FULL)
				|| testInfo.name.contains(ZNO_LIGHT)) {
			testName = ZNO;
		} else {
			testName = EXP_ZNO;
		}

		testName += " " + FOR_YEAR + " " + testInfo.year + " " + YEAR;

		if (testInfo.name.contains("(I " + SESSION + ")")) {
			testProperties = "I " + SESSION + ", ";
		} else if (testInfo.name.contains("(II " + SESSION + ")")) {
			testProperties = "II " + SESSION + ", ";
		}

		if (testInfo.loaded) {
			testProperties += testInfo.taskAll + " ";
			switch (testInfo.taskAll % 10) {
			case 1:
			case 2:
			case 3:
			case 4:
				testProperties += TASK_TEXT;
				break;
			default:
				testProperties += TASKS_TEXT;
			}
			viewHolder.downloadFrame.setVisibility(View.GONE);
		} else {
			testProperties += NEEDED_TO_LOAD;
			viewHolder.downloadFrame.setVisibility(View.VISIBLE);
		}

		viewHolder.testName.setText(testName);
		viewHolder.testProperties.setText(testProperties);

		return testItem;
	}

	public ArrayList<TestInfo> getTestsList() {
		return testsList;
	}

	public void setTestsList(ArrayList<TestInfo> testsList) {
		this.testsList = testsList;
	}

}
