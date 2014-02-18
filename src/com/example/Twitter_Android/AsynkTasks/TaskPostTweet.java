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
		ѕроцесс добавлени€ нового твита следующий:
				1.  —оздаем временный твит и помещаем его в список твитов (это делаем в вызывающем фрагменте).
					“ак возникает видимость быстрой работы приложени€.
				2.  «апускаем отдельную задачу дл€ добавлени€ твита "по-насто€щему".
					ѕосле того, как мы запостим твит на сайте, нужно обновить твит, который
					уже добавили в тписок твитов. Ёто нужно дл€ того, чтобы обновить id твита (а это
					нужно дл€ того, чтобы потом такой твит можно было удалить).
				3.  ≈сли запостить твит не удалось, удал€ем последний добавленный в список твит.
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
