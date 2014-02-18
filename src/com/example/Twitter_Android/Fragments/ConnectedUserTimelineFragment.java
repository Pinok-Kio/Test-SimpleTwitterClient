package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Loader;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TweetAdapter;
import com.example.Twitter_Android.Fragments.Dialogs.DeleteTweetDialog;
import com.example.Twitter_Android.Fragments.Dialogs.PostTweetDialog;
import com.example.Twitter_Android.Fragments.Dialogs.ReplyDialog;
import com.example.Twitter_Android.Loaders.Task_LoadUserTimeline;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

public class ConnectedUserTimelineFragment extends TimelineFragment<Tweet> {
	private Activity mainActivity;
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
	public static ConnectedUserTimelineFragment newInstance() {
		return instance;
	}
	//------------------------------------------------------------------------------------------------------------------

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
				ћожет получитьс€ ситуаци€, когда мы мозвращаемс€ к этому фрагменту, например из другой активити.
				¬ таком случае, чтобы не грузить твиты снова и не создавать адаптер (снова), берем сохраненный адаптер
				и используем его.
			 */
			currentAdapter = (TweetAdapter) cache.getAdapter(ADAPTER_TAG);
			if (currentAdapter == null) {
				mainActivity.getLoaderManager().initLoader(TIMELINE_LOADER_ID, null, this);
			} else {
				setListAdapter(currentAdapter);
				currentAdapter.updateContext(mainActivity);
			}
		} else {
			/*
				“.к. данный фрагмент setRetainInstance(true), то при изменении конфигурации
				(при повороте экрана, например) необходимо обновить ссылку на MainActivity.
				»наче возникает ошибка вида "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState",
				когда диалог пытаетс€ отобразитьс€ в сохраненной (а не в текущей) активити.
			 */
			currentAdapter.updateContext(mainActivity);
		}
		needNewestTweet = false;
	}

	@Override
	protected void loadOldItems() {
		long maxID = getMaxId();
		Bundle args = new Bundle();
		args.putLong(MAX_ID, maxID);
		mainActivity.getLoaderManager().restartLoader(OLD_TWEETS_LOADER, args, this);
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<List<? extends Tweet>> onCreateLoader(int id, Bundle args) {
		/*
			≈сли мы уже загрузили все твиты, которые уже написали, то больше даже не будем пытатьс€ создать loader,
			ведь новые твиты мы будем просто добавл€ть в адаптер.
		 */
		return new Task_LoadUserTimeline(mainActivity, cache.getConnectedUserID(), 0, 0);
	}

	@Override
	public void onLoadFinished(Loader<List<? extends Tweet>> loader, List<? extends Tweet> data) {
		System.out.println("ConnectedUserTimelineFragment onLoadFinished " + data.size() + " " + data.get(0));

		if ((data.size() > 0) && (loader != null)) {
			switch (loader.getId()) {
				case TIMELINE_LOADER_ID:
					if (currentAdapter == null) {
						currentAdapter = new TweetAdapter(mainActivity, data, ADAPTER_TAG);
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
		//«агрузили мало твитов - значит больше грузить нечего и, соответственно, не нужно.
		if (data.size() < TWEETS_BEFORE_END) {
			needOldTweet = false;
		}
		isLoading = false;
	}

	@Override
	public void onLoaderReset(Loader<List<? extends Tweet>> loader) {
		currentAdapter = null;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Tweet tweetToReply = getItem(position);
		DialogFragment dialogFragment = ReplyDialog.getInstance(tweetToReply);
		dialogFragment.show(mainActivity.getFragmentManager().beginTransaction(), ReplyDialog.TAG);
		return true;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_post_tweet:
				postTweet();
				return true;
			case R.id.menu_item_remove_tweet:
				Toast.makeText(mainActivity, "NEED TO DO REMOVE TWEET DIALOG", Toast.LENGTH_LONG).show();
				deleteTweet();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void postTweet() {
		DialogFragment dialog = PostTweetDialog.newInstance(currentAdapter);
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), PostTweetDialog.TAG);
	}

	private void deleteTweet() {
		DialogFragment dialog = DeleteTweetDialog.getInstance(currentAdapter, getSelectedItem());
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), DeleteTweetDialog.TAG);
	}
}
