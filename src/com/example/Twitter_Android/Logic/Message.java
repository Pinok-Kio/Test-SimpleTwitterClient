package com.example.Twitter_Android.Logic;

public class Message {
	private final long MESSAGE_ID;
	private final String TEXT;
	private final String CREATION_TIME;
	private final Person sender;

	public Message(long mid, String text, String time, Person p) {
		MESSAGE_ID = mid;
		TEXT = text;
		CREATION_TIME = time;
		sender = p;
	}

	public long geId() {
		return MESSAGE_ID;
	}

	public String getText() {
		return TEXT;
	}

	public String getTime() {
		return CREATION_TIME;
	}

	public Person getSender() {
		return sender;
	}

	@Override
	public String toString() {
		return "Message{" +
				"MESSAGE_ID=" + MESSAGE_ID +
				", TEXT='" + TEXT + '\'' +
				", CREATION_TIME='" + CREATION_TIME + '\'' +
				", sender name = " + sender.getName() + '}';
	}
}
