package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class UserTimelineLoader extends TweetLoader<Tweet> {
	private final long maxID;
	private final long sinceID;
	private final long userID;

	//------------------------------------------------------------------------------------------------------------------
	public UserTimelineLoader(Context context, long userID, long maxID, long sinceID) {
		super(context);
		/*
			Check. One of this parameters always must be 0.
			If not - loading newest tweets
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
	public List<? extends Tweet> loadInBackground() {
		final Connector connector = new Connector();
		List<? extends Tweet> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.getStatuses_UserTimeline(userID, maxID, sinceID, RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
	//------------------------------------------------------------------------------------------------------------------
}
