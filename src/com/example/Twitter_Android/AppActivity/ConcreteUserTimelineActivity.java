package com.example.Twitter_Android.AppActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import com.example.Twitter_Android.Fragments.ConcreteUserTimelineFragment;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.R;

/*
	TODO: menu, adapter.
 */
public class ConcreteUserTimelineActivity extends Activity {
	private ImageView avatar;
	private Person currentPerson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_concrete_person_tweets);
		DataCache cache = DataCache.getInstance();

		Intent callingIntent = getIntent();
		long person_id = callingIntent.getLongExtra("PERSON_ID", 0);
		currentPerson = cache.getPerson(person_id);

		avatar = (ImageView) findViewById(R.id.friend_avatar_imageview);
		avatar.setImageBitmap(cache.getImage(currentPerson.getProfileImage()));

		setTitle(currentPerson.getName());

		showTweets();
	}

	private void showTweets() {
		Fragment fragment = ConcreteUserTimelineFragment.newInstance(currentPerson, avatar);
		getFragmentManager().beginTransaction().replace(R.id.frame_layout_timeline, fragment, ConcreteUserTimelineFragment.TAG).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_concrete_user_timeline, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		ActionBar aBar = getActionBar();
		if (aBar != null) {
			aBar.setTitle(title);
		}
	}
}
