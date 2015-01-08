/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 * 
 *  @author: yg@wareninja.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.wareninja.opensource.discourse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wareninja.opensource.discourse.utils.MyWebClient;
import com.wareninja.opensource.discourse.utils.ResponseListener;
import com.wareninja.opensource.discourse.utils.ResponseModel;

import org.apache.http.util.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class DiscourseApiClient {

	String api_url = "";// base url. e.g. http://your_discourse_domain.com
	String api_key = "";
	String api_username = "";
	public DiscourseApiClient(String api_url, String api_key, String api_username) {
		this.api_url = api_url;
		this.api_key = api_key;
		this.api_username = api_username;
	}
	//public DiscourseApiClient() {
	//}
	public void setApiBase(String api_url, String api_key, String api_username) {
		this.api_url = api_url;
		this.api_key = api_key;
		this.api_username = api_username;
	}
	public static enum FILTER {
		LIKE,
		WAS_LIKED,
		BOOKMARK,
		NEW_TOPIC,
		REPLY,
		RESPONSE,
		MENTION,
		QUOTE,
		STAR,
		EDIT,
		NEW_PRIVATE_MESSAGE,
		GOT_PRIVATE_MESSAGE
	};
	
/////////////////////
//USERS
/////////////////////

	/**
	 * getUser
	 * 
	 * parameters empty: if you want to get the current authenticated user (api_key user)
	 * parameters.put("username",username): to get specific user
	 * 
	 * @param parameters
	 * @param responseListener
	 */
	public void getUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "getUser";
		
		// example: https://base_domain/users/<username>.json?api_key=<key>&api_username=<caller_username>
		
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//-if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		if (parameters.containsKey("username")) {
			//parameters.put("api_username", parameters.get("username"));
			methodName += "/users/" + parameters.get("username") + ".json";
		}
		else if (!TextUtils.isEmpty(this.api_username)) {
			//parameters.put("api_username", this.api_username);
			methodName += "/users/" + this.api_username + ".json";
		}
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.get(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}
		*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * createUser
	 * parameters MUST already contain
	 * 'name': name,
	 * 'email': email,
	 * 'username': username,
	 * 'password': password
    */
	public void createUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = createUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel createUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "createUser";
		
		ResponseModel responseModel = new ResponseModel();
		
		if (parameters==null) parameters = new HashMap<String, String>();
		if (!parameters.containsKey("name") || !parameters.containsKey("email") || !parameters.containsKey("username") || !parameters.containsKey("password")) {
			
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = "Missing parameters!!!";
			//responseListener.onError_wMeta(responseModel.meta);
			//return;
		}
		
		// step.1: fetchConfirmationValue
		String confirmationValue = fetchConfirmationValue();
		// {"value":"....","challenge":"..."}
		
		// step.2: createUser
		/*
		 that.post('users',
	      {
	        'name': name,
	        'email': email,
	        'username': username,
	        'password': password,
	        'password_confirmation': json.value,
	        'challenge': json.challenge.split("").reverse().join("") // reverse the string - boo! security via obscurity
	      },
		 */
		if (!confirmationValue.startsWith("ERROR|")) {
			
			MyWebClient webClient = new MyWebClient(this.api_url);
			//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
			//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
			
			// use from confirmationvalue -> {"value":"....","challenge":"..."}
			JsonObject confirmationValueJson = (new JsonParser()).parse(confirmationValue).getAsJsonObject();
			if (confirmationValueJson.has("value")) parameters.put("password_confirmation", confirmationValueJson.get("value").getAsString());
			if (confirmationValueJson.has("challenge")) {
				String challenge = confirmationValueJson.get("challenge").getAsString();
				parameters.put("challenge", (new StringBuilder(challenge)).reverse().toString()); // reverse the string - boo! security via obscurity
			}
			
			String methodName = "";
			methodName += "/users";//"/users";
			methodName = webClient.enrichMethodName(methodName, this.api_key, "");// append api_key only!
			
			//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName + " | parameters: "+parameters );
			
			String responseStr = webClient.post(methodName, parameters);
			responseModel.meta.code = webClient.getHttpResponseCode();
			responseModel.data = responseStr;
			/*if (responseModel.meta.code<=201) { // success
				responseListener.onComplete_wModel(responseModel);
			}
			else {// error occured!
				responseModel.meta.errorType = "general";
				responseModel.meta.errorDetail = responseStr;
				responseListener.onError_wMeta(responseModel.meta);
			}
			*/
			if (responseModel.meta.code>201) {// error occured!
				responseModel.meta.errorType = "general";
				responseModel.meta.errorDetail = responseStr;
				//responseListener.onError_wMeta(responseModel.meta);
			}
		}
		else {// ERROR!!!
			responseModel.meta.code = 500;
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = confirmationValue;
			//responseListener.onError_wMeta(responseModel.meta);
		}
		
		return responseModel;
	}
	
	/** fetchConfirmationValue
	 * used right before createUser
	 * endpoint: users/hp.json
	 * discourse api should bypass the honeypot since it is a trusted user (confirmed via api key)
	 */
	protected String fetchConfirmationValue() {
		return fetchConfirmationValue( new HashMap<String, String>() );
	}
	protected String fetchConfirmationValue(Map<String, String> parameters) {
		
		final String TAG = "fetchConfirmationValue";
		
		
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/users/hp.json";
		
		String responseStr = webClient.get(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		if (responseModel.meta.code<=201) { // success
			//responseListener.onComplete_wModel(responseModel);
			return responseModel.data+"";
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			//responseListener.onError_wMeta(responseModel.meta);
			return "ERROR|"+responseModel.meta.errorDetail;
		}
	}
	
	/**
	 * approveUser
	 * parameters MUST  contain
	 * 'userid': userid,
	 * 'username': username
    */
	public void approveUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = approveUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel approveUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "approveUser";
		/* admin_user_approve PUT      /admin/users/:user_id/approve(.:format)
		this.put('admin/users/' + id + '/approve',
    	{ context: 'admin/users/' + username },
		 */
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/admin/users/" + parameters.get("userid") + "/approve";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		parameters.put("context", "/admin/users/" + parameters.get("username"));
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.put(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * activateUser
	 * parameters MUST  contain
	 * 'userid': userid,
	 * 'username': username
	 * 
	 * @param parameters
	 * @param responseListener
    */
	public void activateUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = activateUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel activateUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "activateUser"; 
		/* admin_user_activate PUT      /admin/users/:user_id/activate(.:format)
		this.put('admin/users/' + id + '/activate',
    	{ context: 'admin/users/' + username },
		 */
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/admin/users/" + parameters.get("userid") + "/activate";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		//-parameters.put("context", "/admin/users/" + parameters.get("username"));
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.put(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * trustUser
	 * parameters MUST  contain
	 * 'userid': userid,
	 * 'level': 0
	 * -> level can be: 0 (new user), 1 (basic user), 2 (regular user), 3 (leader), 4 (elder)
    */
	public void trustUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = trustUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel trustUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "trustUser"; 
		/*  admin_user_trust_level PUT      /admin/users/:user_id/trust_level(.:format)
		 * 
		 */
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/admin/users/" + parameters.get("userid") + "/trust_level";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		webClient.enrichMethodName_addParam(methodName
				, "level"
				, parameters.containsKey("level")?parameters.get("level"):"0"
				);
		//parameters.put("level", parameters.containsKey("level")?parameters.get("level"):"0");
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.put(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * generateApiKey :  generate api_key for specific user!
	 * parameters MUST  contain
	 * 'userid': userid,
    */
	public void generateApiKey(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = generateApiKey(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel generateApiKey(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "generateApiKey"; 
		/*  admin_user_generate_api_key POST     /admin/users/:user_id/generate_api_key(.:format) 
		 */
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		
		String methodName = "";
		methodName += "/admin/users/" + parameters.get("userid") + "/generate_api_key";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		webClient.enrichMethodName_addParam(methodName
				, "level"
				, parameters.containsKey("level")?parameters.get("level"):"0"
				);
		//parameters.put("level", parameters.containsKey("level")?parameters.get("level"):"0");
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.post(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * deleteUser
	 * parameters MUST  contain
	 * 'userid': userid,
	 * 'username': username
    */
	public void deleteUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = deleteUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel deleteUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		/*
		this.delete(id + '.json',
    	{ context: 'admin/users/' + username },
		 */
		
		return responseModel;
	}
	
	/**
	 * loginUser
	 * parameters MUST  contain
	 * 'username': username
	 * 'password': password
    */
	public void loginUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = loginUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel loginUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "loginUser";
		// this.post('session', { 'login': username, 'password': password },
		
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		
		String methodName = "";
		methodName += "/session";
		methodName = webClient.enrichMethodName(methodName, this.api_key, "");// append api_key only
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.post(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * logoutUser
	 * parameters MUST  contain
	 * 'username': username
    */
	public void logoutUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = logoutUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel logoutUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "logoutUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		/*
		 this.delete('session/' + username, {}, function(error, body, httpCode) {
		    callback(error, body, httpCode);
		  });
		 */
		
		return responseModel;
	}
	
	
/////////////////////
//SEARCH
/////////////////////
	
	/**
	 * searchForUser
	 * parameters MUST  contain
	 * 'username': username
    */
	public void searchForUser(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = searchForUser(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel searchForUser(Map<String, String> parameters) {
		// SYNCHRONOUS function
		// TODO:
		/*
		this.get('users/search/users.json', { term: username }, function(error, body, httpCode) {
		    callback(error, body, httpCode);
		  });
		 */
		final String TAG = "searchForUser";
		
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//-if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/users/search/users.json";
		if (parameters.containsKey("username")) {
			parameters.put("term", parameters.get("username"));
		}
		else if (!TextUtils.isEmpty(this.api_username)) {
			parameters.put("term", this.api_username);
		}
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.get(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * search
	 * parameters MUST  contain
	 * 'term': term
    */
	public void search(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = search(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel search(Map<String, String> parameters) {
		// SYNCHRONOUS function
		/*
this.get('search.json', { term: term }, function(error, body, httpCode) {
    callback(error, body, httpCode);
  });
		 */
		final String TAG = "search";
		
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//-if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/search.json";
		if (parameters.containsKey("term")) {
			parameters.put("term", parameters.get("term"));
		}
		else {
			parameters.put("term", "");
		}
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.get(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	
///////////////////////
//TOPICS AND REPLIES
///////////////////////
	
	/**
	 * createTopic
	 * parameters MUST  contain
	 * 'title': title
	 * 'raw': raw
	 * 'category': category
	 * 
    */
	/*
https://github.com/discourse/discourse/blob/master/lib/post_creator.rb
 # Acceptable options:
  #
  #   raw                     - raw text of post
  #   image_sizes             - We can pass a list of the sizes of images in the post as a shortcut.
  #   invalidate_oneboxes     - Whether to force invalidation of oneboxes in this post
  #   acting_user             - The user performing the action might be different than the user
  #                             who is the post "author." For example when copying posts to a new
  #                             topic.
  #   created_at              - Post creation time (optional)
  #   auto_track              - Automatically track this topic if needed (default true)
  #
  #   When replying to a topic:
  #     topic_id              - topic we're replying to
  #     reply_to_post_number  - post number we're replying to
  #
  #   When creating a topic:
  #     title                 - New topic title
  #     archetype             - Topic archetype
  #     category              - Category to assign to topic
  #     target_usernames      - comma delimited list of usernames for membership (private message)
  #     target_group_names    - comma delimited list of groups for membership (private message)
  #     meta_data             - Topic meta data hash
  #     cooking_options       - Options for rendering the text
  #
	 */
	public void createTopic(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = createTopic(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel createTopic(Map<String, String> parameters) {
		// SYNCHRONOUS function
		
		final String TAG = "createTopic";
		/*
this.post('posts', { 'title': title, 'raw': raw, 'category': category, 'archetype': 'regular' }, function(error, body, httpCode) {
    callback(error, body, httpCode);
  });
		 */
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		parameters.put("archetype", "regular");
		
		String methodName = "";
		methodName += "/posts";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		String responseStr = webClient.post(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	/**
	 * getCreatedTopics
	 * parameters MAY  contain
	 * 'username': username
	 * if not, pulls topics of api_username
	 * 
    */
	public void getCreatedTopics(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getCreatedTopics(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getCreatedTopics(Map<String, String> parameters) {
		// SYNCHRONOUS function
		
		final String TAG = "getCreatedTopics";
		MyWebClient webClient = new MyWebClient(this.api_url);
		if (parameters==null) parameters = new HashMap<String, String>();
		//if (!TextUtils.isEmpty(this.api_key)) parameters.put("api_key", this.api_key);
		//if (!TextUtils.isEmpty(this.api_username)) parameters.put("api_username", this.api_username);
		
		String methodName = "";
		methodName += "/user_actions.json";
		methodName = webClient.enrichMethodName(methodName, this.api_key, this.api_username);// append api_key and api_username
		
		//responseListener.onBegin("BEGIN"+"|"+TAG+"| methodName:"+methodName );
		
		if (parameters.containsKey("username")) {
			parameters.put("username", parameters.get("username"));
		}
		else if (!TextUtils.isEmpty(this.api_username)) {
			parameters.put("username", this.api_username);
		}
		//parameters.put("filter", FILTER.NEW_TOPIC.name());
		String responseStr = webClient.get(methodName, parameters);
		ResponseModel responseModel = new ResponseModel();
		responseModel.meta.code = webClient.getHttpResponseCode();
		responseModel.data = responseStr;
		/*if (responseModel.meta.code<=201) { // success
			responseListener.onComplete_wModel(responseModel);
		}
		else {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
			responseListener.onError_wMeta(responseModel.meta);
		}*/
		if (responseModel.meta.code>201) {// error occured!
			responseModel.meta.errorType = "general";
			responseModel.meta.errorDetail = responseStr;
		}
		
		return responseModel;
	}
	
	
	public void replyToTopic(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = replyToTopic(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel replyToTopic(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void replyToPost(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = replyToPost(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel replyToPost(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void getTopicAndReplies(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getTopicAndReplies(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getTopicAndReplies(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void deleteTopic(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = deleteTopic(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel deleteTopic(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void updateTopic(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = updateTopic(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel updateTopic(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void updatePost(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = updatePost(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel updatePost(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	
/////////////////////
//PRIVATE MESSAGES
/////////////////////
	public void createPrivateMessage(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = createPrivateMessage(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel createPrivateMessage(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void getPrivateMessages(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getPrivateMessages(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getPrivateMessages(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void getPrivateMessageThread(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getPrivateMessageThread(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getPrivateMessageThread(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void getSentPrivateMessages(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getSentPrivateMessages(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getSentPrivateMessages(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void getReceivedPrivateMessages(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = getReceivedPrivateMessages(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel getReceivedPrivateMessages(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}
	
	public void replyToPrivateMessage(Map<String, String> parameters, ResponseListener responseListener) {
		// ASYNCHRONOUS function
		ResponseModel responseModel = replyToPrivateMessage(parameters);
		
		if (responseModel.data==null || responseModel.meta.code>201) {
			responseListener.onError_wMeta(responseModel.meta);
		}
		else {
			responseListener.onComplete_wModel(responseModel);
		}
	}
	public ResponseModel replyToPrivateMessage(Map<String, String> parameters) {
		// SYNCHRONOUS function
		final String TAG = "deleteUser";
		ResponseModel responseModel = new ResponseModel();
		// TODO: 
		
		
		return responseModel;
	}

	
}
