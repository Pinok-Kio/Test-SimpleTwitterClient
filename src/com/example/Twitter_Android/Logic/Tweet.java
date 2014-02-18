package com.example.Twitter_Android.Logic;

import java.util.Arrays;

public class Tweet {
	private final String text;
	private final String creationDate;
	private final String lang;
	private final String[] hashtags;
	private final Person person;
	private final String mediaUrl;
	private final long ID;
	private final boolean hasMedia;
	private final boolean hasLinkInText;
	private boolean isSelected;

	public Tweet(String text, String creationDate, String lang, String[] hashtags, Person person, String[] mediaUrl, long ID) {
		this.text = text;
		this.creationDate = creationDate;
		this.lang = lang;
		this.hashtags = hashtags;
		this.person = person;
		this.mediaUrl = (mediaUrl != null) ? mediaUrl[0] : null;
		this.ID = ID;
		hasMedia = (mediaUrl != null);
		hasLinkInText = (text.contains("http"));
	}

	public String getText() {
		return text;
	}

	public String getCreationTime() {
		return creationDate;
	}

	public String getLang() {
		return lang;
	}

	public String[] getHashtags() {
		return hashtags;
	}

	public Person getPerson() {
		return person;
	}

	public boolean hasMedia() {
		return hasMedia;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public long getID() {
		return ID;
	}

	public boolean hasLinkInText() {
		return hasLinkInText;
	}

	@Override
	public String toString() {
		String line = "----------------------------------------------------------------------";
		return line + "\n" + text + " created at: " + creationDate + "\n" + Arrays.toString(hashtags) + "\n" + person;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Tweet tweet = (Tweet) o;

		return ID == tweet.ID
				&& hasLinkInText == tweet.hasLinkInText
				&& hasMedia == tweet.hasMedia
				&& isSelected == tweet.isSelected
				&& !(creationDate != null ? !creationDate.equals(tweet.creationDate) : tweet.creationDate != null)
				&& Arrays.equals(hashtags, tweet.hashtags)
				&& !(lang != null ? !lang.equals(tweet.lang) : tweet.lang != null)
				&& !(mediaUrl != null ? !mediaUrl.equals(tweet.mediaUrl) : tweet.mediaUrl != null)
				&& !(person != null ? !person.equals(tweet.person) : tweet.person != null)
				&& !(text != null ? !text.equals(tweet.text) : tweet.text != null);
	}

	@Override
	public int hashCode() {
		int result = text.hashCode();
		result = 31 * result + creationDate.hashCode();
		result = 31 * result + (lang != null ? lang.hashCode() : 0);
		result = 31 * result + (hashtags != null ? Arrays.hashCode(hashtags) : 0);
		result = 31 * result + person.hashCode();
		result = 31 * result + (mediaUrl != null ? mediaUrl.hashCode() : 0);
		result = 31 * result + (int) (ID ^ (ID >>> 32));
		result = 31 * result + (hasMedia ? 1 : 0);
		result = 31 * result + (hasLinkInText ? 1 : 0);
		result = 31 * result + (isSelected ? 1 : 0);
		return result;
	}
}
