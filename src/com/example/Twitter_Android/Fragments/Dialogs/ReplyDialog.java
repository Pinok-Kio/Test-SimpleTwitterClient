package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.Dialog;
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
import com.example.Twitter_Android.AsynkTasks.TaskReply;
import com.example.Twitter_Android.Logic.Tweet;
import com.example.Twitter_Android.R;

import java.util.concurrent.Executors;

public class ReplyDialog extends DialogFragment {
	private final Tweet tweetToReply;
	private static final int TWEET_MAX_LENGTH = 140;
	public static final String TAG = "REPLY_DIALOG_TAG";

	private ReplyDialog(Tweet t) {
		tweetToReply = t;
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo);
	}

	public static ReplyDialog getInstance(Tweet t) {
		return new ReplyDialog(t);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.dialog_reply_to, container, false);
		final TextView textToReply = (TextView) v.findViewById(R.id.dialog_tweet_to_reply_text);
		final TextView tweetLength = (TextView) v.findViewById(R.id.dialog_tweet_length);
		final EditText replyText = (EditText) v.findViewById(R.id.dialog_tweet_text);

		textToReply.setText(tweetToReply.getText());

		replyText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				tweetLength.setText("Tweet length: " + s.length() + " / " + (TWEET_MAX_LENGTH - s.length()) + " symbols left");
			}
		});

		final Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		final Button btnReply = (Button) v.findViewById(R.id.button_reply);
		btnReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = "@" + tweetToReply.getAuthor().getScreenName() + " " + replyText.getText().toString();
				long tweetID = tweetToReply.getID();
				Bundle params = new Bundle();
				params.putLong(TaskReply.TWEET_ID, tweetID);
				params.putString(TaskReply.TEXT, text);
				TaskReply task = new TaskReply(getActivity());
				task.executeOnExecutor(Executors.newCachedThreadPool(), params);
				dismiss();
			}
		});

		Dialog d = getDialog();
		if (d != null) {
			d.setTitle(getString(R.string.title_dialog_reply_to) + " " + tweetToReply.getAuthor().getName());
		}

		return v;
	}
}
