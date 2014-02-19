package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.AsynkTasks.TaskRetweet;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.concurrent.Executors;

public class RetweetDialog extends DialogFragment {
	private final Tweet tweet;
	public static final String TAG = "RETWEET_DIALOG";

	private RetweetDialog(Tweet t) {
		tweet = t;
	}

	public static RetweetDialog getInstance(Tweet t) {
		return new RetweetDialog(t);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View v = inflater.inflate(R.layout.dialog_retweet, container, false);
		final ImageView avatar = (ImageView) v.findViewById(R.id.user_avatar_imageview);
		final TextView name = (TextView) v.findViewById(R.id.username_textview);
		final TextView screenName = (TextView) v.findViewById(R.id.user_screen_name_textview);
		final TextView text = (TextView) v.findViewById(R.id.tweet_text_textview);

		final Person person = tweet.getAuthor();
		final Bitmap bitmapAvatar = DataCache.getInstance().getImage(person.getProfileImage());
		if (bitmapAvatar == null) {
			avatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_noavatar));
		} else {
			avatar.setImageBitmap(bitmapAvatar);
		}
		name.setText(person.getName());
		screenName.setText("@" + person.getScreenName());
		text.setText(tweet.getText());

		final Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		final Dialog d = getDialog();
		final Button btnRetweet = (Button) v.findViewById(R.id.button_retweet);
		btnRetweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final long tweetID = tweet.getID();
				if (d != null) {
					d.hide();
				}
				TaskRetweet task = new TaskRetweet(getActivity());
				task.executeOnExecutor(Executors.newCachedThreadPool(), tweetID);
				dismiss();
			}
		});

		if (d != null) {
			d.setTitle(R.string.text_retweet);
		}

		return v;
	}
}
