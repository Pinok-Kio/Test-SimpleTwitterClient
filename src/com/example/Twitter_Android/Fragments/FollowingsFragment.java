package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.example.Twitter_Android.AppActivity.ConcreteUserTimelineActivity;
import com.example.Twitter_Android.AsynkTasks.TaskUnfollow;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.FollowingsListAdapter;
import com.example.Twitter_Android.Loaders.FollowingsLoader;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

import java.util.List;

public class FollowingsFragment extends TimelineFragment<Person> {
	private Activity mainActivity;
	private final Connector connector;
	private TimelineAdapter<Person> currentAdapter;
	private static final FollowingsFragment instance;
	private long nextCursor = -1;
	private static final int FOLLOWINGS_LOADER = Constants.FOLLOWINGS_LOADER_ID;
	public static final String TAG = "TAG_FOLLOWINGS_FRAGMENT";

	static {
		instance = new FollowingsFragment();
	}

	private FollowingsFragment() {
		connector = Connector.getInstance();
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	//------------------------------------------------------------------------------------------------------------------

	public static FollowingsFragment getInstance() {
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
			currentAdapter = (FollowingsListAdapter) DataCache.getInstance().getAdapter(FollowingsListAdapter.TAG);
			if (currentAdapter == null) {
				mainActivity.getLoaderManager().initLoader(FOLLOWINGS_LOADER, null, this);
			} else {
				setListAdapter(currentAdapter);
			}
		}
		needNewestTweet = false;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	protected void loadOldItems() {
		mainActivity.getLoaderManager().restartLoader(FOLLOWINGS_LOADER, null, this);
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Person person = currentAdapter.getItem(position);
		Intent intent = new Intent(mainActivity, ConcreteUserTimelineActivity.class);
		intent.putExtra("PERSON_ID", person.getID());
		DataCache.getInstance().putPerson(person.getID(), person);
		startActivity(intent);
		return true;
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<List<? extends Person>> onCreateLoader(int id, Bundle args) {
		if (id == FOLLOWINGS_LOADER) {
			return new FollowingsLoader(getActivity(), nextCursor);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<List<? extends Person>> loader, List<? extends Person> data) {
		if (loader.getId() == FOLLOWINGS_LOADER) {
			if (currentAdapter == null) {
				currentAdapter = new FollowingsListAdapter(mainActivity, data);
				setListAdapter(currentAdapter);
				DataCache.getInstance().saveAdapter(FollowingsListAdapter.TAG, currentAdapter);
			} else {
				currentAdapter.addItemsToBottom(data);
			}
			nextCursor = connector.getCurrentCursor();
			isLoading = false;
			if (nextCursor == 0) {
				needOldTweet = false;
				isLoading = true;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<List<? extends Person>> loader) {
//		setListAdapter(null);
	}
	//------------------------------------------------------------------------------------------------------------------


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_followings, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_send_message:
				Toast.makeText(mainActivity, "FOLLOWINGS FRAGMENT Send message", Toast.LENGTH_LONG).show();
				return true;

			case R.id.menu_item_unfollow:
				unfollow();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	//------------------------------------------------------------------------------------------------------------------
	/*
	    Отписаться от выбранного друга (followings).
	 */
	private void unfollow() {
		Person toUnfollow = getSelectedItem();
		final long idToDelete = toUnfollow.getID();
		toUnfollow.changeFriendshipRelations();
		currentAdapter.removeItem(getSelectedListItemPosition());
		TaskUnfollow task = new TaskUnfollow();
		task.execute(idToDelete);
	}

}
