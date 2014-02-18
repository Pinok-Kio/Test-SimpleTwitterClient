package com.example.Twitter_Android.Fragments.Dialogs;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.example.Twitter_Android.AsynkTasks.ImageDownloader;
import com.example.Twitter_Android.Logic.DataCache;
import com.example.Twitter_Android.R;

public class ShowImageDialog extends DialogFragment implements View.OnClickListener {
	private static final String TAG_IMAGE = "IMAGE";
	public static final String TAG = "DIALOG_SHOW_IMAGE";

	private ShowImageDialog() {
	}

	public static ShowImageDialog newInstance(String image) {
		ShowImageDialog dialog = new ShowImageDialog();
		Bundle args = new Bundle();
		args.putString(TAG_IMAGE, image);
		dialog.setArguments(args);
		dialog.setRetainInstance(true);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		setStyle(DialogFragment.STYLE_NORMAL, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.layout_dialog_show_image, container, false);
		ImageView iv = (ImageView) view.findViewById(R.id.tweet_media_imageview);

		String imagePath = getArguments().getString(TAG_IMAGE);

		Bitmap bitmap = DataCache.getInstance().getImage(imagePath);
		if (bitmap != null) {
			iv.setImageBitmap(bitmap);
		} else {
			ImageDownloader imageDownloader = new ImageDownloader();
			imageDownloader.loadBitmap(imagePath, iv);
		}

		Button btnClose = (Button) view.findViewById(R.id.button_close_dialog);
		btnClose.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

}
