package com.example.Twitter_Android.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Task_LoadHomeTimeline extends TweetLoader<Tweet> {
	private final int DEFAULT_TWEET_COUNT;
	private final long maxID;
	private final long sinceID;

	//------------------------------------------------------------------------------------------------------------------
	public Task_LoadHomeTimeline(Context context, long maxID, long sinceID) {
		super(context);
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ALLOWED_TWEET_COUNT, "50");
		DEFAULT_TWEET_COUNT = Integer.valueOf(value);
		/*
			Проверка. Один из этих параметров всегда должен быть равен 0.
			Иначе выбираем, что грузить будем новейшие твиты.
		 */
		if (maxID != 0 && sinceID != 0) {
			maxID = 0;
		}
		this.maxID = maxID;
		this.sinceID = sinceID;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public List<? extends Tweet> loadInBackground() {
		final Connector connector = Connector.getInstance();
		List<Tweet> loadedTweets = new ArrayList<>();
		System.out.println("LOADING HOME TIMELINE");
		try {
			loadedTweets = connector.getStatuses_HomeTimeline(maxID, sinceID, DEFAULT_TWEET_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
	//------------------------------------------------------------------------------------------------------------------
}
