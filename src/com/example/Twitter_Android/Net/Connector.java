package com.example.Twitter_Android.Net;

import android.net.Uri;
import com.example.Twitter_Android.Logic.Message;
import com.example.Twitter_Android.Logic.Person;
import com.example.Twitter_Android.Logic.Tweet;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.List;

public class Connector {
	private final Parser parser;
	private final OAuthService service;
	private static Token requestToken;
	private static Token accessToken;
	private static final int ERROR_TOO_MANY_REQUESTS = 429; //HTTP code
	private static final Connector instance = new Connector();
	private long cursor;

	private Connector() {
		parser = new Parser();
		final String oauth_consumer_key = "0Y11uYXFdtcOtjO7TapGQw";
		final String oauth_consumer_secret = "fp9Lf8TpN87yd0yq1qiTD32mAdP5Z0Ohf8r6wVMuyeA";
		service = new ServiceBuilder().provider(TwitterApi.SSL.class)
				.apiKey(oauth_consumer_key)
				.apiSecret(oauth_consumer_secret)
				.build();
	}

	public static Connector getInstance() {
		return instance;
	}

	//------------------------------AUTHORIZATION-----------------------------------------------------------------------
	public void setAccessToken(Token token) {
		accessToken = token;
	}

	public String getAuthUrl() {
		requestToken = service.getRequestToken();
		return service.getAuthorizationUrl(requestToken);
	}

	/**
	 * App authorization.
	 *
	 * @param code auth code from twitter auth page.
	 */
	public Token authorize(String code) {
		final Verifier v = new Verifier(code);
		accessToken = service.getAccessToken(requestToken, v);
		return accessToken;
	}

	public boolean isAuthorized() {
		return accessToken != null;
	}

	//------------------------------CONNECTED USER----------------------------------------------------------------------

	/**
	 * Getting connected user screen name.
	 * Using for getting information about connected user.
	 *
	 * @return screen_name пользователя.
	 * @throws ParseException
	 */
	private String getConnectedUserScreenName() throws ParseException {
		final String address = "https://api.twitter.com/1.1/account/settings.json";
		Response response = connectGet(address);
		return parser.getUserScreenName(response.getBody());
	}

	public Person getAuthPerson() throws ParseException {
		final String userScreenName = getConnectedUserScreenName();
		final String address = "https://api.twitter.com/1.1/users/show.json?screen_name=" + userScreenName;
		Response response = connectGet(address);
		return parser.getPerson(response.getBody());
	}

	public Person getPersonByID(long user_id) throws ParseException {
		String address = "https://api.twitter.com/1.1/users/show.json?user_id=" + user_id;
		Response response = connectGet(address);
		return parser.getPerson(response.getBody());
	}

	/**
	 * @param firstUserID  first user ID
	 * @param secondUserID second user ID
	 * @return true - friends, false - not friends
	 * @throws ParseException incorrect ID
	 */
	public boolean isThisUserFriends(long firstUserID, long secondUserID) throws ParseException {
		String address = "https://api.twitter.com/1.1/friendships/show.json?source_id=" + firstUserID + "&target_id=" + secondUserID;
		Response response = connectGet(address);
		return parser.isFriends(response.getBody());

	}

	//------------------------------FOLLOWINGS--------------------------------------------------------------------------

	/**
	 * Get friends (followings) of user specified by id.
	 *
	 * @param id user id
	 * @param crsr cursor (see twitter api)
	 * @return list of followings
	 * @throws ParseException incorrect data
	 */
	public List<Person> getFriends(long id, long crsr) throws ParseException {
		String address = "https://api.twitter.com/1.1/friends/list.json?cursor=" + crsr
				+ "&user_id=" + id + "&skip_status=true&include_user_entities=false&count=50";
		Response response = connectGet(address);
		cursor = parser.getNextCursor(response.getBody());
		return parser.getFriends(response.getBody());
	}

	public long getCurrentCursor() {
		return cursor;
	}

	//-------------------------------TIMELINES--------------------------------------------------------------------------

	/**
	 * Get statuses (tweets) from connected user homepage.
	 *
	 * @param max_id   id последнего полученного твита. Все вновь получаемые твиты будут старше
	 *                 (т.е. созданы ранее) твита с указанным id
	 * @param since_id id первого полученного твита. Новые твиты будут моложе (т.е. созданы позднее),
	 *                 чем указанный твит.
	 * @param count    требуемое количество твитов. 0 - будет загружено количество по умолчанию (20 штук).
	 * @return список твитов с домашней страницы пользователя.
	 * @throws ParseException
	 */
	public List<Tweet> getStatuses_HomeTimeline(long max_id, long since_id, int count) throws ParseException {
		String address = "https://api.twitter.com/1.1/statuses/home_timeline.json";
		if (since_id != 0) {
			address += "?since_id=" + since_id;
		} else if (max_id == 0) {
			address += "?count=" + count;
		} else {
			address += "?count=" + count + "&max_id=" + max_id;
		}
		Response response = connectGet(address);
		if (response.getCode() == ERROR_TOO_MANY_REQUESTS) {
			System.out.println("getStatuses_HomeTimeline ERROR " + response.getBody());
			/*
				TODO something
			 */
			return new ArrayList<>();
		}
		return parser.getTweets(response.getBody());

	}

