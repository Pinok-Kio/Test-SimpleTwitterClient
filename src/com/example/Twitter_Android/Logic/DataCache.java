package com.example.Twitter_Android.Logic;
/**
 *  Типа кэш.
 */

import android.graphics.Bitmap;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DataCache {
	private static final Map<String, Bitmap> bitmaps = new HashMap<>();
	private static final Map<Long, Person> personInfo = new ConcurrentHashMap<>();
	private static final Map<String, BaseAdapter> adapters = new ConcurrentHashMap<>();
	private static final DataCache instance = new DataCache();
	private long bitmapCacheSize;
	private static final int MAX_CACHE_SIZE = 32 * 1024 * 1024;
	private int maxBitmapDimension = 500;
	private long connectedUserID;

	private DataCache() {
	}

	public static DataCache getInstance() {
		return instance;
	}

	public void putImage(String key, Bitmap image) {
		bitmapCacheSize += image.getByteCount();
		if (bitmapCacheSize > MAX_CACHE_SIZE) {
			bitmaps.clear();
			bitmapCacheSize = 0;
			System.out.println("CLEAR CACHE");
		}
		bitmaps.put(key, image);
	}

	public Bitmap getImage(String key) {
		return bitmaps.get(key);

	}

	public void putPerson(long key, Person person) {
		personInfo.put(key, person);
	}

	public Person getPerson(long key) {
		return personInfo.get(key);
	}

	public long getConnectedUserID() {
		return connectedUserID;
	}

	public void setConnectedUserID(long uid) {
		if (connectedUserID == 0) {
			connectedUserID = uid;
		}
	}

	public void setMaxBitmapDimension(int dimension) {
		maxBitmapDimension = dimension;
	}

	public int getMaxBitmapDimension() {
		return maxBitmapDimension;
	}

	public void saveAdapter(String key, BaseAdapter toSave) {
		adapters.put(key, toSave);
	}

	public BaseAdapter getAdapter(String key) {
		return adapters.get(key);
	}
}
