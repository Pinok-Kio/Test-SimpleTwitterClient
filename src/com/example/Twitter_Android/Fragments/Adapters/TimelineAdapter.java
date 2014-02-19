package com.example.Twitter_Android.Fragments.Adapters;

import android.app.Activity;
import android.widget.BaseAdapter;
import com.example.Twitter_Android.Logic.Tweet;
import com.mass.cmassive.CMassive;

import java.util.List;

/**
 * Абстрактный адаптер для отображения списка твитов или пользователей (и сообщений).
 * Позволяет добавлять новый данные взамен текущих, в начало или в конец и удалять данные.
 *
 * @param <T> тип содержимого - используется Tweet, Person
 */
public abstract class TimelineAdapter<T> extends BaseAdapter {
	private CMassive<T> items;
	private long maxID = -1;    //see max_id & since_id in TwitterAPI
	private long sinceID = -1;
	private final String TAG;

	@SuppressWarnings("unchecked")
	TimelineAdapter(List<? extends T> newItems, String tag) {
		items = new CMassive<>(newItems);
		TAG = tag;
		if (newItems.get(0) instanceof Tweet) {
			sinceID = ((Tweet) newItems.get(0)).getID();
			maxID = ((Tweet) newItems.get(newItems.size() - 1)).getID() - 1;
			System.out.println("CONSTRUCTOR: maxID=" + maxID + ", sinceID=" + sinceID);
		}
	}

	@Override
	public int getCount() {
		return items.getDataSize();
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public T getItem(int position) {
		return items.getItem(position);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public long getItemId(int position) {
		return items.getItem(position).hashCode();
	}

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Add new items instead existing items
	 *
	 * @param newItems items to add
	 */
	@SuppressWarnings("unchecked")
	public void addItemsInstead(List<? extends T> newItems) {
		items = new CMassive<>(newItems);
		if (newItems.get(0) instanceof Tweet) {
			sinceID = ((Tweet) newItems.get(0)).getID();
			maxID = ((Tweet) newItems.get(newItems.size() - 1)).getID() - 1;
			System.out.println("addItemsInstead: maxID=" + maxID + ", sinceID=" + sinceID);
		}
		notifyDataSetChanged();
	}

	/**
	 * Add new items on top of the existing items array
	 *
	 * @param newItems items to add
	 */
	@SuppressWarnings("unchecked")
	public void addItemsToTop(List<? extends T> newItems) {
		items.insertToStart(newItems);
		if (newItems.get(0) instanceof Tweet) {
			sinceID = ((Tweet) newItems.get(0)).getID();
			System.out.println("addItemsToTop: maxID=" + maxID +
					" sinceID=" + sinceID +
					" tmp[0]=" + (((Tweet) newItems.get(0)).getID()) +
					" tmp[last]=" + (((Tweet) newItems.get(newItems.size() - 1)).getID()) + " mewItems.size=" + newItems.size());
		}
		notifyDataSetChanged();
	}

	/**
	 * Add new items at the end of the existing items array
	 *
	 * @param newItems items to add
	 */
	@SuppressWarnings("unchecked")
	public void addItemsToBottom(List<? extends T> newItems) {
		items.insertToEnd(newItems);
		if (newItems.get(0) instanceof Tweet) {
			maxID = ((Tweet) newItems.get(newItems.size() - 1)).getID() - 1;
			System.out.println("addItemsToBottom: maxID=" + maxID + ", sinceID=" + sinceID);
		}
		notifyDataSetChanged();
	}

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Remove item from array and compact array
	 *
	 * @param item item to remove
	 */
	@SuppressWarnings("unchecked")
	public void removeItem(T item) {
		items.removeItem(item);
		notifyDataSetChanged();
	}

	/**
	 * Remove item from position in array
	 *
	 * @param position item position to remove
	 */
	@SuppressWarnings("unchecked")
	public void removeItem(int position) {
		items.removeItem(position);
		notifyDataSetChanged();
	}

	/**
	 * Replace last added tweet. Using in PostTweetDialog
	 *
	 * @param tweet update tweet.
	 */
	public void updateLastAddedTweet(T tweet) {
		items.updateItem(tweet, 0);
		notifyDataSetChanged();
	}

	//------------------------------------------------------------------------------------------------------------------
	public synchronized long getMaxID() {
		return maxID;
	}

	public synchronized long getSinceID() {
		return sinceID;
	}
	//------------------------------------------------------------------------------------------------------------------

	public abstract void updateContext(Activity context);

	public String getTag() {
		return TAG;
	}
}
