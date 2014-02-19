package com.example.Twitter_Android.Net;

import com.example.Twitter_Android.Logic.Message;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class Parser {
	private final JSONParser parser;
	private final DateFormat incomingDate = new SimpleDateFormat("EE MMM dd HH:mm:ss Z yyyy");
	private final DateFormat formattedDate = new SimpleDateFormat("EE dd MMM HH:mm", new Locale("ru"));


	public Parser() {
		parser = new JSONParser();
		formattedDate.setTimeZone(TimeZone.getTimeZone("GMT+7"));
	}

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить список твитов из строки.
	 * Строка представляет собой JSON ARRAY из твитов.
	 * 1.   Разбираем строку на элементы массива.
	 * 2.   Разбираем каждый элемент.
	 * SuppressWarnings("unchecked") - Т.к. jsonArray содержит только элементы типа JSONObject
	 * и, в данном случае, приведение типов безопасно.
	 *
	 * @param str строка для разбора на твиты.
	 * @return список твитов.
	 * @throws ParseException если строка некорректная или null.
	 */
	@SuppressWarnings("unchecked")
	public List<Tweet> getTweets(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONArray jsonArray = (JSONArray) parsedString;
		List<Tweet> tweets = new ArrayList<>();
		boolean isRetweeted = false;
		for (JSONObject arrayValue : (Iterable<JSONObject>) jsonArray) {
			Tweet tweet;
			JSONObject retweeted = (JSONObject) arrayValue.get("retweeted_status");    //Проверка, это твит или ретвит?
			if (retweeted != null) {
				isRetweeted = true;
				//Это ретвит, будем показывать оригинального автора.
				tweet = getSingleTweet(retweeted);
			} else {
				isRetweeted = false;
				//Это не ретвит.
				tweet = getSingleTweet(arrayValue);
			}
			if(isRetweeted){
				JSONObject user = (JSONObject) arrayValue.get("user");
				Person retweetedBy = getPerson(user);
				tweet.setRetweetedBy(retweetedBy);
			}
			tweets.add(tweet);
		}
		return tweets;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Возвращает одиночный твит.
	 *
	 * @param object JSONObject для разбора.
	 * @return твит.
	 */
	private Tweet getSingleTweet(JSONObject object) {
		JSONObject user = (JSONObject) object.get("user");
		Person person = getPerson(user);
		String text = (String) object.get("text");
		String createdAt = "Unknown";
		try {
			createdAt = getCreationDate(object);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		String lang = (String) object.get("lang");
		String[] tags = getTags(object);
		String[] media = getMedia(object);
		long ID = (long) object.get("id");
		return new Tweet(text, createdAt, lang, tags, person, media, ID);
	}
	//------------------------------------------------------------------------------------------------------------------

	public Tweet getSingleTweet(String str) throws ParseException {
		return getSingleTweet((JSONObject) parser.parse(str));
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returning next_cursor value in followings timeline.
	 *
	 * @param str string to parse
	 * @return next_cursor value
	 * @throws ParseException incorrect or null string
	 */
	public long getNextCursor(String str) throws ParseException {
		JSONObject parsedString = (JSONObject) parser.parse(str);
		return (long) parsedString.get("next_cursor");
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить список твитов, соответствующих поисковому запросу.
	 *
	 * @param str JSON стока для разбора.
	 * @return список твитов.
	 * @throws ParseException если строка некорректная или null.
	 */
	public List<Tweet> getSearchedTweets(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONObject array = (JSONObject) parsedString;
		JSONArray jsonArray = (JSONArray) array.get("statuses");
		return getTweets(jsonArray.toJSONString());
	}

	public List<Person> getSearchedUsers(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONArray array = (JSONArray) parsedString;
		List<Person> result = new ArrayList<>();
		for (JSONObject arrayValue : (Iterable<JSONObject>) array) {
			Person person = getPerson(arrayValue);
			result.add(person);
		}
		return result;
	}
	//------------------------------------------------------------------------------------------------------------------

	public Person getPerson(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONObject data = (JSONObject) parsedString;
		return getPerson(data);
	}

	private Person getPerson(JSONObject person) {
		String name = (String) person.get("name");
		String profileImage = (String) person.get("profile_image_url_https");
		String location = (String) person.get("location");
		String description = (String) person.get("description");
		String screenName = (String) person.get("screen_name");
		boolean friend = (boolean) person.get("following");
		long ID = (long) person.get("id");
		return new Person(name, screenName, profileImage, location, description, friend, ID);
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Возаращает дату создания твита. Из строки с датой вырезается ненужная часть, поэтому оформлено отдельной функцией.
	 *
	 * @param object JSONObject для разбора.
	 * @return строковое представление даты.
	 */
	private String getCreationDate(JSONObject object) throws java.text.ParseException {
		String date = (String) object.get("created_at");
		return formattedDate.format(incomingDate.parse(date));
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Возвращает массив тэгов.
	 *
	 * @param object JSONObject для разбора.
	 * @return массив тэгов.
	 */
	@SuppressWarnings("unchecked")
	private String[] getTags(JSONObject object) {
		JSONObject entities = (JSONObject) object.get("entities");
		JSONArray jsonHashTags = (JSONArray) entities.get("hashtags");
		String[] tags = new String[jsonHashTags.size()];
		int i = 0;
		for (JSONObject s : (Iterable<JSONObject>) jsonHashTags) {
			tags[i++] = (String) s.get("text");
		}
		return tags;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Возвращает массив ссылок (как строки) на приложенные медиафайлы.
	 *
	 * @param object JSONObject для разбора.
	 * @return массив ссылок (как строки)
	 */
	@SuppressWarnings("unchecked")
	private String[] getMedia(JSONObject object) {
		JSONObject entities = (JSONObject) object.get("entities");
		JSONArray jsonMedia = (JSONArray) entities.get("media");
		if (jsonMedia != null) {
			String[] media = new String[jsonMedia.size()];
			int i = 0;
			for (JSONObject s : (Iterable<JSONObject>) jsonMedia) {
				media[i++] = (String) s.get("media_url_https");
			}

			return media;
		}
		return null;
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить из строки отображаемое имя подключенного пользователя.
	 *
	 * @param str строка ответа сервера
	 * @return отображаемое имя пользователя.
	 * @throws ParseException
	 */
	public String getUserScreenName(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONObject object = (JSONObject) parsedString;
		return (String) object.get("screen_name");
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Получить список друзей полтьзователя.
	 *
	 * @param str строка ответа сервера
	 * @return список друзей пользователя.
	 * @throws ParseException
	 */
	public List<Person> getFriends(String str) throws ParseException {
		List<Person> friends = new ArrayList<>();
		Object parsedString = parser.parse(str);
		JSONObject object = (JSONObject) parsedString;
		JSONArray jsonArray = (JSONArray) object.get("users");
		for (JSONObject arrayValue : (Iterable<JSONObject>) jsonArray) {
			Person person = getPerson(arrayValue);
			friends.add(person);
		}
		return friends;
	}

	//------------------------------------------------------------------------------------------------------------------
	public boolean isFriends(String str) throws ParseException {
		Object parsedString = parser.parse(str);
		JSONObject jsonString = (JSONObject) parsedString;
		JSONObject relationship = (JSONObject) jsonString.get("relationship");
		JSONObject targetFriend = (JSONObject) relationship.get("target");
		return (boolean) targetFriend.get("followed_by");
	}

	public List<Message> getMessages(String str) throws ParseException, java.text.ParseException {
		JSONArray jsonArray = (JSONArray) parser.parse(str);
		List<Message> result = new ArrayList<>();
		for (JSONObject arrayValue : (Iterable<JSONObject>) jsonArray) {
			long messageId = (long) arrayValue.get("id");
			String text = (String) arrayValue.get("text");
			String time = getCreationDate(arrayValue);
			Person sender = getPerson((JSONObject) arrayValue.get("sender"));
			Message message = new Message(messageId, text, time, sender);
			result.add(message);
		}
		return result;
	}
}
