package com.example.Twitter_Android.Logic;

import android.app.Activity;
import android.content.Context;
import org.scribe.model.Token;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileWorker {
	private static final String tokenFilename = "SavedAccessToken.bin";
	private final Activity activity;
	private Token accessToken = null;

	public FileWorker(Activity activity) {
		this.activity = activity;
	}

	public boolean loadAccessToken() {
		File file = new File(activity.getFilesDir(), tokenFilename);
		if (file.length() > 0) {
			try {
				FileChannel read = new FileInputStream(file).getChannel();
				/*
					� ����� �������� ������ �����. �.�. ��� ������ ��������� ����� ����, ��
					����� ��������� �� int.
				 */
				ByteBuffer buffer = ByteBuffer.allocateDirect((int) file.length());
				read.read(buffer);
				accessToken = (Token) new ObjectInputStream(new ByteArrayInputStream(buffer.array())).readObject();
				buffer.clear();
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return accessToken != null;
	}

	/**
	 * ��������� accessToken � ����.
	 *
	 * @param token accessToken ��� ����������.
	 * @throws IOException
	 */
	public void saveAccessToken(Token token) throws IOException {
		final FileOutputStream fos = activity.openFileOutput(tokenFilename, Context.MODE_PRIVATE);
		final ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(token);
		oos.flush();
		oos.close();
	}

	public Token getAccessToken() {
		return accessToken;
	}
}
