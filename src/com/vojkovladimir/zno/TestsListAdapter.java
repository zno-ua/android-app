package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vojkovladimir.zno.models.TestInfo;

public class TestsListAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater lInflater;
	private ArrayList<TestInfo> list;

	public TestsListAdapter(Context context, ArrayList<TestInfo> list) {
		this.context = context;
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View lessonItem = lInflater.inflate(R.layout.tests_list_item, parent,
				false);

		TextView testNameView = (TextView) lessonItem
				.findViewById(R.id.tests_list_test_name);
		TextView testPropertiesView = (TextView) lessonItem
				.findViewById(R.id.tests_list_test_properties);

		// View downloadFrame =
		// (View)lessonItem.findViewById(R.id.tests_list_download_icon);

		TestInfo testInfo = list.get(position);

		String testNameFull = testInfo.name;
		String testName = "";
		String testProperties = "";
		String session = context.getResources()
				.getString(R.string.session_text);

		if (testNameFull.contains(context.getResources().getString(
				R.string.check_zno_full))
				|| testNameFull.contains(context.getResources().getString(
						R.string.check_zno_lite))) {
			testName = context.getResources().getString(R.string.zno);
		} else {
			testName = context.getResources().getString(R.string.exp_zno);
		}

		testName += " " + context.getResources().getString(R.string.for_year)
				+ " " + testInfo.year + " "
				+ context.getResources().getString(R.string.year);

		if (testNameFull.contains("(I" + session + ")")) {
			testProperties = "I" + session + ", ";
		}
		
		if (testNameFull.contains("(II" + session + ")")) {
			testProperties = "II" + session + ", ";
		}
		
		if(testInfo.tasksNum<0){
			testProperties += context.getResources().getString(R.string.needed_to_load_text);
		}else{
			testProperties += testInfo.tasksNum + " "+context.getResources().getString(R.string.tasks_text);
		}
 
		testNameView.setText(testName);
		testPropertiesView.setText(testProperties);
		
		return lessonItem;
	}

}
