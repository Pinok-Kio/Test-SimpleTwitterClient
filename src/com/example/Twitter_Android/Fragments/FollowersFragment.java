package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.example.Twitter_Android.AppActivity.ConcreteUserTimelineActivity;
import com.example.Twitter_Android.Fragments.Adapters.FollowingsListAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Dialogs.DirectMessageDialog;
import com.example.Twitter_Android.Loaders.FollowersLoader;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

import java.util.List;

public class FollowersFragment extends TimelineFragment<Person> {
	private final Connector connector;
	private TimelineAdapter<Person> currentAdapter;
	private static final FollowersFragment instance;
	private long nextCursor = -1;
	private static final int FOLLOWERS_LOADER = Constants.FOLLOWERS_LOADER_ID;
	public static final String TAG = "TAG_FOLLOWERS_FRAGMENT";
	private static final String ADAPTER_TAG = "FOLLOWERS_ADAPTER";

	static {
		instance = new FollowersFragment();
	}

	private FollowersFragment() {
		connector = new Connector();
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	//------------------------------------------------------------------------------------------------------------------

	public static FollowersFragment getInstance() {
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
			currentAdapter = (FollowingsListAdapter) DataCache.getInstance().getAdapter(ADAPTER_TAG);
			if (currentAdapter == null) {
				LoaderManager loaderManager = getLoaderManager();
				if (loaderManager != null) {
					loaderManager.initLoader(FOLLOWERS_LOADER, null, this);
				}
			} else {
				setListAdapter(currentAdapter);
			}
		}
		needNewestTweet = false;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	protected void loadOldItems() {
		LoaderManager loaderManager = getLoaderManager();
		if (loaderManager != null) {
			loaderManager.restartLoader(FOLLOWERS_LOADER, null, this);
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Person person = currentAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), ConcreteUserTimelineActivity.class);
		intent.putExtra("PERSON_ID", person.getID());
		DataCache.getInstance().putPerson(person.getID(), person);
		startActivity(intent);
		return true;
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<List<Person>> onCreateLoader(int id, Bundle args) {
		if (id == FOLLOWERS_LOADER) {
			return new FollowersLoader(getActivity(), connector, nextCursor);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<List<Person>> loader, List<Person> data) {
		if (loader.getId() == FOLLOWERS_LOADER) {
			if (currentAdapter == null) {
				currentAdapter = new FollowingsListAdapter(getActivity(), data, ADAPTER_TAG);
				setListAdapter(currentAdapter);
				DataCache.getInstance().saveAdapter(ADAPTER_TAG, currentAdapter);
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
	public void onLoaderReset(Loader<List<Person>> loader) {
//		setListAdapter(null);
	}
	//------------------------------------------------------------------------------------------------------------------


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_followers, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_send_message:
				sendDirectMessage();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendDirectMessage() {
		Activity activity = getActivity();
		if (activity != null) {
			Person selectedPerson = getSelectedItem();
			DialogFragment dialog = DirectMessageDialog.getInstance(selectedPerson);
			dialog.show(activity.getFragmentManager().beginTransaction(), DirectMessageDialog.TAG);
		}
	}
	//------------------------------------------------------------------------------------------------------------------
}
