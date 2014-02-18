package com.example.Twitter_Android.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Task_LoadSearchedUsers extends TweetLoader<Person> {
	private final String toFind;
	private final int MAX_RESULT_COUNT;

	public Task_LoadSearchedUsers(Context context, String query) {
		super(context);
		toFind = query;
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ALLOWED_TWEET_COUNT, "100");
		MAX_RESULT_COUNT = Integer.valueOf(value);
	}

	@Override
	public List<Person> loadInBackground() {
		final Connector connector = Connector.getInstance();

		List<Person> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.findUsers(toFind, MAX_RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
}



