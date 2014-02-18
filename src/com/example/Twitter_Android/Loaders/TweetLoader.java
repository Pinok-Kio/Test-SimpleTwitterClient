package com.example.Twitter_Android.Loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.preference.PreferenceManager;
import com.example.Twitter_Android.AppActivity.SettingsActivity;

import java.util.List;

abstract class TweetLoader<T> extends AsyncTaskLoader<List<? extends T>> {
	protected final int RESULT_COUNT;
	private List<? extends T> loadedTweets;
	private static final String DEFAULT_TWEET_COUNT = "50";

	//------------------------------------------------------------------------------------------------------------------
	TweetLoader(Context context) {
		super(context);
		String value = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ALLOWED_TWEET_COUNT, DEFAULT_TWEET_COUNT);
		RESULT_COUNT = Integer.valueOf(value);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void deliverResult(List<? extends T> data) {
		if (isReset()) {
			releaseResources(data);
			return;
		}
		List<? extends T> oldData = loadedTweets;
		loadedTweets = data;

		if (isStarted()) {
			super.deliverResult(data);
		}

		if (oldData != null && oldData != data) {
			releaseResources(oldData);
		}
	}

	void setLoadedTweets(List<? extends T> loadedData) {
		loadedTweets = loadedData;
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		if (loadedTweets != null && loadedTweets.size() > 0) {
			deliverResult(loadedTweets);
		} else {
			forceLoad();
		}
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onReset() {
		// Ensure the loader has been stopped.
		onStopLoading();

		// At this point we can release the resources associated with 'mData'.
		if (loadedTweets != null) {
			releaseResources(loadedTweets);
			loadedTweets = null;
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCanceled(List<? extends T> data) {
		super.onCanceled(data);
		releaseResources(data);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public abstract List<? extends T> loadInBackground();
	//------------------------------------------------------------------------------------------------------------------

	private void releaseResources(List<? extends T> data) {
		if (data != null) {
			data.clear();
		}
	}
}
