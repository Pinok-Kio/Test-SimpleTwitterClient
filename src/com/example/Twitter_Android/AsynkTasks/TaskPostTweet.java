package com.example.Twitter_Android.AsynkTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;
import org.json.simple.parser.ParseException;

public class TaskPostTweet extends AsyncTask<String, Void, Tweet> {
	private final Context context;
	private final TimelineAdapter<Tweet> adapter;

	/*
		������� ���������� ������ ����� ���������:
				1.  ������� ��������� ���� � �������� ��� � ������ ������ (��� ������ � ���������� ���������).
					��� ��������� ��������� ������� ������ ����������.
				2.  ��������� ��������� ������ ��� ���������� ����� "��-����������".
					����� ����, ��� �� �������� ���� �� �����, ����� �������� ����, �������
					��� �������� � ������ ������. ��� ����� ��� ����, ����� �������� id ����� (� ���
					����� ��� ����, ����� ����� ����� ���� ����� ���� �������).
				3.  ���� ��������� ���� �� �������, ������� ��������� ����������� � ������ ����.
	 */
	public TaskPostTweet(Context context, TimelineAdapter<Tweet> adapter) {
		this.context = context;
		this.adapter = adapter;
	}

	@Override
	protected Tweet doInBackground(String... params) {
		if (params.length > 0) {
			try {
				return Connector.getInstance().postTweet(params[0]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Tweet tweet) {
		super.onPostExecute(tweet);
		if (tweet == null) {
			showToast();
			adapter.removeItem(0);
		} else {
			adapter.updateLastAddedTweet(tweet);
		}
	}

	private void showToast() {
		String message = context.getString(R.string.toast_post_tweet_error);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
