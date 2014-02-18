package com.example.Twitter_Android.Fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;

import java.util.List;

public abstract class TimelineFragment<T> extends ListFragment implements LoaderManager.LoaderCallbacks<List<? extends T>>,
		AdapterView.OnItemLongClickListener {

	private TimelineAdapter<T> currentAdapter;
	private final DataCache cache;
	private int currentScrollPosition;
	private int selectedListItemPosition = 0;

	/**
	 * ����������� ������, ����� ������� ����� ���������� �������� �����, � ��.
	 */
	private static final int NEWEST_TWEET_LOAD_PERIOD = 180_000;
	/**
	 * ����� ���������� �������� ����� ������. ����������, ����� ������� �������� ����� ������ ��� � 3 ������.
	 */
	private long prevLoadTime;
	/**
	 * �� ������� ������ �� ����� �������� ��������� ����� ������.
	 */
	static final int TWEETS_BEFORE_END = 20;
	public static final String TAG = "TAG_TIMELINE_FRAGMENT";
	private static final String SCROLL_POSITION = "SCROLL_POSITION";

	volatile boolean isLoading = false;   //���� �� �������� ����� ������?
	volatile boolean needNewestTweet = true;  //����� �� ������� ����� (��������) ������?
	volatile boolean needOldTweet = true; //����� �� ������� ������ ������?

	TimelineFragment() {
		prevLoadTime = System.currentTimeMillis();
		cache = DataCache.getInstance();
	}

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final ListView listView = getListView();
		if (listView != null) {
			listView.setOnItemLongClickListener(this);
			/*
				��������� �� ���� - ���������� ������ ����� (��� �������� - �����, ���� ������� �����).
			 */
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				private int item = 0;

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					item += firstVisibleItem;
					/*
						����� �� ����� ������ �������� "TWEETS_BEFORE_END" ������, �������� ���������� ����� ������.
						��� ���� ��������� �������� (��-�� ����, ��� �������� ���������� �� �����������),
						����� ����� ������ �� �������� ������������ �� ���������� ������� �������� ������ ������.
						��� ����, ����� ����� ��������, ������ ���������� isLoading, ������� �������� �����������
						�������� ��������.
						����� ���������� �������� ����������� ����� ������ ���������� ���������� isLoading � false.
					 */
					if (!isLoading) {
						if (currentAdapter != null) {
							if (needOldTweet && ((firstVisibleItem + visibleItemCount) > (currentAdapter.getCount() - TWEETS_BEFORE_END))) {
								isLoading = true;
								loadOldItems();
							} else if (item > 0 && firstVisibleItem == 0) {
							/*
								����� ������ ���������, ���� ��� ���������� ������ ������� � ListView, � ������
								���������� ��� ��� ���, ������ �� ���������� ����� �� ����� � ����� �� ����������
								�������� �����.
							 */
								if (needNewestTweet && timeToLoad()) {
									item = 0;
									isLoading = true;
									loadNewestItems();
									prevLoadTime = System.currentTimeMillis();
								}
							}
						}
					}
				}
			});

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
					selectedListItemPosition = position;
				}
			});
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	private boolean timeToLoad() {
		return (System.currentTimeMillis() - prevLoadTime) >= NEWEST_TWEET_LOAD_PERIOD;
	}

	/*
		�.�. ��� ������������ �������� � ���������� ��������� �� TimelineAdapter,
		�� ������ ���� �� ������.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setListAdapter(ListAdapter adapter) {
		super.setListAdapter(adapter);
		currentAdapter = (TimelineAdapter<T>) adapter;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * �������� ������� (Tweet ��� Person) �� �������� �������.
	 *
	 * @param position ������� ���������� �������� � ListView.
	 * @return �������� �������.
	 */
	T getItem(int position) {
		return (currentAdapter != null) ? (currentAdapter.getItem(position)) : null;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * �������� �������� max_id ��� ��������� ������.
	 *
	 * @return max_id
	 */
	long getMaxId() {
		return (currentAdapter != null) ? currentAdapter.getMaxID() : 0;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * �������� �������� since_id ��� �������� ����� ������.
	 *
	 * @return since_id
	 */
	long getSinceID() {
		return (currentAdapter != null) ? currentAdapter.getSinceID() : 0;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * �������� ������ ������.
	 * ����������, ����� ������������ ������������ ��� ����������� ������ �� �����.
	 */
	protected abstract void loadOldItems();

	/**
	 * ��������� �������� ������.
	 * ����������, ����� ������������ ������� ������ � ������, �� ���� ������ ���� � NEWEST_TWEET_LOAD_PERIOD ��.
	 */
	void loadNewestItems(){

	}
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public abstract Loader<List<? extends T>> onCreateLoader(int id, Bundle args);

	@Override
	public abstract void onLoadFinished(Loader<List<? extends T>> loader, List<? extends T> data);

	@Override
	public abstract void onLoaderReset(Loader<List<? extends T>> loader);

	//------------------------------------------------------------------------------------------------------------------
	@Override
	public abstract boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			currentScrollPosition = savedInstanceState.getInt(SCROLL_POSITION, 0);
		}
		if (currentAdapter != null && getListView() != null) {
			getListView().setSelection(currentScrollPosition);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (getListView() != null) {
			currentScrollPosition = getListView().getFirstVisiblePosition();
			outState.putInt(SCROLL_POSITION, currentScrollPosition);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (currentAdapter != null && getListView() != null) {
			setListAdapter(currentAdapter);
			getListView().setSelection(currentScrollPosition);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (getListView() != null) {
			currentScrollPosition = getListView().getFirstVisiblePosition();
		}

		if (currentAdapter != null) {
			cache.saveAdapter(currentAdapter.getTag(), currentAdapter);
		}
	}

	T getSelectedItem() {
		return (currentAdapter != null) ? currentAdapter.getItem(selectedListItemPosition) : null;
	}

	int getSelectedListItemPosition() {
		return selectedListItemPosition;
	}
}
