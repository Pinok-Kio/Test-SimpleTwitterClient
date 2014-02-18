package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Loader;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Fragments.Dialogs.UserInfoDialog;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.ConcreteUserTimelineAdapter;
import com.example.Twitter_Android.Fragments.Dialogs.DirectMessageDialog;
import com.example.Twitter_Android.Loaders.Task_LoadUserTimeline;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

public class ConcreteUserTimelineFragment extends TimelineFragment<Tweet> {
	private Activity mainActivity;
	private TimelineAdapter<Tweet> currentAdapter;
	private static final String TAG_UID = "VALUE_USER_ID";
	private static final String MAX_ID = "VALUE_MAX_ID";
	private static final String SINCE_ID = "VALUE_SINCE_ID";
	public static final String TAG = "CONCRETE_USER_TIMELINE_FRAGMENT";
	private static final String ADAPTER_TAG = "C_U_TL_F";   //Concrete_User_TimeLine_Fragment
	private static final int FIRST_TIME_LOADER = Constants.CONCRETE_USER_TIMELINE_LOADER;
	private static final int OLD_TWEETS_LOADER = Constants.CONCRETE_USER_OLD_TWEETS_LOADER;
	private static final int NEWEST_TWEETS_LOADER = Constants.CONCRETE_USER_NEWEST_TWEETS_LOADER;
	private static Person currentPerson;
	private static ImageView avatar;
	private final ImageDownloader imageDownloader = new ImageDownloader();
	private final DataCache cache = DataCache.getInstance();

	//------------------------------------------------------------------------------------------------------------------
	public static ConcreteUserTimelineFragment newInstance(Person person, ImageView selectedPersonAvatar) {
		ConcreteUserTimelineFragment fragment = new ConcreteUserTimelineFragment();
		currentPerson = person;
		avatar = selectedPersonAvatar;
		Bundle args = new Bundle();
		args.putLong(TAG_UID, person.getID());
		fragment.setArguments(args);
		fragment.setHasOptionsMenu(true);
		return fragment;
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
		Bundle args = getArguments();

		if (args != null) {
			mainActivity.getLoaderManager().restartLoader(FIRST_TIME_LOADER, args, this);
		}

		if (currentAdapter == null) {
			/*
				Может получиться ситуация, когда мы мозвращаемся к этому фрагменту, например из другой активити.
				В таком случае, чтобы не грузить твиты снова и не создавать адаптер (снова), берем сохраненный адаптер
				и используем его.
			 */
			currentAdapter = (ConcreteUserTimelineAdapter) cache.getAdapter(ADAPTER_TAG);
			if (currentAdapter != null) {
				setListAdapter(currentAdapter);
				currentAdapter.updateContext(mainActivity);
			} else {
				mainActivity.getLoaderManager().initLoader(FIRST_TIME_LOADER, null, this);
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

	//------------------------------------------------------------------------------------------------------------------
	@Override
	protected void loadOldItems() {
		Bundle args = new Bundle();
		long maxID = getMaxId();
		long uid = currentPerson.getID();
		args.putLong(MAX_ID, maxID);
		args.putLong(TAG_UID, uid);
		mainActivity.getLoaderManager().restartLoader(OLD_TWEETS_LOADER, args, this);
	}

	@Override
	protected void loadNewestItems() {
		Bundle args = new Bundle();
		long sinceID = getSinceID();
		long uid = currentPerson.getID();
		args.putLong(SINCE_ID, sinceID);
		args.putLong(TAG_UID, uid);
		mainActivity.getLoaderManager().restartLoader(NEWEST_TWEETS_LOADER, args, this);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<List<? extends Tweet>> onCreateLoader(int id, Bundle args) {
		/*
			Здесь будет:
			        если id == FIRST_TIME_LOADER, то maxID и sinceID == 0
					если id == OLD_TWEETS_LOADER, то maxID !=0 и sinceID == 0
					если id == NEWEST_TWEETS_LOADER, то maxID ==0 и sinceID != 0
		 */
		long uid = args.getLong(TAG_UID, 0);
		long maxID = args.getLong(MAX_ID, 0);
		long sinceID = args.getLong(SINCE_ID, 0);
		return new Task_LoadUserTimeline(mainActivity, uid, maxID, sinceID);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onLoadFinished(Loader<List<? extends Tweet>> loader, List<? extends Tweet> data) {
		if (data.size() > 0) {
			if (currentAdapter == null) {
				currentAdapter = new ConcreteUserTimelineAdapter(mainActivity, data, ADAPTER_TAG);
				setListAdapter(currentAdapter);
			} else {
				switch (loader.getId()) {
					case FIRST_TIME_LOADER:
						currentAdapter.addItemsInstead(data);
						break;

					case OLD_TWEETS_LOADER:
						currentAdapter.addItemsToBottom(data);
						break;

					case NEWEST_TWEETS_LOADER:
						currentAdapter.addItemsToTop(data);
						break;
				}
			}
			mainActivity.setTitle(currentPerson.getName());
		}
		isLoading = false;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onLoaderReset(Loader<List<? extends Tweet>> loader) {
		currentAdapter = null;
		setListAdapter(null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		currentPerson = getItem(position).getPerson();
		imageDownloader.loadBitmap(currentPerson.getProfileImage(), avatar);
		final long selectedUserId = currentPerson.getID();
		Bundle args = new Bundle();
		args.putLong(TAG_UID, selectedUserId);
		mainActivity.setTitle("Loading: " + currentPerson.getName());
		mainActivity.getLoaderManager().restartLoader(FIRST_TIME_LOADER, args, this);
		return true;
	}
	//------------------------------------------------------------------------------------------------------------------


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_send_message:
				sendMessage();
				return true;

			case R.id.menu_item_info:
				showInfo();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendMessage() {
		DirectMessageDialog dialog = DirectMessageDialog.getInstance(currentPerson);
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), DirectMessageDialog.TAG);
	}

	private void showInfo() {
		final Tweet tweet = getSelectedItem();
		if (tweet != null) {
			Person person = tweet.getPerson();
			System.out.println("INFO = " + person);
			final DialogFragment dialog = UserInfoDialog.newInstance(person);
			dialog.show(mainActivity.getFragmentManager().beginTransaction(), UserInfoDialog.TAG);
		}
	}
}
