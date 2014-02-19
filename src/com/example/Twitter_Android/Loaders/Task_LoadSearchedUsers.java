package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Task_LoadSearchedUsers extends TweetLoader<Person> {
	private final String toFind;

	public Task_LoadSearchedUsers(Context context, String query) {
		super(context);
		toFind = query;
	}

	@Override
	public List<? extends Person> loadInBackground() {
		final Connector connector = new Connector();

		List<? extends Person> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.findUsers(toFind, RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
}



