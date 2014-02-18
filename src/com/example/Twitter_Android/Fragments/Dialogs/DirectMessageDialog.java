package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.AsynkTasks.TaskSendMessage;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.R;

public class DirectMessageDialog extends DialogFragment {
	private final Person person;
	private static final int TWEET_MAX_LENGTH = 140;
	public static final String TAG = "DIRECT_MESSAGE_DIALOG";

	private DirectMessageDialog(Person p) {
		person = p;
	}

	public static DirectMessageDialog getInstance(Person p) {
		return new DirectMessageDialog(p);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.dialog_send_message, container, false);
		final ImageView avatar = (ImageView) v.findViewById(R.id.avatar);
		final TextView name = (TextView) v.findViewById(R.id.user_name);
		final TextView screenName = (TextView) v.findViewById(R.id.user_screen_name);
		final TextView tweetSize = (TextView) v.findViewById(R.id.dialog_tweet_length);
		final EditText message = (EditText) v.findViewById(R.id.dialog_message_text);
		final Button btnCancel = (Button) v.findViewById(R.id.button_cancel);
		final Button btnSend = (Button) v.findViewById(R.id.button_send_message);

		Bitmap avatarBitmap = DataCache.getInstance().getImage(person.getProfileImage());
		if (avatarBitmap != null) {
			avatar.setImageBitmap(avatarBitmap);
		} else {
			ImageDownloader downloader = new ImageDownloader();
			downloader.loadBitmap(person.getProfileImage(), avatar);
		}

		name.setText(person.getName());
		screenName.setText("@" + person.getScreenName());

		message.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				tweetSize.setText("Tweet length: " + s.length() + " / " + (TWEET_MAX_LENGTH - s.length()) + " symbols left");
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = message.getText().toString();
				if (text.length() > 0) {
					Bundle args = new Bundle();
					args.putLong(TaskSendMessage.ID, person.getID());
					args.putString(TaskSendMessage.TEXT, text);
					TaskSendMessage task = new TaskSendMessage();
					task.execute(args);
					dismiss();
				}
			}
		});

		Dialog d = getDialog();
		if (d != null) {
			d.setTitle(getString(R.string.title_dialog_send_message) + " " + person.getName());
		}

		return v;
	}
}
