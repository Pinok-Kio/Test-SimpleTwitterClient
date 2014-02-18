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
	 * Минимальный период, через который можно подгружать новейшие твиты, в мс.
	 */
	private static final int NEWEST_TWEET_LOAD_PERIOD = 180_000;
	/**
	 * Время предыдущей загрузки новых твитов. Необходимо, чтобы сделать загрузку новых твитов раз в 3 минуты.
	 */
	private long prevLoadTime;
	/**
	 * За сколько твитов до конца начинать подгрузку новых твитов.
	 */
	static final int TWEETS_BEFORE_END = 20;
	public static final String TAG = "TAG_TIMELINE_FRAGMENT";
	private static final String SCROLL_POSITION = "SCROLL_POSITION";

	volatile boolean isLoading = false;   //Идет ли загрузка новых данных?
	volatile boolean needNewestTweet = true;  //Нужно ли грузить новые (новейшие) данные?
	volatile boolean needOldTweet = true; //Нужно ли грузить старые данные?

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
				Долистали до низу - подгружаем старые твиты (или наоборот - новые, если листаем вверх).
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
						Когда до конца списка остается "TWEETS_BEFORE_END" твитов, начинаем подгружать новую порцию.
						При этом возникает ситуация (из-за того, что загрузка происходит не моментально),
						когда новый запрос на загрузку отправляется до завершения текущей загрузки порции твитов.
						Для того, чтобы этого избежать, вводим переменную isLoading, которая является индикатором
						процесса загрузки.
						После завершения загружки производный класс должен установить переменную isLoading в false.
					 */
					if (!isLoading) {
						if (currentAdapter != null) {
							if (needOldTweet && ((firstVisibleItem + visibleItemCount) > (currentAdapter.getCount() - TWEETS_BEFORE_END))) {
								isLoading = true;
								loadOldItems();
							} else if (item > 0 && firstVisibleItem == 0) {
							/*
								Здесь просто проверяем, если уже показывали первый элемент в ListView, и теперь
								показываем его еще раз, значит мы пролистали вверх до упора и нужно бы подгрузить
								новейшие твиты.
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
		Т.к. все используемые адаптеры в приложении наследуют от TimelineAdapter,
		то ошибок быть не должно.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setListAdapter(ListAdapter adapter) {
		super.setListAdapter(adapter);
		currentAdapter = (TimelineAdapter<T>) adapter;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить элемент (Tweet или Person) на заданной позиции.
	 *
	 * @param position позиция выбранного элемента в ListView.
	 * @return выбанный элемент.
	 */
	T getItem(int position) {
		return (currentAdapter != null) ? (currentAdapter.getItem(position)) : null;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить параметр max_id для подгрузки твитов.
	 *
	 * @return max_id
	 */
	long getMaxId() {
		return (currentAdapter != null) ? currentAdapter.getMaxID() : 0;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить параметр since_id для загрузки новых твитов.
	 *
	 * @return since_id
	 */
	long getSinceID() {
		return (currentAdapter != null) ? currentAdapter.getSinceID() : 0;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Загрузка старых данный.
	 * Происходит, когда пользователь пролистывает все загруженные данные до конца.
	 */
	protected abstract void loadOldItems();

	/**
	 * Загрузить новейшие данные.
	 * Происходит, когда пользователь листает список в начало, не чаще одного раза в NEWEST_TWEET_LOAD_PERIOD мс.
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
