package com.example.Twitter_Android.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Task_LoadUserTimeline extends TweetLoader<Tweet> {
	private static Connector connector;
	private final int DEFAULT_TWEET_COUNT;
	private final long maxID;
	private final long sinceID;
	private final long userID;

	//------------------------------------------------------------------------------------------------------------------
	public Task_LoadUserTimeline(Context context, long userID, long maxID, long sinceID) {
		super(context);
		connector = Connector.getInstance();
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ALLOWED_TWEET_COUNT, "50");
		DEFAULT_TWEET_COUNT = Integer.valueOf(value);
		/*
			Проверка. Один из этих параметров всегда должен быть равен 0.
			Иначе выбираем, что грузить будем новейшие твиты.
		 */
		if (maxID != 0 && sinceID != 0) {
			sinceID = 0;
		}
		this.maxID = maxID;
		this.sinceID = sinceID;
		this.userID = userID;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public List<Tweet> loadInBackground() {
		System.out.println("LOADING USER TIMELINE");
		List<Tweet> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.getStatuses_UserTimeline(userID, maxID, sinceID, DEFAULT_TWEET_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
	//------------------------------------------------------------------------------------------------------------------
}
