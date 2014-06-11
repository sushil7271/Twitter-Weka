package com.tmm.Twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.gson.Gson;
import com.tmm.android.twitter.OnTaskCompleted;
import com.tmm.android.twitter.R;
import com.tmm.android.twitter.TweetsActivity;
import com.tmm.android.twitter.reader.TweetReader;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;

/**
 * Demonstrates how to use a twitter application keys to access a user's timeline
 */
public class GetTweets   {


	String ScreenName = "sushil7271";//"therockncoder";
	final static String LOG_TAG = "rnc";
	Button Classify_follwerTweets;
	ArrayList<Tweet> jobs = new ArrayList<Tweet>(); ;

	private OnTaskCompleted listener;
	 public GetTweets(OnTaskCompleted listener){
	        this.listener=listener;
	    }
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.follwertweets);
		activity = this;

		ScreenName= getIntent().getStringExtra("ScreenName");
		Classify_follwerTweets=(Button)findViewById(R.id.Classify_follwerTweets);
		Classify_follwerTweets.setOnClickListener(this);
		//downloadTweets();
	}*/

	// download twitter timeline after first checking to see if there is a network connection
	public void downloadTweets(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadTwitterTask().execute(ScreenName);
		} else {
			Log.v(LOG_TAG, "No network connection available.");
		}
	}

	// Uses an AsyncTask to download a Twitter user's timeline
	private class DownloadTwitterTask extends AsyncTask<String, Void, String> {
		final static String CONSUMER_KEY = "oUQPlO81Wp4j7AzVgvwcFg";
		final static String CONSUMER_SECRET = "LqtWwEpHaGXDPC0fDOOdruCG47MN2rnvRFWKmmXPI";
		final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
		final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";

		@Override
		protected String doInBackground(String... screenNames) {
			String result = null;

			if (screenNames.length > 0) {
				result = getTwitterStream(screenNames[0]);
			}
			return result;
		}

		// onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
		@Override
		protected void onPostExecute(String result) {
			Log.d("result", result);
			Twitter twits = jsonToTwitter(result);
			jobs=twits;
			listener.onTaskCompleted(jobs);
			// lets write the results to the console as well
			for (Tweet tweet : twits) {
				Log.i(LOG_TAG, tweet.getText());
			}
			//return jobs;
			//		jobs = TweetReader.retrieveSpecificUsersTweets((twitter4j.Twitter) twits);
			// send the tweets to the adapter for rendering
			//ArrayAdapter<Tweet> adapter = new ArrayAdapter<Tweet>(activity, android.R.layout.simple_list_item_1, twits);
			//setListAdapter(adapter);
		}

		// converts a string of JSON data into a Twitter object
		private Twitter jsonToTwitter(String result) {
			Twitter twits = null;
			if (result != null && result.length() > 0) {
				try {
					Gson gson = new Gson();
					twits = gson.fromJson(result, Twitter.class);
				} catch (IllegalStateException ex) {
					// just eat the exception
				}
			}
			return twits;
		}

		// convert a JSON authentication object into an Authenticated object
		private Authenticated jsonToAuthenticated(String rawAuthorization) {
			Authenticated auth = null;
			if (rawAuthorization != null && rawAuthorization.length() > 0) {
				try {
					Gson gson = new Gson();
					auth = gson.fromJson(rawAuthorization, Authenticated.class);
				} catch (IllegalStateException ex) {
					// just eat the exception
				}
			}
			return auth;
		}

		private String getResponseBody(HttpRequestBase request) {
			StringBuilder sb = new StringBuilder();
			try {

				DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
				HttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				String reason = response.getStatusLine().getReasonPhrase();

				if (statusCode == 200) {

					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();

					BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
					String line = null;
					while ((line = bReader.readLine()) != null) {
						sb.append(line);
					}
				} else {
					sb.append(reason);
				}
			} catch (UnsupportedEncodingException ex) {
			} catch (ClientProtocolException ex1) {
			} catch (IOException ex2) {
			}
			return sb.toString();
		}

		private String getTwitterStream(String screenName) {
			String results = null;

			// Step 1: Encode consumer key and secret
			try {
				// URL encode the consumer key and secret
				String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
				String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

				// Concatenate the encoded consumer key, a colon character, and the
				// encoded consumer secret
				String combined = urlApiKey + ":" + urlApiSecret;

				// Base64 encode the string
				String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

				// Step 2: Obtain a bearer token
				HttpPost httpPost = new HttpPost(TwitterTokenURL);
				httpPost.setHeader("Authorization", "Basic " + base64Encoded);
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
				String rawAuthorization = getResponseBody(httpPost);
				Authenticated auth = jsonToAuthenticated(rawAuthorization);

				// Applications should verify that the value associated with the
				// token_type key of the returned object is bearer
				if (auth != null && auth.token_type.equals("bearer")) {

					// Step 3: Authenticate API requests with bearer token
					HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);

					// construct a normal HTTPS request and include an Authorization
					// header with the value of Bearer <>
					httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
					httpGet.setHeader("Content-Type", "application/json");
					// update the results with the body of the response
					results = getResponseBody(httpGet);
				}
			} catch (UnsupportedEncodingException ex) {
			} catch (IllegalStateException ex1) {
			}
			return results;
		}
	}

	/*@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.Classify_follwerTweets:
			final ProgressDialog pd = new ProgressDialog(MainActivity.this);
			pd.setMessage("classifying Tweets please wait...");
			pd.setCanceledOnTouchOutside(false);
			pd.show();
			new Thread(){
				public void run(){
					try {
						MyFilteredLearner learner;
						learner = new MyFilteredLearner();
						//learner.loadDataset("/sdcard/New Folder/smsspam.small.arff");
						learner.loadDataset("/sdcard/New Folder/ISEAR_Happy_Sad.arff");
						// Evaluation mus be done before training
						// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
						learner.evaluate();
						learner.learn();
						learner.saveModel("/sdcard/New Folder/ISEAR_Happy_Sad.model");

						MyFilteredClassifier classifier = new MyFilteredClassifier();
						generateNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",jobs);
						classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						classifier.loadModel("/sdcard/New Folder/ISEAR_Happy_Sad.model");
						classifier.makeInstance();
						ClassifiedClass=classifier.classify();
						runOnUiThread(new Runnable() {



							@Override
							public void run() {
								// TODO Auto-generated method stub
								showAlert("You are "+ClassifiedClass);
							}
						});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						pd.dismiss();
					}
				}
			}.start();

			break;

		default:
			break;
		}
	}
	 */


	public void generateNoteOnSD(String Path,String sFileName, ArrayList<Tweet> sBody)
	{
		try
		{
			File root = new File(Path);
			if (!root.exists()) {
				root.mkdirs();
			}
			File gpxfile = new File(root, sFileName);
			FileWriter writer = new FileWriter(gpxfile);
			for (int i = 0; i < sBody.size(); i++) {
				Log.e("tweet", sBody.get(i).getText());
				writer.append(i+1+". "+sBody.get(i).getText());
			}
			writer.flush();
			writer.close();
			//Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		}catch(IOException e)
		{
			e.printStackTrace();
			// importError = e.getMessage();
			//iError();
		}
	}
	/*private void showAlert(String Message){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
		builder1.setMessage(Message);
		builder1.setCancelable(true);
		builder1.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert11 = builder1.create();
		alert11.show();
	}*/
}
