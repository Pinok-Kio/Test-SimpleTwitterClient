package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class FollowingsLoader extends TweetLoader<Person> {
	private final long cursor;
	private final Connector connector;

	//------------------------------------------------------------------------------------------------------------------
	public FollowingsLoader(Context context, Connector c, long cursor) {
		super(context);
		connector = c;
		this.cursor = cursor;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public List<Person> loadInBackground() {
		List<Person> followings = new ArrayList<>();
		try {
			followings = connector.getFriends(DataCache.getInstance().getConnectedUserID(), cursor);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return followings;
	}
}
