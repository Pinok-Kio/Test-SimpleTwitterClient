package com.example.Twitter_Android.Fragments.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.Twitter_Android.AppActivity.SettingsActivity;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Fragments.Dialogs.ReplyDialog;
import com.example.Twitter_Android.Fragments.Dialogs.RetweetDialog;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

/**
 * Адаптер для отображения списка твитов.
 */
public class TweetAdapter extends TimelineAdapter<Tweet> {
	private final LayoutInflater layoutInflater;
	private final DataCache cache;
	private final ImageDownloader imageDownloader;
	private Activity mainActivity;
	private static boolean isImagesLoadingAllowed;

	private static class ViewHolder {
		TextView retweetedBy;
		TextView name;
		TextView screenName;
		TextView date;
		TextView text;
		ImageView avatar;
		ImageView attachedImage;
		Button buttonShowImage;
		ImageButton buttonReply;
		ImageButton buttonRetweet;
		ImageButton buttonFavorite;
		ProgressBar progress;
	}

	//------------------------------------------------------------------------------------------------------------------
	public TweetAdapter(Activity activity, List<? extends Tweet> newTweets, String tag) {
		super(newTweets, tag);
		layoutInflater = LayoutInflater.from(activity);
		mainActivity = activity;
		cache = DataCache.getInstance();
		imageDownloader = new ImageDownloader();
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
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.single_tweet_layout, null);
			holder = new ViewHolder();
			holder.retweetedBy = (TextView) convertView.findViewById(R.id.retweeted_by);
			holder.name = (TextView) convertView.findViewById(R.id.username_textview);
			holder.screenName = (TextView) convertView.findViewById(R.id.user_screen_name_textview);
			holder.date = (TextView) convertView.findViewById(R.id.tweet_date_textview);
			holder.text = (TextView) convertView.findViewById(R.id.tweet_text_textview);
			holder.avatar = (ImageView) convertView.findViewById(R.id.user_avatar_imageview);
			holder.attachedImage = (ImageView) convertView.findViewById(R.id.attached_image);
			holder.buttonShowImage = (Button) convertView.findViewById(R.id.button_show_image);
			holder.buttonReply = (ImageButton) convertView.findViewById(R.id.image_button_reply);
			holder.buttonRetweet = (ImageButton) convertView.findViewById(R.id.image_button_retweet);
			holder.buttonFavorite = (ImageButton) convertView.findViewById(R.id.image_button_favorite);
			holder.progress = (ProgressBar) convertView.findViewById(R.id.image_loading_progress);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Tweet tweet = getItem(position);
		final Person author = tweet.getAuthor();

		if (tweet.isRetweeted()) {
			Person p = tweet.retweetedBy();
			holder.retweetedBy.setText(mainActivity.getString(R.string.text_retweeted_by) + " " + p.getName() + " @" + p.getScreenName());
			holder.retweetedBy.setVisibility(View.VISIBLE);
		} else {
			holder.retweetedBy.setVisibility(View.GONE);
		}

		holder.name.setText(author.getName());
		holder.screenName.setText("@" + author.getScreenName());
		holder.date.setText(tweet.getCreationTime());
		holder.text.setText(tweet.getText());
		/*
			TextView с распознанной гиперссылкой перехватывает фокус, т.о. становится невозможна корректная
			обработка касаний. Поэтому, делаем так.
			TODO:   Подумать, как можно (если можно) сделать аккуратнее.
		 */
		if (tweet.hasLinkInText()) {
			holder.text.setFocusable(false);
			holder.text.setFocusableInTouchMode(false);
		}
		//--------Загружаем аватарку-----------------------------------
		if (isImagesLoadingAllowed) {
			Bitmap bitmapAvatar = cache.getImage(tweet.getAuthor().getProfileImage());
			if (bitmapAvatar == null) {
				imageDownloader.loadBitmap(tweet.getAuthor().getProfileImage(), holder.avatar);
			} else {
				holder.avatar.setImageBitmap(bitmapAvatar);
			}
		}
		//--------Загружаем приложенное изображение---------------------
		if (tweet.hasMedia()) {
			Bitmap attachedBitmap = cache.getImage(tweet.getMediaUrl());
			if (attachedBitmap != null) {
				//Показываем картинку, убираем кнопку.
				holder.attachedImage.setImageBitmap(attachedBitmap);
				holder.attachedImage.setVisibility(View.VISIBLE);
				holder.buttonShowImage.setVisibility(View.GONE);
			} else {
				holder.attachedImage.setVisibility(View.GONE);
				holder.buttonShowImage.setVisibility(View.VISIBLE);
				holder.buttonShowImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//Показываем прогрессбар, грузим картинку, убираем кнопку.
						holder.progress.setVisibility(View.VISIBLE);
						imageDownloader.loadBitmap(tweet.getMediaUrl(), holder.attachedImage, holder.progress);
						v.setVisibility(View.GONE);
					}
				});
				holder.buttonShowImage.setFocusable(false);
			}
		} else {
			holder.attachedImage.setVisibility(View.GONE);
			holder.buttonShowImage.setVisibility(View.GONE);
			holder.progress.setVisibility(View.GONE);
		}

		holder.buttonReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reply(tweet);
			}
		});

		holder.buttonRetweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				retweet(tweet);
			}
		});

		holder.buttonFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mainActivity, "Need to create favorite method", Toast.LENGTH_LONG).show();
			}
		});

		holder.buttonReply.setFocusable(false);
		holder.buttonRetweet.setFocusable(false);
		holder.buttonFavorite.setFocusable(false);

		return convertView;
	}

	//------------------------------------------------------------------------------------------------------------------
	private void reply(Tweet toReply) {
		ReplyDialog dialog = ReplyDialog.getInstance(toReply);
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), ReplyDialog.TAG);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void retweet(Tweet toRetweet) {
		RetweetDialog dialog = RetweetDialog.getInstance(toRetweet);
		dialog.show(mainActivity.getFragmentManager().beginTransaction(), RetweetDialog.TAG);
	}

	//------------------------------------------------------------------------------------------------------------------
	//TODO: потом избавиться от этих методов.
	@Override
	public void updateContext(Activity activity) {
		mainActivity = activity;
	}
}
