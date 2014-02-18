package com.example.Twitter_Android.AsynkTasks;

import android.os.AsyncTask;
import com.example.Twitter_Android.Net.Connector;

public class TaskUnfollow extends AsyncTask<Long, Void, Void> {
	@Override
	protected Void doInBackground(Long... params) {
		if (params.length > 0) {
			Connector.getInstance().unfollow(params[0]);
		}
		return null;
	}
}
