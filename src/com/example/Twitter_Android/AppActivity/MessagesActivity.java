package com.example.Twitter_Android.AppActivity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ListView;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Fragments.Adapters.TweetAdapter;
import com.example.Twitter_Android.Loaders.MessageLoader;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Tweet;

import java.util.List;

public class MessagesActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Tweet>> {
	private static final String ADAPTER_TAG = "M_A_ADAPTER";
	private TimelineAdapter<Tweet> adapter;
	private final int MESSAGE_LOADER = 123;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = (TimelineAdapter<Tweet>) DataCache.getInstance().getAdapter(ADAPTER_TAG);
		if (adapter == null) {
			getLoaderManager().initLoader(MESSAGE_LOADER, null, this);
		}
	}

	@Override
	public Loader<List<Tweet>> onCreateLoader(int id, Bundle args) {
		return new MessageLoader(this, 0, 0);
	}

	@Override
	public void onLoadFinished(Loader<List<Tweet>> loader, List<Tweet> data) {
		if (loader.getId() == MESSAGE_LOADER) {
			if (adapter == null) {
				adapter = new TweetAdapter(this, data, ADAPTER_TAG);
				ListView lv = getListView();
				lv.setAdapter(adapter);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Tweet>> loader) {

	}
}
