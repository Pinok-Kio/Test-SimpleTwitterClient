package com.example.Twitter_Android.AsynkTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

public class TaskReply extends AsyncTask<Bundle, Void, Boolean> {
	public static final String TWEET_ID = "ID";
	public static final String TEXT = "TEXT";
	private final Context context;

	public TaskReply(Context context) {
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(Bundle... params) {
		if (params.length > 0) {
			final Connector connector = new Connector();
			Bundle param = params[0];
			long tweetID = param.getLong(TWEET_ID, 0);
			String text = param.getString(TEXT, "");

			return connector.replyTo(tweetID, text);
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		if (!aBoolean) {
			showToast();
		}
	}

	private void showToast() {
		String message = context.getString(R.string.toast_reply_error);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
