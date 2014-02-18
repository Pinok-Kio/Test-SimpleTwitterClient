package com.example.Twitter_Android.Fragments.Preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.example.Twitter_Android.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
