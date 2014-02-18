package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.Twitter_Android.AsynkTasks.TaskPostTweet;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class PostTweetDialog extends DialogFragment {
	private static TimelineAdapter<Tweet> adapter;

	public static final String TAG = "WRITE_TWEET_DIALOG_TAG";

	private PostTweetDialog() {
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo);
	}

	public static PostTweetDialog newInstance(TimelineAdapter<Tweet> a) {
		adapter = a;
		return new PostTweetDialog();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.dialog_post_tweet, container, false);

		final TextView tweetLength = (TextView) v.findViewById(R.id.dialog_tweet_length);
		final EditText tweetText = (EditText) v.findViewById(R.id.dialog_tweet_text);
		tweetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = s.length();
				tweetLength.setText("Tweet length: " + length + " / " + (140 - length) + " symbols left");
			}
		});

		final Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		final Button btnPostTweet = (Button) v.findViewById(R.id.button_post_tweet);
		btnPostTweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String text = tweetText.getText().toString();
				if (text.length() > 0) {
					addTweetInList(text);
					TaskPostTweet task = new TaskPostTweet(getActivity(), adapter);
					task.executeOnExecutor(Executors.newCachedThreadPool(), text);
					dismiss();
				}
			}
		});
		return v;
	}

	private void addTweetInList(String text) {
		final DataCache cache = DataCache.getInstance();
		final long myID = cache.getConnectedUserID();
		final Person person = cache.getPerson(myID);
		final Tweet newTweet = new Tweet(text, new Date().toString(), null, null, person, null, 100000);
		final List<Tweet> tmp = new ArrayList<>();
		tmp.add(newTweet);
		adapter.addItemsToTop(tmp);
	}
}
