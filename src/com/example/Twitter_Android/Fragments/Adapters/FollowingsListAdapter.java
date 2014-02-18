package com.example.Twitter_Android.Fragments.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.R;

import java.util.List;

public class FollowingsListAdapter extends TimelineAdapter<Person> {
	private final LayoutInflater layoutInflater;
	private final ImageDownloader imageDownloader;
	private final DataCache cache;
	public static final String TAG = "FOLLOWING_LIST_ADAPTER";
	private boolean isImagesLoadingAllowed;

	//------------------------------------------------------------------------------------------------------------------

	private static class ViewHolder {
		TextView name;
		TextView screenName;
		TextView from;
		TextView text;
		ImageView avatar;
	}
	//------------------------------------------------------------------------------------------------------------------

	public FollowingsListAdapter(Activity activity, List<? extends Person> persons) {
		super(persons, TAG);
		layoutInflater = LayoutInflater.from(activity);
		imageDownloader = new ImageDownloader();
		cache = DataCache.getInstance();
		isImagesLoadingAllowed = PreferenceManager.getDefaultSharedPreferences(activity)
				.getBoolean(SettingsActivity.ALLOW_IMAGE_LOADING, true);
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public int getItemViewType(int position) {
		return 0;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final Person person = getItem(position);
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.single_friend_layout, null);
			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView.findViewById(R.id.friend_avatar_imageview);
			holder.name = (TextView) convertView.findViewById(R.id.friend_name_textview);
			holder.screenName = (TextView) convertView.findViewById(R.id.friend_screen_name_textview);
			holder.from = (TextView) convertView.findViewById(R.id.friend_from_textview);
			holder.text = (TextView) convertView.findViewById(R.id.friend_info_textview);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(person.getName());
		holder.screenName.setText("@" + person.getScreenName());
		holder.from.setText("From: " + person.getLocation());
		holder.text.setText(person.getDescription());

		if (isImagesLoadingAllowed) {
			Bitmap bitmapAvatar = cache.getImage(person.getProfileImage());
			if (bitmapAvatar == null) {
				imageDownloader.loadBitmap(person.getProfileImage(), holder.avatar);
			} else {
				holder.avatar.setImageBitmap(bitmapAvatar);
			}
		}
		return convertView;
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void updateContext(Activity context) {

	}
}
