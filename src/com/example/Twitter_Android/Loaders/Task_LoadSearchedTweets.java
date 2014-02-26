package com.example.Twitter_Android.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Task_LoadSearchedTweets extends TweetLoader<Tweet> {
	private final String toFind;

	public Task_LoadSearchedTweets(Context context, String query) {
		super(context);
		toFind = query;
	}

	@Override
	public List<Tweet> loadInBackground() {
		final Connector connector = new Connector();
		List<Tweet> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.findTweets(toFind, RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
}
