package com.example.Twitter_Android.Fragments.Adapters;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Tweet;

import java.util.List;

public class MessagesAdapter extends TimelineAdapter<Tweet> {
	private final LayoutInflater layoutInflater;
	private final DataCache cache;
	private final ImageDownloader imageDownloader;
	private Activity mainActivity;
	private static boolean isImagesLoadingAllowed;

	private static class Holder{
		ImageView avatar;
		TextView name;
	}

	public MessagesAdapter(Activity activity, List<Tweet> newItems, String tag) {
		super(newItems, tag);
		layoutInflater = LayoutInflater.from(activity);
		cache = DataCache.getInstance();
		imageDownloader = new ImageDownloader();
		mainActivity = activity;
		isImagesLoadingAllowed = PreferenceManager.getDefaultSharedPreferences(activity)
				.getBoolean(SettingsActivity.ALLOW_IMAGE_LOADING, true);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){

		}else{

		}
		return null;
	}


	@Override
	public void updateContext(Activity context) {

	}
}
