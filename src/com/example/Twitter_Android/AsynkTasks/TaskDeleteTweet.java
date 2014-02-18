package com.example.Twitter_Android.AsynkTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

public class TaskDeleteTweet extends AsyncTask<Long, Void, Boolean> {
	private final Context context;

	public TaskDeleteTweet(Context context) {
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(Long... params) {
		return (params.length > 0) && (Connector.getInstance().deleteTweet(params[0]));
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		if (!aBoolean) {
			showToast();
		}
	}

	private void showToast() {
		String message = context.getString(R.string.toast_delete_tweet_error);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
