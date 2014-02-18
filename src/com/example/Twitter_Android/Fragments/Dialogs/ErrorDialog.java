package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.Twitter_Android.R;

public class ErrorDialog extends DialogFragment {
	private static final String ID_TEXT = "ID_TEXT";
	public static final String TAG = "ERROR_DIALOG";

	private ErrorDialog() {

	}

	public static ErrorDialog getInstance(int textResource) {
		ErrorDialog dialog = new ErrorDialog();
		Bundle args = new Bundle();
		args.putInt(ID_TEXT, textResource);
		dialog.setArguments(args);

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.dialog_error, container, false);
		TextView errorMessage = (TextView) v.findViewById(R.id.message);
		int messageID = getArguments().getInt(ID_TEXT);
		errorMessage.setText(messageID);
		Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return v;
	}
}