	/**
	 * Получить timeline авторизированного пользователя.
	 *
	 * @param userID   id пользователя, для которого нужно получить timeline.
	 * @param max_id   id последнего полученного твита. Все вновь получаемые твиты будут старше
	 *                 (т.е. созданы ранее) твита с указанным id
	 * @param since_id id первого полученного твита. Новые твиты будут моложе (т.е. созданы позднее),
	 *                 чем указанный твит.
	 * @param count    требуемое количество твитов. 0 - будет загружено количество по умолчанию (20 штук).
	 * @return список твитов пользователя.
	 * @throws ParseException
	 */
	public List<Tweet> getStatuses_UserTimeline(long userID, long max_id, long since_id, int count) throws ParseException {
		String address = "https://api.twitter.com/1.1/statuses/user_timeline.json?user_id=";
		if (max_id == 0 && since_id == 0) {
			address += userID + "&count=" + count;
		} else if (since_id == 0) {
			address += userID + "&count=" + count + "&max_id=" + max_id;
		} else {
			address += userID + "&count=" + count + "&since_id=" + since_id;
		}

		Response response = connectGet(address);
		if (response.getCode() == ERROR_TOO_MANY_REQUESTS) {
			System.out.println("getStatuses_HomeTimeline ERROR " + response.getBody());
			/*
				TODO something
			 */
			return new ArrayList<>();
		}
		return parser.getTweets(response.getBody());
	}

	//-------------------------------SEARCHES---------------------------------------------------------------------------

	public List<Tweet> findTweets(String toFind, int count) throws ParseException {
		String address = "https://api.twitter.com/1.1/search/tweets.json?q=";
		String correctRequest = address + getCorrectRequest(toFind) + "&count=" + count;
		Response response = connectGet(correctRequest);
		return parser.getSearchedTweets(response.getBody());
	}

	public List<Person> findUsers(String name, int count) throws ParseException {
		String address = "https://api.twitter.com/1.1/users/search.json?q=";
		String correctRequest = address + getCorrectRequest(name) + "&count=" + count;
		Response response = connectGet(correctRequest);
		return parser.getSearchedUsers(response.getBody());
	}
	//-------------------------------TWEETS-----------------------------------------------------------------------------

	/**
	 * Запостить твит.
	 *
	 * @param newTweet строка, которую нужно запостить.
	 * @return added tweet
	 */
	public Tweet postTweet(String newTweet) throws ParseException {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/statuses/update.json");
		request.addBodyParameter("status", newTweet);
		service.signRequest(accessToken, request);
		Response response = request.send();
		return parser.getSingleTweet(response.getBody());
	}

	/**
	 * Remove tweet
	 *
	 * @param id tweet id to remove
	 * @return operation status
	 */
	public boolean deleteTweet(long id) {
		String address = "https://api.twitter.com/1.1/statuses/destroy/" + id + ".json";
		OAuthRequest request = new OAuthRequest(Verb.POST, address);
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}

	/**
	 * Reply to selected tweet
	 *
	 * @param tweetID tweet id reply to
	 * @param text    message text
	 * @return operation status
	 */
	public boolean replyTo(long tweetID, String text) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/statuses/update.json");
		request.addBodyParameter("status", text);
		request.addBodyParameter("in_reply_to_status_id", Long.toString(tweetID));
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}

	/**
	 * Ретвит твита.
	 *
	 * @param id id выбранного твита.
	 * @return is result successful?
	 */
	public boolean retweet(long id) {
		String address = "https://api.twitter.com/1.1/statuses/retweet/" + id + ".json";
		OAuthRequest request = new OAuthRequest(Verb.POST, address);
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Отправить сообщение выбранному пользователю.
	 *
	 * @param userId id пользователя.
	 * @param text   текст сообщения.
	 * @return is result successful?
	 */
	public boolean sendDirectMessage(long userId, String text) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/direct_messages/new.json");
		String encodedText = Uri.encode(text);
		request.addBodyParameter("text", encodedText);
		request.addBodyParameter("user_id", Long.toString(userId));
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}

	public List<Message> getReceivedMessages(long max_id, long since_id, int count) throws ParseException, java.text.ParseException {
		String address = "https://api.twitter.com/1.1/direct_messages.json?include_entities=false&skip_status=true&count=" + count;
		if (since_id > 0) {
			address += "&since_id=" + since_id;
		} else if (max_id > 0) {
			address += "&max_id=" + max_id;
		}
		Response response = connectGet(address);
		return parser.getMessages(response.getBody());
	}

	//-------------------------------FOLLOW/UNFOLLOW--------------------------------------------------------------------

	/**
	 * Отписаться от пользователя (т.е. Unfollow).
	 *
	 * @param user_id ID пользователя, от которого нужно отписаться.
	 * @return is result successful?
	 */
	public boolean unfollow(long user_id) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/friendships/destroy.json");
		request.addBodyParameter("user_id", Long.toString(user_id));
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}

	/**
	 * Подписаться на пользователя (т.е. стать Follower'ом).
	 *
	 * @param user_id ID пользователя, на которого нужно подписаться.
	 * @return is result successful?
	 */
	public boolean follow(long user_id) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/friendships/create.json");
		request.addBodyParameter("user_id", Long.toString(user_id));
		request.addBodyParameter("follow", "false");
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.isSuccessful();
	}
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Get запрос.
	 *
	 * @param url запрос.
	 * @return ответ сервера.
	 */
	private Response connectGet(final String url) {
		OAuthRequest request = new OAuthRequest(Verb.GET, url);
		service.signRequest(accessToken, request);
		return request.send();
	}

	private String getCorrectRequest(String string) {
		return Uri.encode(string);
	}
}
