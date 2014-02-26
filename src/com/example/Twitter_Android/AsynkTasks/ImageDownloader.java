package com.example.Twitter_Android.AsynkTasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.example.Twitter_Android.Logic.DataCache;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageDownloader {
	private static final DataCache cache = DataCache.getInstance();

	//------------------------------------------------------------------------------------------------------------------
	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;
		private final WeakReference<ImageView> imageViewReference;
		private final WeakReference<ProgressBar> progressBar;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<>(imageView);
			progressBar = null;
		}

		public BitmapDownloaderTask(ImageView imageView, ProgressBar progress) {
			imageViewReference = new WeakReference<>(imageView);
			progressBar = new WeakReference<>(progress);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downloadBitmap(params[0]);
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			ImageView imageView = imageViewReference.get();
			final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
			if (this == bitmapDownloaderTask && imageView != null) {
				imageView.setImageBitmap(bitmap);
				if (!imageView.isShown()) {
					imageView.setVisibility(View.VISIBLE);
				}
			}
			if (progressBar != null) {
				ProgressBar bar = progressBar.get();
				if (bar != null) {
					bar.setVisibility(View.GONE);
				}
			}
		}
	}
	//------------------------------------------------------------------------------------------------------------------

	private static class AsyncDrawable extends ColorDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public AsyncDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
			super(Color.BLACK);
			bitmapDownloaderTaskReference = new WeakReference<>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}
	//------------------------------------------------------------------------------------------------------------------

	private static boolean cancelPotentialWork(String data, ImageView imageView) {
		final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			final String bitmapData = bitmapDownloaderTask.url;
			if (bitmapData != null && !bitmapData.equals(data)) {
				// Cancel previous task
				bitmapDownloaderTask.cancel(true);
			} else if (bitmapData == null) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}
	//------------------------------------------------------------------------------------------------------------------

	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}
	//------------------------------------------------------------------------------------------------------------------

	public void loadBitmap(String imageUrl, ImageView imageView) {
		if (cancelPotentialWork(imageUrl, imageView)) {
			final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(imageUrl);
		}
	}

	public void loadBitmap(String imageUrl, ImageView imageView, ProgressBar progress) {
		if (cancelPotentialWork(imageUrl, imageView)) {
			final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, progress);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(imageUrl);
		}
	}

	//------------------------------------------------------------------------------------------------------------------

	private Bitmap downloadBitmap(String url) {
		Bitmap cachedBitmap = cache.getImage(url);
		if (cachedBitmap == null) {
			final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			final HttpGet getRequest = new HttpGet(url);

			try {
				HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					return null;
				}

				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						inputStream = entity.getContent();
						final Bitmap bitmap = getScaledBitmap(inputStream);
						cache.putImage(url, bitmap);
						return bitmap;
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			} catch (Exception e) {
				getRequest.abort();
			} finally {
				client.close();
			}
			return null;
		}
		return cachedBitmap;
	}
	//------------------------------------------------------------------------------------------------------------------

	private Bitmap getScaledBitmap(InputStream inputStream) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		final int MAX_DIMENSION = cache.getScreenWidth();

		if (bitmapHeight > MAX_DIMENSION) {
			float scale = (float) MAX_DIMENSION / (float) bitmapHeight;
			bitmapHeight = MAX_DIMENSION;
			int newWidth = (int) (bitmapWidth * scale);

			Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, newWidth, bitmapHeight, true);
			bitmap = compressBitmap(bitmap1);

		} else if (bitmapWidth > MAX_DIMENSION) {
			float scale = (float) MAX_DIMENSION / (float) bitmapWidth;
			bitmapWidth = MAX_DIMENSION;
			int newHeight = (int) (bitmapHeight * scale);

			Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, bitmapWidth, newHeight, true);
			bitmap = compressBitmap(bitmap1);

		} else if (bitmapWidth > 100) {
			bitmap = compressBitmap(bitmap);
		}
		return bitmap;
	}
	//------------------------------------------------------------------------------------------------------------------

	private Bitmap compressBitmap(Bitmap b) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Bitmap bitmap = null;
		if (b.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
			bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
