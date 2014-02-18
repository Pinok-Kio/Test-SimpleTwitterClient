package com.example.Twitter_Android.AppActivity;

import android.app.*;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import com.example.Twitter_Android.Fragments.ConnectedUserTimelineFragment;
import com.example.Twitter_Android.Fragments.Dialogs.ErrorDialog;
import com.example.Twitter_Android.Fragments.FollowingsFragment;
import com.example.Twitter_Android.Fragments.HomeTimelineFragment;
import com.example.Twitter_Android.Fragments.TimelineFragment;
import com.example.Twitter_Android.Logic.AppDatabase;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.FileWorker;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;
import org.json.simple.parser.ParseException;
import org.scribe.model.Token;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends Activity implements ActionBar.TabListener {
	private final Connector connector;
	private Fragment currentFragment;   //��������, ������������ � ������ ������.

	public MainActivity() {
		connector = Connector.getInstance();
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
			����� ��� ������ ������ onCreate (��������, ����� ������������ �� ������ Activity)
			�� ��������� �����, ������ ��������.
		 */
		if (connector.isAuthorized()) {
			authorizationEnded();
		} else {
			final FileWorker fw = new FileWorker(this);
			/*
				��������� ��������� �����. ���� ������� - ������ ���������� �������������� � ����� ��������.
				����� - �������� �����������.
			 */
			if (fw.loadAccessToken()) {
				connector.setAccessToken(fw.getAccessToken());
				authorizationEnded();
			} else {
				setContentView(R.layout.layout_enter_auth_code);
			}
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Button ��� ����������� �� layout_enter_auth_code.xml
	 * @param v button
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void buttonClickHandler(View v) throws ExecutionException, InterruptedException {
		/*
			TODO: ������� ���-������ ��-�������.
		 */
		switch (v.getId()) {
			case R.id.button_code_entered:
				String btnLabel = ((Button) v).getText().toString();
				String agree = getResources().getString(R.string.btn_agree);
				if (btnLabel.equals(agree)) {
					openAuthPage();
					((Button) v).setText(R.string.btn_ok);
				} else {
					v.setEnabled(false);
					((Button) v).setText(R.string.text_wait);
					enterCode();
					authorizationEnded();
				}
		}
	}

	/*
	    ��������� � �������� �������� ��� �����������.
	 */
	private void openAuthPage() {
		String authURL = "";
		final ExecutorService exec = Executors.newSingleThreadExecutor();
		final Callable<String> authStringCallable = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return connector.getAuthUrl();
			}
		};
		try {
			authURL = exec.submit(authStringCallable).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			int errorText = R.string.simply_error;
			DialogFragment dialog = ErrorDialog.getInstance(errorText);
			dialog.show(getFragmentManager().beginTransaction(), ErrorDialog.TAG);
		}
		exec.shutdown();
		final Uri authPageUri = Uri.parse(authURL);
		final Intent openAuthPage = new Intent(Intent.ACTION_VIEW);
		openAuthPage.setData(authPageUri);
		startActivity(openAuthPage);
	}

	/*
		������ ��� �����������, �������� � ��������� �����.
	 */
	private void enterCode() throws ExecutionException, InterruptedException {
		final EditText et = (EditText) findViewById(R.id.enter_code_editText);
		final String code = et.getText().toString().trim();
		final ExecutorService exec = Executors.newSingleThreadExecutor();
		final Callable<Token> enterCodeTask = new Callable<Token>() {
			@Override
			public Token call() throws Exception {
				return connector.authorize(code);
			}
		};
		final Token token = exec.submit(enterCodeTask).get();
		exec.shutdown();
		final FileWorker fw = new FileWorker(this);
		try {
			fw.saveAccessToken(token);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
		��������� �����������. ������������� ���������� ������� ���, ���� � �������� �������� ������.
	 */
	private void authorizationEnded() {
		setContentView(R.layout.main_screen_layout);
		setImageDimensions();
		setTabs();
		startDataLoading();
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Define image sizes for images in ShowImageDialog.
	 * For high resolution screen will show images in higher resolution.
	 */
	private void setImageDimensions() {
		DataCache cache = DataCache.getInstance();
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		cache.setMaxBitmapDimension(size.x);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void startDataLoading() {
		getMyUID();
		/*
			���� ����������� ��������� ������ - ��������, ��� ����������, � ����������� �� �������� ������.
			TODO: �������� ������.
		 */
		if (isLandscapeOrientation()) {
			Configuration configuration = getResources().getConfiguration();
			switch (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
				case Configuration.SCREENLAYOUT_SIZE_NORMAL:
					System.out.println("SCREENLAYOUT_SIZE_NORMAL");
					break;
				case Configuration.SCREENLAYOUT_SIZE_LARGE:
					System.out.println("SCREENLAYOUT_SIZE_LARGE");
					Fragment followingsFragment = FollowingsFragment.getInstance();
					showFragment(followingsFragment, FollowingsFragment.TAG);
					break;
				case Configuration.SCREENLAYOUT_SIZE_XLARGE:
					System.out.println("SCREENLAYOUT_SIZE_XLARGE");
					break;
				default:
					break;
			}
		}
		/*
			���� �������� ���������� � ����� ������.
		 */
		TimelineFragment fragment = HomeTimelineFragment.newInstance();
		showFragment(fragment, HomeTimelineFragment.TAG);

	}

	/**
	 * �������� ������ ��������������� ������������.
	 * ��� ������ ����� � ��������� ������ ����������,
	 * ����� ����� �� ��������� � ���������, ��� ������ ��� ����������� ����� internet.
	 */
	private void getMyUID() {
		DataCache cache = DataCache.getInstance();
		Person me = null;
		try {
			me = loadPersonFromDb();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (me == null) {
			//�� ����� � ��, ����� ��������� � ��������� � ��/
			me = getMeFromNet();
			savePersonToDB(me);
		}
		cache.setConnectedUserID(me.getID());
		cache.putPerson(me.getID(), me);
	}

	/*
		TODO: �������� ���������� �� AsyncTask.
	 */
	private Person loadPersonFromDb() throws ExecutionException, InterruptedException {
		return Executors.newSingleThreadExecutor().submit(new Callable<Person>() {
			@Override
			public Person call() {
				AppDatabase appDb = new AppDatabase(getApplicationContext());
				SQLiteDatabase sqlDb = appDb.getReadableDatabase();
				String[] projection = {
						AppDatabase.FieldEntry.COLUMN_NAME_USER_ID,
						AppDatabase.FieldEntry.COLUMN_NAME_USER_NAME,
						AppDatabase.FieldEntry.COLUMN_NAME_USER_SCREEN_NAME,
						AppDatabase.FieldEntry.COLUMN_NAME_USER_LOCATION,
						AppDatabase.FieldEntry.COLUMN_NAME_USER_DESCRIPTION,
						AppDatabase.FieldEntry.COLUMN_NAME_USER_PROFILE_IMAGE
				};

				Cursor cursor = sqlDb.query(AppDatabase.FieldEntry.TABLE_NAME, projection, null, null, null, null, null);
				cursor.moveToFirst();
				long id = cursor.getLong(0);
				String name = cursor.getString(1);
				String screenName = cursor.getString(2);
				String loc = cursor.getString(3);
				String desc = cursor.getString(4);
				String profile = cursor.getString(5);
				Person me = new Person(name, screenName, profile, loc, desc, true, id);
				cursor.close();
				sqlDb.close();
				appDb.close();
				return me;
			}
		}).get();
	}

	/*
		TODO: �������� ���������� �� AsyncTask.
	 */
	private void savePersonToDB(final Person person) {
		System.out.println("savePersonToDB");
		Executors.newCachedThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				AppDatabase appDb = new AppDatabase(getApplicationContext());
				SQLiteDatabase sqlDb = appDb.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_ID, person.getID());
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_NAME, person.getName());
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_SCREEN_NAME, person.getScreenName());
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_LOCATION, person.getLocation());
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_DESCRIPTION, person.getDescription());
				values.put(AppDatabase.FieldEntry.COLUMN_NAME_USER_PROFILE_IMAGE, person.getProfileImage());
				sqlDb.insert(AppDatabase.FieldEntry.TABLE_NAME, null, values);
				sqlDb.close();
				appDb.close();
			}
		});
	}

	/*
		TODO: �������� ���������� �� AsyncTask.
	 */
	private Person getMeFromNet() {
		Person me = null;
		try {
			me = Executors.newSingleThreadExecutor().submit(new Callable<Person>() {
				@Override
				public Person call() {
					try {
						return connector.getAuthPerson();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return null;
				}
			}).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return me;
	}

	//------------------------------------------------------------------------------------------------------------------
	private void setTabs() {
		ActionBar aBar = getActionBar();
		if (aBar != null) {
			ActionBar.Tab tabHome = aBar.newTab().setText(R.string.menuitem_home_timeline).setTabListener(this);
			ActionBar.Tab tabFriends = aBar.newTab().setText(R.string.menuitem_friends).setTabListener(this);
			ActionBar.Tab tabMyTweets = aBar.newTab().setText(R.string.menuitem_my_tweets).setTabListener(this);

			aBar.addTab(tabHome);
			aBar.addTab(tabFriends);
			aBar.addTab(tabMyTweets);

			aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int selectedTab = getActionBar().getSelectedNavigationIndex();
		int menuType = (selectedTab == 0) ? R.menu.menu_home_timeline : (selectedTab == 1) ? R.menu.menu_followings : R.menu.menu_my_tweets;
		menu.clear();
		getMenuInflater().inflate(menuType, menu);

		if (menuType == R.menu.menu_home_timeline) {
			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			if (searchView == null) {
				return false;
			}

			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			ComponentName cn = new ComponentName(this, SearchableActivity.class);
			SearchableInfo info = searchManager.getSearchableInfo(cn);
			searchView.setSearchableInfo(info);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/*
		����� Settings � About ����� �������� �����, ������� ������������ �� �����.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_messages:
				//���� ���, TODO: ����� ������� �������� ��� ������ ��� ������ � �����������.
				Executors.newCachedThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						try {
							connector.getReceivedMessages(0, 0, 10);
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (java.text.ParseException e) {
							e.printStackTrace();
						}
					}
				});
				Toast.makeText(this, "NEED TO CREATE MESSAGE DIALOG/ACTIVITY", Toast.LENGTH_LONG).show();
				return true;

			case R.id.menu_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;

			case R.id.menu_about:
				Toast.makeText(this, "Nee to make ABOUT dialog", Toast.LENGTH_LONG).show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	//------------------------------------------------------------------------------------------------------------------

	void showFragment(Fragment fragment, String tag) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.animator.aminator_appearance, R.animator.animator_disappear);

		currentFragment = fragment;
		//�������� �������� ���������� ������, �������������� - � ������ �����.
		if (tag.equals(HomeTimelineFragment.TAG)) {
			ft.replace(R.id.frame_layout_timeline, fragment, tag);
		} else {
			showFragmentInRightPlace(ft, fragment, tag);
		}

		ft.commit();
	}

	/**
	 * � �������������� ��������� �� ������� ������ ����� ��������� ����������.
	 * ��� ������� ���������� ���. �������� � ������ �����.
	 * @param ft FragmentTransaction
	 * @param fragment fragment to show
	 * @param tag fragment tag
	 */
	private void showFragmentInRightPlace(FragmentTransaction ft, Fragment fragment, String tag) {
		switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
				ft.replace(R.id.frame_layout_timeline, fragment, tag);
				break;
			case Configuration.SCREENLAYOUT_SIZE_LARGE:
				ft.replace(R.id.frame_layout_for_different_fragments, fragment, tag);
				break;
		}
	}
	//------------------------------------------------------------------------------------------------------------------

	private void showFriends() {
		FollowingsFragment followingsFragment = (FollowingsFragment) getFragmentManager().findFragmentByTag(FollowingsFragment.TAG);
		if (followingsFragment == null) {
			followingsFragment = FollowingsFragment.getInstance();
		}
		showFragment(followingsFragment, FollowingsFragment.TAG);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void showMyTweets() {
		ConnectedUserTimelineFragment connectedUserTimelineFragment =
				(ConnectedUserTimelineFragment) getFragmentManager().findFragmentByTag(ConnectedUserTimelineFragment.TAG);
		if (connectedUserTimelineFragment == null) {
			connectedUserTimelineFragment = ConnectedUserTimelineFragment.newInstance();
		}

		showFragment(connectedUserTimelineFragment, ConnectedUserTimelineFragment.TAG);
	}

	//------------------------------------------------------------------------------------------------------------------
	private boolean isLandscapeOrientation() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		int tabPosition = tab.getPosition();
		switch (tabPosition) {
			case 0:
				Fragment homeTimelineFragment = getFragmentManager().findFragmentByTag(HomeTimelineFragment.TAG);
				if (homeTimelineFragment == null) {
					homeTimelineFragment = HomeTimelineFragment.newInstance();
				}
				showFragment(homeTimelineFragment, HomeTimelineFragment.TAG);
				break;
			case 1:
				showFriends();
				break;
			case 2:
				showMyTweets();
				break;
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		if (currentFragment != null && currentFragment instanceof ListFragment) {
			/*
				��� currentFragment != null, ������ listFragment ��� ���������� �
				getListView() ������ ������� ListView, � �� NPE.
			 */
			((ListFragment) currentFragment).getListView().setSelection(0);
		}
	}
	//------------------------------------------------------------------------------------------------------------------

}
