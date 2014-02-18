package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class HomeTimelineLoader extends TweetLoader<Tweet> {
	private final long maxID;
	private final long sinceID;

	//------------------------------------------------------------------------------------------------------------------
	public HomeTimelineLoader(Context context, long maxID, long sinceID) {
		super(context);
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
		List<? extends Tweet> loadedTweets = new ArrayList<>();
		try {
			loadedTweets = connector.getStatuses_HomeTimeline(maxID, sinceID, RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		setLoadedTweets(loadedTweets);
		return loadedTweets;
	}
	//------------------------------------------------------------------------------------------------------------------
}
