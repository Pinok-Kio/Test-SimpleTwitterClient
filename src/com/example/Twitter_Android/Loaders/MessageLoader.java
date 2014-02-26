package com.example.Twitter_Android.Loaders;

import android.content.Context;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MessageLoader extends TweetLoader<Tweet> {
	private final long maxID;
	private final long sinceID;

	public MessageLoader(Context context, long maxID, long sinceID) {
		super(context);
		if (maxID != 0 && sinceID != 0) {
			maxID = 0;
		}
		this.maxID = maxID;
		this.sinceID = sinceID;
	}

	@Override
	public List<Tweet> loadInBackground() {
		Connector connector = new Connector();
		List<Tweet> result = new ArrayList<>();
		try {
			result = connector.getReceivedMessages(maxID, sinceID, RESULT_COUNT);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
