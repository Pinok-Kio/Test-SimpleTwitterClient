package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class FollowingsLoader extends TweetLoader<Person> {
	private final Connector connector;
	private final long cursor;

	//------------------------------------------------------------------------------------------------------------------
	public FollowingsLoader(Context context, long cursor) {
		super(context);
		connector = Connector.getInstance();
		this.cursor = cursor;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public List<? extends Person> loadInBackground() {
		List<? extends Person> followings = new ArrayList<>();
		try {
			followings = connector.getFriends(DataCache.getInstance().getConnectedUserID(), cursor);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return followings;
	}
}
