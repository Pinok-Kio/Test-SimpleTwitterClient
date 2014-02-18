package com.example.Twitter_Android.AsynkTasks;

import android.os.AsyncTask;
import android.os.Bundle;
import com.example.Twitter_Android.Net.Connector;

public class TaskSendMessage extends AsyncTask<Bundle, Void, Boolean> {
	public static final String ID = "ID";
	public static final String TEXT = "TEXT";

	@Override
	protected Boolean doInBackground(Bundle... params) {
		if (params.length > 0) {
			Bundle args = params[0];
			long id = args.getLong(ID, 0);
			String text = args.getString(TEXT, ";-)");
			return Connector.getInstance().sendDirectMessage(id, text);
		}
		return false;
	}
}
