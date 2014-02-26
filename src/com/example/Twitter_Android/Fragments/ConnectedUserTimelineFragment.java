package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TweetAdapter;
import com.example.Twitter_Android.Fragments.Dialogs.DeleteTweetDialog;
import com.example.Twitter_Android.Fragments.Dialogs.PostTweetDialog;
import com.example.Twitter_Android.Fragments.Dialogs.ReplyDialog;
import com.example.Twitter_Android.Loaders.UserTimelineLoader;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

public class ConnectedUserTimelineFragment extends TimelineFragment<Tweet> {
	private TimelineAdapter<Tweet> currentAdapter;
	private static final DataCache cache;

	private static final int TIMELINE_LOADER_ID = Constants.CONNECTED_USER_TIMELINE_LOADER;
	private static final int OLD_TWEETS_LOADER = Constants.CONNECTED_USER_OLD_TWEETS_LOADER;
	public static final String TAG = "TAG_CONNECTED_USER_TIMELINE";
	private static final String ADAPTER_TAG = "CON_U_TL_F";
	private static final String MAX_ID = "MAX_ID";
	private static final ConnectedUserTimelineFragment instance;

	static {
		instance = new ConnectedUserTimelineFragment();
		instance.setRetainInstance(true);
		instance.setHasOptionsMenu(true);
		cache = DataCache.getInstance();
	}

	//------------------------------------------------------------------------------------------------------------------
	public static ConnectedUserTimelineFragment getInstance() {
		return instance;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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
				LoaderManager loaderManager = getLoaderManager();
				if (loaderManager != null) {
					loaderManager.initLoader(TIMELINE_LOADER_ID, null, this);
				}
			} else {
				setListAdapter(currentAdapter);
				currentAdapter.updateContext(getActivity());
			}
		} else {
			/*
				Т.к. данный фрагмент setRetainInstance(true), то при изменении конфигурации
				(при повороте экрана, например) необходимо обновить ссылку на MainActivity.
				Иначе возникает ошибка вида "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState",
				когда диалог пытается отобразиться в сохраненной (а не в текущей) активити.
			 */
			currentAdapter.updateContext(getActivity());
		}
		needNewestTweet = false;
	}

	@Override
	protected void loadOldItems() {
		long maxID = getMaxId();
		Bundle args = new Bundle();
		args.putLong(MAX_ID, maxID);
		LoaderManager loaderManager = getLoaderManager();
		if (loaderManager != null) {
			loaderManager.restartLoader(OLD_TWEETS_LOADER, args, this);
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<List<Tweet>> onCreateLoader(int id, Bundle args) {
		return new UserTimelineLoader(getActivity(), cache.getConnectedUserID(), 0, 0);
	}

	@Override
	public void onLoadFinished(Loader<List<Tweet>> loader, List<Tweet> data) {
		if ((data.size() > 0) && (loader != null)) {
			switch (loader.getId()) {
				case TIMELINE_LOADER_ID:
					if (currentAdapter == null) {
						currentAdapter = new TweetAdapter(getActivity(), data, ADAPTER_TAG);
						setListAdapter(currentAdapter);
					}
					break;
				case OLD_TWEETS_LOADER:
					if (currentAdapter != null) {
						currentAdapter.addItemsToBottom(data);
					}
					break;
			}
		}
		//Загрузили мало твитов - значит больше грузить нечего и, соответственно, не нужно.
		if (data.size() < TWEETS_BEFORE_END) {
			needOldTweet = false;
		}
		isLoading = false;
	}

	@Override
	public void onLoaderReset(Loader<List<Tweet>> loader) {
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Tweet tweetToReply = getItem(position);
		if (tweetToReply != null) {
			Activity activity = getActivity();
			if (activity != null) {
				DialogFragment dialogFragment = ReplyDialog.getInstance(tweetToReply);
				dialogFragment.show(getActivity().getFragmentManager().beginTransaction(), ReplyDialog.TAG);
				return true;
			}
		}
		return false;
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_my_tweets, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_post_tweet:
				postTweet();
				return true;
			case R.id.menu_item_remove_tweet:
				deleteTweet();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void postTweet() {
		Activity activity = getActivity();
		if (activity != null) {
			DialogFragment dialog = PostTweetDialog.newInstance(currentAdapter);
			dialog.show(activity.getFragmentManager().beginTransaction(), PostTweetDialog.TAG);
		}
	}

	private void deleteTweet() {
		Activity activity = getActivity();
		if (activity != null) {
			DialogFragment dialog = DeleteTweetDialog.getInstance(currentAdapter, getSelectedItem());
			dialog.show(activity.getFragmentManager().beginTransaction(), DeleteTweetDialog.TAG);
		}
	}
}
