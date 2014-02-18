package com.example.Twitter_Android.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.Loaders.AsyncUserInfoLoader;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Logic.Constants;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.AppActivity.MainActivity;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

public class UserInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Person> {
	private MainActivity mainActivity;
	private static Connector connector;
	private ImageView userAvatar;
	private TextView userName;
	private TextView userInfo;
	private static ImageDownloader imageDownloader;
	private static final int USER_INFO_LOADER_ID = Constants.USER_INFO_LOADER_ID;

	public static final String ARG_ID = "USER_ID";
	public static final String TAG = "TAG_USER_INFO_FRAGMENT";
	//------------------------------------------------------------------------------------------------------------------

	public UserInfoFragment() {
		Log.i(TAG, "create new UserInfoFragment");
		if (imageDownloader == null) {
			imageDownloader = new ImageDownloader();
		}

		Bundle args = new Bundle();
		args.putLong(ARG_ID, 0);
		setArguments(args);
//		setRetainInstance(true);
	}

	public static UserInfoFragment newInstance(Connector c, long id) {
		connector = c;
		UserInfoFragment fragment = new UserInfoFragment();
		Bundle args = new Bundle();
		args.putLong(ARG_ID, id);
		fragment.setArguments(args);
		return fragment;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mainActivity.getLoaderManager().initLoader(USER_INFO_LOADER_ID, getArguments(), this).forceLoad();
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.i(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.layout_connected_user_info_fragment, container, false);
		userAvatar = (ImageView) v.findViewById(R.id.userinfo_fragment_user_avatar);
		userName = (TextView) v.findViewById(R.id.userinfo_fragment_username_textview);
		userInfo = (TextView) v.findViewById(R.id.userinfo_fragment_user_description_textView);
		return v;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public Loader<Person> onCreateLoader(int id, Bundle args) {
		return new AsyncUserInfoLoader(mainActivity, connector, args.getLong("ID_VALUE", 0));
	}

	@Override
	public void onLoadFinished(Loader<Person> loader, Person data) {
		if (loader.getId() == USER_INFO_LOADER_ID) {
			userName.setText(data.getName());
			userInfo.setText(data.getDescription());
			imageDownloader.loadBitmap(data.getProfileImage(), userAvatar);
		}
	}

	@Override
	public void onLoaderReset(Loader<Person> loader) {

	}
	//------------------------------------------------------------------------------------------------------------------

	public void loadNewInfo(Bundle args) {
		Log.i(TAG, "Loading new Info id = " + args.getLong("ID_VALUE"));
		mainActivity.getLoaderManager().restartLoader(USER_INFO_LOADER_ID, args, this).forceLoad();
	}
}
