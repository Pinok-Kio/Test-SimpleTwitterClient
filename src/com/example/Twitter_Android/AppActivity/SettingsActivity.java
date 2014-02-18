package com.example.Twitter_Android.AppActivity;

import android.app.Activity;
import android.os.Bundle;
import com.example.Twitter_Android.Fragments.Preferences.SettingsFragment;
import com.example.Twitter_Android.R;

public class SettingsActivity extends Activity {
	public static final String ALLOW_IMAGE_LOADING = "key_pref_images_loading";
	public static final String ALLOWED_TWEET_COUNT = "key_pref_number_of_tweets";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.animator.aminator_appearance, R.animator.animator_disappear)
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}
}
