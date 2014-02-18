package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.Twitter_Android.AsynkTasks.TaskFollow;
import com.example.Twitter_Android.AsynkTasks.TaskUnfollow;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.Fragments.Adapters.FollowingsListAdapter;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Net.Connector;
import com.example.Twitter_Android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInfoDialog extends DialogFragment {
	private final Person person;
	public static final String TAG = "USER_INFO_DIALOG";

	//------------------------------------------------------------------------------------------------------------------
	private UserInfoDialog(Person p) {
		person = p;
	}
	//------------------------------------------------------------------------------------------------------------------

	public static UserInfoDialog newInstance(Person p) {
		return new UserInfoDialog(p);
	}
	//------------------------------------------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		final View v = inflater.inflate(R.layout.layout_friend_info_dialog, container, false);

		final ImageView avatar = (ImageView) v.findViewById(R.id.info_friend_avatar_imageview);
		final TextView name = (TextView) v.findViewById(R.id.info_friend_name_textview);
		final TextView screenName = (TextView) v.findViewById(R.id.info_friend_screen_name_textview);
		final TextView info = (TextView) v.findViewById(R.id.info_friend_info_textview);
		final Button btnFollow = (Button) v.findViewById(R.id.info_button_follow);

		if (person.isFriend()) {
			btnFollow.setText(R.string.text_unfollow);
		} else {
			btnFollow.setText(R.string.text_follow);
		}

		btnFollow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnFollow.getText().equals(getString(R.string.text_unfollow))) {
					unfollow(person.getID());
					btnFollow.setText(R.string.text_follow);
				} else {
					follow(person.getID());
					btnFollow.setText(R.string.text_unfollow);
				}
				person.changeFriendshipRelations();
			}
		});

		Button btnCancel = (Button) v.findViewById(R.id.button_close_dialog);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		avatar.setImageBitmap(DataCache.getInstance().getImage(person.getProfileImage()));
		name.setText(person.getName());
		screenName.setText("@" + person.getScreenName());
		info.setText(person.getDescription());

		Dialog d = getDialog();
		if (d != null) {
			d.setTitle(R.string.text_information);
		}
		return v;
	}
	//------------------------------------------------------------------------------------------------------------------

	private void unfollow(final long id) {
		/*
			Чтобы данный персонаж (от которого мы отписались) больше не отображался в списке друзей - его нужно удалить.
			Смотрим, загружали ли мы уже список друзей. Если ДА - значит у нас уже есть сохраненный адаптер
			и нужно удалить оттуда данного персонажа. Иначе - ничего не делаем.
		 */
		BaseAdapter adapter = DataCache.getInstance().getAdapter(FollowingsListAdapter.TAG);
		if (adapter != null) {
			((FollowingsListAdapter) adapter).removeItem(person);
			System.out.println("UNFOLLOW ((FollowingsListAdapter) adapter).removeItem(person)");
		}
		TaskUnfollow task = new TaskUnfollow();
		task.execute(id);
	}
	//------------------------------------------------------------------------------------------------------------------

	private void follow(final long id) {
		/*
			Аналогично предыдущему комментарию.
		 */
		BaseAdapter adapter = DataCache.getInstance().getAdapter(FollowingsListAdapter.TAG);
		if (adapter != null) {
			List<Person> tmp = new ArrayList<>();
			tmp.add(person);
			System.out.println(" FOLLOW (FollowingsListAdapter) adapter).addItemsToTop(tmp)");
			((FollowingsListAdapter) adapter).addItemsToTop(tmp);
		}
		TaskFollow task = new TaskFollow();
		task.execute(id);
	}
}
