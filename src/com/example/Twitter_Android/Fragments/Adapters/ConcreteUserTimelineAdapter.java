package com.example.Twitter_Android.Fragments.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Fragments.Dialogs.ReplyDialog;
import com.example.Twitter_Android.Fragments.Dialogs.RetweetDialog;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.List;

/**
 * Адаптер для отображения TimeLine конкретного пользователя.
 * TODO: поразмыслить над граф. интерфейсом, чтобы можно было использовать меньше адаптеров.
 */
public class ConcreteUserTimelineAdapter extends TimelineAdapter<Tweet> {
	private final LayoutInflater layoutInflater;
	private Activity mainActivity;
	private final ImageDownloader imageDownloader;

	//------------------------------------------------------------------------------------------------------------------
	private static class ViewHolder {
		TextView retweetedBy;
		TextView name;
		TextView screenName;
		TextView date;
		TextView text;
		ImageView attachedImage;
		Button button;
		ImageButton buttonReply;
		ImageButton buttonRetweet;
		ImageButton buttonFavorite;
	}
	//------------------------------------------------------------------------------------------------------------------

	public ConcreteUserTimelineAdapter(Activity activity, List<? extends Tweet> newTweets, String tag) {
		super(newTweets, tag);
		mainActivity = activity;
		layoutInflater = LayoutInflater.from(mainActivity);
		imageDownloader = new ImageDownloader();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.single_tweet_layout_without_avatar, null);
			holder = new ViewHolder();
			holder.retweetedBy = (TextView) convertView.findViewById(R.id.retweeted_by);
			holder.name = (TextView) convertView.findViewById(R.id.username_textview);
			holder.screenName = (TextView) convertView.findViewById(R.id.user_screen_name_textview);
			holder.date = (TextView) convertView.findViewById(R.id.tweet_date_textview);
			holder.text = (TextView) convertView.findViewById(R.id.tweet_text_textview);
			holder.attachedImage = (ImageView) convertView.findViewById(R.id.attached_image);
			holder.button = (Button) convertView.findViewById(R.id.button_show_image);
			holder.buttonReply = (ImageButton) convertView.findViewById(R.id.image_button_reply);
			holder.buttonRetweet = (ImageButton) convertView.findViewById(R.id.image_button_retweet);
			holder.buttonFavorite = (ImageButton) convertView.findViewById(R.id.image_button_favorite);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Tweet tweet = getItem(position);
		Person author = tweet.getAuthor();

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

		if (tweet.hasLinkInText()) {
			holder.text.setFocusable(false);
			holder.text.setFocusableInTouchMode(false);
		}

		if (tweet.hasMedia()) {
			Bitmap bitmap = DataCache.getInstance().getImage(tweet.getMediaUrl());
			if (bitmap != null) {
				holder.attachedImage.setImageBitmap(bitmap);
				holder.attachedImage.setVisibility(View.VISIBLE);
				holder.button.setVisibility(View.GONE);
			} else {
				holder.attachedImage.setVisibility(View.GONE);
				holder.button.setVisibility(View.VISIBLE);

				holder.button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						imageDownloader.loadBitmap(tweet.getMediaUrl(), holder.attachedImage);
						v.setVisibility(View.GONE);
					}
				});
				holder.button.setFocusable(false);
			}
		} else {
			holder.button.setVisibility(View.GONE);
			holder.attachedImage.setVisibility(View.GONE);
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

	@Override
	public void updateContext(Activity context) {
		mainActivity = context;
	}
}

