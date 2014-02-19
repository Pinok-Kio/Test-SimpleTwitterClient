package com.example.Twitter_Android.AppActivity;

import android.app.*;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.Twitter_Android.Fragments.*;
import com.example.Twitter_Android.Fragments.Dialogs.ErrorDialog;
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
	private int screenSize;                     //normal? large? xlarge?
	private TimelineFragment currentFragment;   //Right side fragment

	public MainActivity() {
		connector = new Connector();
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
			Чтобы при каждом вызове onCreate (например, когда возвращаемся из другой Activity)
			не загружать токен, делаем проверку.
		 */
		if (connector.isAuthorized()) {
			authorizationEnded();
		} else {
			final FileWorker fw = new FileWorker(this);
			/*
				Попробуем загрузить токен. Если удалось - значит приложение авторизировано и можно работать.
				Иначе - начинаем авторизацию.
			 */
			if (fw.loadAccessToken()) {
				connector.setAccessToken(fw.getAccessToken());
				authorizationEnded();
			} else {
				setContentView(R.layout.layout_enter_auth_code);
			}
		}
	}

	//--------------------AUTHORIZATION---------------------------------------------------------------------------------

	/**
	 * Button для авторизации из layout_enter_auth_code.xml
	 *
	 * @param v button
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void buttonClickHandler(View v) throws ExecutionException, InterruptedException {
		/*
			TODO: сделать как-нибудь по-другому.
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

	private void authorizationEnded() {
		setContentView(R.layout.main_screen_layout);
		setScreenSizePix();
		getMyUID();
		startDataLoading();
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Define max image width for showing images.
	 */
	private void setScreenSizePix() {
		DataCache cache = DataCache.getInstance();
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		cache.setScreenWidth(size.x);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void startDataLoading() {
		Configuration configuration = getResources().getConfiguration();
		screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

		if (isLandscapeOrientation() && screenSize != Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			showHomeTimeline();
		}

		setTabs();
	}

	/**
	 * Загрузка данных авторизованного пользователя.
	 * Эти данные нужны в некоторых местах приложения,
	 * лучше будем их сохранять и загружать, чем каждый раз запрашивать через internet.
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
			//не нашли в БД, тогда загружаем и сохраняем в БД/
			me = getMeFromNet();
			savePersonToDB(me);
		}
		cache.setConnectedUserID(me.getID());
		cache.putPerson(me.getID(), me);
	}

	/*
		TODO: возможно переделать на AsyncTask.
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
		TODO: возможно переделать на AsyncTask.
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
		TODO: возможно переделать на AsyncTask.
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
			if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
				ActionBar.Tab tabHome = aBar.newTab().setText(R.string.menuitem_home_timeline).setTabListener(this);
				aBar.addTab(tabHome);
			}
			ActionBar.Tab tabMyTweets = aBar.newTab().setText(R.string.menuitem_my_tweets).setTabListener(this);
			ActionBar.Tab tabFriends = aBar.newTab().setText(R.string.menuitem_friends).setTabListener(this);
			ActionBar.Tab tabFollowers = aBar.newTab().setText(R.string.menuitem_my_followers).setTabListener(this);
			aBar.addTab(tabMyTweets);
			aBar.addTab(tabFriends);
			aBar.addTab(tabFollowers);
			aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
	}

	/*
		Опции Settings и About будут доступны везде, поэтому обрабатываем их здесь.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_messages:
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

		//Основной фрагмент показываем всегда, дополнительный - в нужном месте.
		if (tag.equals(HomeTimelineFragment.TAG)) {
			ft.replace(R.id.frame_layout_timeline, fragment, tag);
			ft.commit();
		} else {
			showFragmentInRightPlace(ft, fragment, tag);
		}
	}

	/**
	 * В горизонтальном положении на большом экране будет несколько фрагментов.
	 * Эта функция показывает доп. фрагмент в нужном месте.
	 *
	 * @param ft       FragmentTransaction
	 * @param fragment fragment to show
	 * @param tag      fragment tag
	 */
	private void showFragmentInRightPlace(FragmentTransaction ft, Fragment fragment, String tag) {
		if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			ft.replace(R.id.frame_layout_timeline, fragment, tag);
		} else {
			ft.replace(R.id.frame_layout_other_fragments, fragment, tag);
		}
		ft.commit();
	}
	//------------------------------------------------------------------------------------------------------------------

	private void showFriends() {
		TimelineFragment followingsFragment =
				(FollowingsFragment) getFragmentManager().findFragmentByTag(FollowingsFragment.TAG);
		if (followingsFragment == null) {
			followingsFragment = FollowingsFragment.getInstance();
		}
		currentFragment = followingsFragment;
		showFragment(followingsFragment, FollowingsFragment.TAG);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void showMyTweets() {
		TimelineFragment myTweets =
				(ConnectedUserTimelineFragment) getFragmentManager().findFragmentByTag(ConnectedUserTimelineFragment.TAG);
		if (myTweets == null) {
			myTweets = ConnectedUserTimelineFragment.getInstance();
		}
		currentFragment = myTweets;
		showFragment(myTweets, ConnectedUserTimelineFragment.TAG);
	}

	private void showHomeTimeline() {
		TimelineFragment homeTimelineFragment =
				(HomeTimelineFragment) getFragmentManager().findFragmentByTag(HomeTimelineFragment.TAG);
		if (homeTimelineFragment == null) {
			homeTimelineFragment = HomeTimelineFragment.newInstance();
		}
		if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			currentFragment = homeTimelineFragment;
		}
		showFragment(homeTimelineFragment, HomeTimelineFragment.TAG);
	}

	private void showMyFollowers() {
		TimelineFragment myFollowers = (FollowersFragment) getFragmentManager().findFragmentByTag(FollowersFragment.TAG);
		if (myFollowers == null) {
			myFollowers = FollowersFragment.getInstance();
		}
		currentFragment = myFollowers;
		showFragment(myFollowers, FollowersFragment.TAG);

	}

	//------------------------------------------------------------------------------------------------------------------
	private boolean isLandscapeOrientation() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		int currentSelectedTab = tab.getPosition();
		if (isLandscapeOrientation() && screenSize != Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			currentSelectedTab++;
		}
		switch (currentSelectedTab) {
			case 0:
				showHomeTimeline();
				break;
			case 1:
				showMyTweets();
				break;
			case 2:
				showFriends();
				break;
			case 3:
				showMyFollowers();
				break;
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

	}

	/*
		Show first list element
	 */
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		if (currentFragment != null && currentFragment.isVisible()) {
			ListView lv = currentFragment.getListView();
			if (lv != null) {
				lv.setSelection(0);
			}
		}
	}
}
