package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class FollowersLoader extends TweetLoader<Person> {
	private final Connector connector;
	private final long cursor;

	public FollowersLoader(Context context, Connector connector, long cursor) {
		super(context);
		this.connector = connector;
		this.cursor = cursor;
	}

	@Override
	public List<Person> loadInBackground() {
		List<Person> followers = new ArrayList<>();

		try {
			followers = connector.getFollowers(DataCache.getInstance().getConnectedUserID(), cursor);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return followers;
	}
}
