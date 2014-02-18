package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import com.example.Twitter_Android.AppActivity.ConcreteUserTimelineActivity;
import com.example.Twitter_Android.AppActivity.SearchableActivity;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TweetAdapter;
import com.example.Twitter_Android.Fragments.Dialogs.DirectMessageDialog;
import com.example.Twitter_Android.Fragments.Dialogs.UserInfoDialog;
import com.example.Twitter_Android.Loaders.HomeTimelineLoader;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

import java.util.List;

public class HomeTimelineFragment extends TimelineFragment<Tweet> {
	private Activity mainActivity;
	private static Connector connector;
	private TimelineAdapter<Tweet> currentAdapter;
	private static final String MAX_ID = "MAX_ID";
	private static final String SINCE_ID = "SINCE_ID";
	private static final HomeTimelineFragment instance;
	private static final int OLD_TWEETS_LOADER = Constants.HOME_OLD_TWEETS_LOADER;
	private static final int NEWEST_TWEETS_LOADER_ID = Constants.HOME_NEWEST_TWEETS_LOADER;
	private static final String ADAPTER_TAG = "H_TL_F_A";   //Home_TimeLine_Fragment_Adapter

	public static final String TAG = "TAG_HOME_TIMELINE_FRAGMENT";
	private static final DataCache cache = DataCache.getInstance();

	static {
		instance = new HomeTimelineFragment();
		instance.setRetainInstance(true);
		instance.setHasOptionsMenu(true);
	}

	//------------------------------------------------------------------------------------------------------------------

	public static HomeTimelineFragment newInstance() {
		if (connector == null) {
			connector = Connector.getInstance();
		}
		return instance;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = activity;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (currentAdapter == null) {
			/*
				Может получиться ситуация, когда мы мозвращаемся к этому фрагменту, например из другой активити.
				В таком случае, чтобы не грузить твиты снова и не создавать адаптер (снова), берем сохраненный адаптер
				и используем его.
			 */
			currentAdapter = (TweetAdapter) cache.getAdapter(ADAPTER_TAG);
			if (currentAdapter == null) {
				mainActivity.getLoaderManager().initLoader(OLD_TWEETS_LOADER, null, this);
			} else {
				setListAdapter(currentAdapter);
				currentAdapter.updateContext(mainActivity);
			}
		} else {
			/*
				Т.к. данный фрагмент setRetainInstance(true), то при изменении конфигурации
				(при повороте экрана, например) необходимо обновить ссылку на MainActivity.
				Иначе возникает ошибка вида "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState",
				когда диалог пытается отобразиться в сохраненной (а не в текущей) активити.
			 */
			currentAdapter.updateContext(mainActivity);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Person selectedTweetAuthor = getItem(position).getPerson();
		view.setBackground(getResources().getDrawable(R.drawable.rounded_corners_pressed));
		Intent intent = new Intent(getActivity(), ConcreteUserTimelineActivity.class);
		intent.putExtra("PERSON_ID", selectedTweetAuthor.getID());
		cache.putPerson(selectedTweetAuthor.getID(), selectedTweetAuthor);
		startActivity(intent);
		return true;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	protected void loadOldItems() {
		Bundle args = new Bundle();
		long maxID = getMaxId();
		args.putLong(MAX_ID, maxID);
		mainActivity.getLoaderManager().restartLoader(OLD_TWEETS_LOADER, args, this);
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Загрузить новейшие твиты.
	 * Происходит, когда пользователь листает твиты в начало, не чаще одного раза в NEWEST_TWEET_LOAD_PERIOD мс.
	 */
	protected void loadNewestItems() {
		Bundle args = new Bundle();
		long sinceID = getSinceID();
		args.putLong(SINCE_ID, sinceID);
		mainActivity.getLoaderManager().restartLoader(NEWEST_TWEETS_LOADER_ID, args, this);
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public Loader<List<? extends Tweet>> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case OLD_TWEETS_LOADER:
				/*
					Поскольку, это HOME_OLD_TWEETS_LOADER, то параметр USER_ID_VALUE всегда == 0, и здесь важен только
					параметр MAX_ID (т.е. max_id в api twitter'а)
				 */
				long tweetMaxValue = 0;
				if (args != null) {
					tweetMaxValue = args.getLong(MAX_ID, 0);
				}
				return new HomeTimelineLoader(mainActivity, tweetMaxValue, 0);

			case NEWEST_TWEETS_LOADER_ID:
				long sinceIdValue = 0;
				if (args != null) {
					sinceIdValue = args.getLong(SINCE_ID, 0);
				}
				return new HomeTimelineLoader(mainActivity, 0, sinceIdValue);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<List<? extends Tweet>> loader, List<? extends Tweet> data) {
		if (data.size() > 0) {
			switch (loader.getId()) {
				case OLD_TWEETS_LOADER:   //Загружаем старые твиты.
					if (currentAdapter == null) {
						currentAdapter = new TweetAdapter(mainActivity, data, ADAPTER_TAG);
						setListAdapter(currentAdapter);
					} else {
						currentAdapter.addItemsToBottom(data);
					}

					if (data.size() < TWEETS_BEFORE_END) {
						//Загрузили все, что могли - больше грузить не будем.
						needOldTweet = false;
					}
					break;

				case NEWEST_TWEETS_LOADER_ID: //Загружаем новейшие твиты.
					int pos = data.size() - 1;
					currentAdapter.addItemsToTop(data);
					ListView listView = getListView();
					if (listView != null) {
						listView.setSelection(pos);
					}
					break;
			}
		}
		isLoading = false;
	}

	@Override
	public void onLoaderReset(Loader<List<? extends Tweet>> loader) {
		currentAdapter = null;
		setListAdapter(null);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_home_timeline, menu);

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		if (searchView == null) {
			return;
		}

		SearchManager searchManager = (SearchManager) mainActivity.getSystemService(Context.SEARCH_SERVICE);
		ComponentName cn = new ComponentName(mainActivity, SearchableActivity.class);
		SearchableInfo info = searchManager.getSearchableInfo(cn);
		searchView.setSearchableInfo(info);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_send_message:
				sendDirectMessage();
				return true;

			case R.id.menu_item_info:
				showInfo();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendDirectMessage() {
		Person selectedPerson = getSelectedItem().getPerson();
		DialogFragment dialog = DirectMessageDialog.getInstance(selectedPerson);
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), DirectMessageDialog.TAG);
	}

	private void showInfo() {
		final Tweet tweet = getSelectedItem();
		if (tweet != null) {
			Person person = tweet.getPerson();
			final DialogFragment dialog = UserInfoDialog.newInstance(person);
			dialog.show(mainActivity.getFragmentManager().beginTransaction(), UserInfoDialog.TAG);
		}
	}
}

