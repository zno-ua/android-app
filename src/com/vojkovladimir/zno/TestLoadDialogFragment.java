package com.vojkovladimir.zno;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.vojkovladimir.zno.api.Api;

public class TestLoadDialogFragment extends DialogFragment implements
		OnClickListener {

	private static final String DB_NAME = "db_name";
	OnTestLoadListener onTestLoad;
	ProgressDialog downloadProgress;

	public static TestLoadDialogFragment newInstance(String dbName,
			OnTestLoadListener onTestLoad, ProgressDialog downloadProgress) {
		TestLoadDialogFragment f = new TestLoadDialogFragment();
		Bundle args = new Bundle();
		args.putString(DB_NAME, dbName);
		
		f.onTestLoad = onTestLoad;
		f.downloadProgress = downloadProgress;
		f.setArguments(args);
		
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
				getActivity());
		dialogBuilder.setMessage(R.string.dialog_load_test_text);
		dialogBuilder.setPositiveButton(R.string.dialog_positive_text, this);
		dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
		return dialogBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE: {
			downloadProgress.show();
			String dbName = getArguments().getString(DB_NAME);
			ZNOApplication.getInstance().addToRequestQueue(
					Api.getTestRequest(dbName.replace("_", "-"), onTestLoad));
		}
			break;
		}
	}
}