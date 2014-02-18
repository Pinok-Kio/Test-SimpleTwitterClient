package com.example.Twitter_Android.AppActivity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Fragments.Adapters.FollowingsListAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TweetAdapter;
import com.example.Twitter_Android.Loaders.Task_LoadSearchedTweets;
import com.example.Twitter_Android.Loaders.Task_LoadSearchedUsers;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

public class SearchableActivity extends ListActivity implements LoaderManager.LoaderCallbacks {
	private static final String ADAPTER_TAG = "SEARCH_ACTIVITY";
	private static final int TWEETS_LOADER = Constants.SEARCH_RESULT_LOADER;
	private static final int USERS_LOADER = Constants.SEARCH_USERS_LOADER;
	private static final String QUERY = "QUERY";


	private TweetAdapter tweetAdapter;
	private FollowingsListAdapter followingsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String intentAction = intent.getAction();
		if ((intentAction != null) && (intentAction.equals(Intent.ACTION_SEARCH))) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doSearch(query);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if ((action != null) && (action.equals(Intent.ACTION_SEARCH))) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doSearch(query);
		}
	}

	private void doSearch(String query) {
		Bundle args = new Bundle();
		args.putString(QUERY, query);
		if (query.trim().startsWith("@")) {
			getLoaderManager().restartLoader(USERS_LOADER, args, this);
		} else {
			getLoaderManager().restartLoader(TWEETS_LOADER, args, this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home_timeline, menu);

		return true;
	}

	@Override
	public Loader onCreateLoader(int id, Bundle args) {
		String query = args.getString(QUERY, "Need_question");
		String qq = Uri.encode(query);
		Loader loader;
		switch (id) {
			case TWEETS_LOADER:
				loader = new Task_LoadSearchedTweets(this, qq);
				return loader;
			case USERS_LOADER:
				loader = new Task_LoadSearchedUsers(this, qq);
				return loader;
		}
		return null;
	}


	@Override
	public void onLoadFinished(Loader loader, Object data) {
		if (loader.getId() == TWEETS_LOADER) {
			if (tweetAdapter == null) {
				tweetAdapter = new TweetAdapter(this, (List<? extends Tweet>) data, ADAPTER_TAG);
				setListAdapter(tweetAdapter);
			} else {
				tweetAdapter.addItemsInstead((List<? extends Tweet>) data);
			}
			followingsAdapter = null;
		} else if (loader.getId() == USERS_LOADER) {
			if (followingsAdapter == null) {
				followingsAdapter = new FollowingsListAdapter(this, (List<? extends Person>) data);
				setListAdapter(followingsAdapter);
			} else {
				followingsAdapter.addItemsInstead((List<? extends Person>) data);
			}
			tweetAdapter = null;
		}
	}

	@Override
	public void onLoaderReset(Loader loader) {

	}

}
