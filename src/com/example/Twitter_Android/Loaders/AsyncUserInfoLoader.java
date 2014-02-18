package com.example.Twitter_Android.Loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import org.json.simple.parser.ParseException;

public class AsyncUserInfoLoader extends AsyncTaskLoader<Person> {
	private final Connector connector;
	private long userID;

	public AsyncUserInfoLoader(Context context, Connector c) {
		super(context);
		connector = c;
	}

	public AsyncUserInfoLoader(Context context, Connector c, long name) {
		super(context);
		connector = c;
		this.userID = name;
	}

	@Override
	public Person loadInBackground() {
		DataCache cache = DataCache.getInstance();
		Person connectedUser = cache.getPerson(userID);
		if (connectedUser == null) {
			if (userID == 0) {
				try {
					connectedUser = connector.getAuthPerson();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				try {
					connectedUser = connector.getPersonByID(userID);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			cache.putPerson(userID, connectedUser);
		}

		cache.setConnectedUserID(connectedUser.getID());
		return connectedUser;
	}
}
