package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.Twitter_Android.AsynkTasks.TaskDeleteTweet;
import com.example.Twitter_Android.Fragments.Adapters.TimelineAdapter;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.concurrent.Executors;

public class DeleteTweetDialog extends DialogFragment {
	private final Tweet tweetToDelete;
	private final TimelineAdapter<Tweet> adapter;
	public static final String TAG = "DELETE_TWEET_DIALOG_TAG";

	private DeleteTweetDialog(TimelineAdapter<Tweet> adapter, Tweet tweetToDelete) {
		this.adapter = adapter;
		this.tweetToDelete = tweetToDelete;
	}

	public static DeleteTweetDialog getInstance(TimelineAdapter<Tweet> adapter, Tweet tweet) {
		return new DeleteTweetDialog(adapter, tweet);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.dialog_delete_tweet, container, false);

		TextView tweetText = (TextView) v.findViewById(R.id.dialog_tweet_to_delete);
		Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		Button btnDelete = (Button) v.findViewById(R.id.button_delete);

		tweetText.setText(tweetToDelete.getText());

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		btnDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.removeItem(tweetToDelete);
				TaskDeleteTweet task = new TaskDeleteTweet(getActivity());
				task.executeOnExecutor(Executors.newCachedThreadPool(), tweetToDelete.getID());
				dismiss();
			}
		});

		return v;
	}
}
