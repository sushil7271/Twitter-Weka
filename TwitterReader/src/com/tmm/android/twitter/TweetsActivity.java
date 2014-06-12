package com.tmm.android.twitter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.actions.Cursor;
import com.sromku.simple.fb.entities.Post;
import com.sromku.simple.fb.example.utils.Utils;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPostsListener;
import com.tmm.Twitter.Tweet;
import com.tmm.android.twitter.reader.TweetReader;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;
import com.tmm.android.twitter.appliaction.TwitterApplication;

public class TweetsActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnTaskCompleted,OnItemClickListener, OnCheckedChangeListener {
	protected static final String TAG = TweetsActivity.class.getName();
	private TwitterListAdapter adapter;
	ArrayList<JSONObject> jobs ;
	String ClassifiedClass="";
	Twitter t;
	ArrayList<String> FollwersNameList = new ArrayList<String>();
	ListView follwerListView,twitterlist,facebookPostlistView;
	String ScreenName="";
	Spinner select_DataSet;
	public static String Selected_DatasetPath="";
	ArrayList<String> ISEARdatasetPathlist =new ArrayList<String>();
	ArrayList<String> wordnetdatasetPathlist =new ArrayList<String>();
	ArrayList<String> FacebookPostList =new ArrayList<String>();
	public static String modelurl="";
	RadioButton ISERbtn,wordnetBtn;

	Button facebook;
	private SimpleFacebook mSimpleFacebook;
	private String mAllPages = "";
	private TextView Pagenumber,LoadMore;
	private ProgressDialog mProgressDialog;
	LinearLayout facebookPostlayout;
	String mailID="";
	protected void showDialog() {
		if (mProgressDialog == null) {
			setProgressDialog();
		}
		mProgressDialog.show();
	}

	protected void hideDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	private void setProgressDialog() {
		mProgressDialog = new ProgressDialog(TweetsActivity.this);
		mProgressDialog.setTitle("Thinking...");
		mProgressDialog.setMessage("Doing the action...");
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSimpleFacebook = SimpleFacebook.getInstance(this);

		// test local language
		Utils.updateLanguage(getApplicationContext(), "en");
		Utils.printHashKey(getApplicationContext());

		select_DataSet=(Spinner)findViewById(R.id.select_DataSet);
		select_DataSet.setOnItemSelectedListener(this);

		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_150");
		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_200");
		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_250");
		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_300");
		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_400");
		ISEARdatasetPathlist.add("ISEAR_Happy_Sad_700");

		wordnetdatasetPathlist.add("happy_sad_wordnetaffect");
		
		t = ((TwitterApplication)getApplication()).getTwitter();
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.facebookpostitem, ISEARdatasetPathlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		select_DataSet.setAdapter(dataAdapter);
		Button buttonTweets =(Button)findViewById(R.id.Tweets);
		buttonTweets.setOnClickListener(this);
		Button buttonClassify =(Button)findViewById(R.id.Classify);
		buttonClassify.setOnClickListener(this);
		twitterlist=(ListView)findViewById(R.id.twitterlist);
		twitterlist.setOnItemClickListener(this);
		follwerListView= (ListView)findViewById(R.id.follwerListView);
		follwerListView.setOnItemClickListener(this);
		Button Get_Follwer_List=(Button)findViewById(R.id.follwer_tweets);
		Get_Follwer_List.setOnClickListener(this);
		facebookPostlayout=(LinearLayout)findViewById(R.id.facebookPostlayout);
		facebook=(Button)findViewById(R.id.facebook);
		facebook.setOnClickListener(this);
		facebookPostlistView=(ListView)findViewById(R.id.facebookPostlist);
		Pagenumber=(TextView)findViewById(R.id.PageNumber);
		LoadMore=(TextView)findViewById(R.id.load_more);
		ISERbtn=(RadioButton)findViewById(R.id.ISEARradioButton);
		wordnetBtn=(RadioButton)findViewById(R.id.WordNetradioButton2);
		ISERbtn.setChecked(true);
		ISERbtn.setOnCheckedChangeListener(this);
		wordnetBtn.setOnCheckedChangeListener(this);
		facebookPostlayout.setVisibility(View.INVISIBLE);
		checkTheVisibility();

		new DownloadtheTweets().execute(t);
	}


	public void GetTheMailID(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Mail ID");
		alert.setMessage("Please Provide the Email-ID");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mailID = input.getText().toString();
				dialog.dismiss();
				// Do something with value!
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
				dialog.dismiss();
			}
		});

		alert.show();
	}
	class DownloadtheTweets extends AsyncTask<Twitter, Void, Void>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showDialog();
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter = new TwitterListAdapter(TweetsActivity.this,jobs);
			//generateNoteOnSD("TweetsTxtFile.txt",jobs);
			twitterlist.setAdapter(adapter);
			FollowerListAdapter adapter = new FollowerListAdapter(TweetsActivity.this,FollwersNameList);
			follwerListView.setAdapter(adapter);
			hideDialog();
			GetTheMailID();
		}
		@Override
		protected Void doInBackground(Twitter... params) {
			jobs = TweetReader.retrieveSpecificUsersTweets(params[0]);
			try {
				PagableResponseList<User> usersResponse  = params[0].getFollowersList(params[0].getScreenName(), -1);
				System.out.println(usersResponse);
				for (int index = 0; index < usersResponse.size(); index++) {
					FollwersNameList.add(usersResponse.get(index).getScreenName());
				}

			} catch (TwitterException e) {

				e.printStackTrace();
			}
			return null;
		}
	}

	public void generateNoteOnSD(String Path,String sFileName, ArrayList<?> sBody)
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
				Log.e("tweet", ((JSONObject) sBody.get(i)).getString("tweet"));
				writer.append(i+1+". "+((JSONObject) sBody.get(i)).getString("tweet"));
			}
			writer.flush();
			writer.close();
			//Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			// importError = e.getMessage();
			//iError();
		}
	}
	public void generateFacebookPostNoteOnSD(String Path,String sFileName, ArrayList<?> sBody)
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
				Log.e("facebook", ((String) sBody.get(i)));
				writer.append(i+1+". "+((String) sBody.get(i)));
			}
			writer.flush();
			writer.close();
			//Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			// importError = e.getMessage();
			//iError();
		}
	}

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.Tweets:
			twitterlist.setVisibility(View.VISIBLE);
			follwerListView.setVisibility(View.GONE);
			facebookPostlayout.setVisibility(View.GONE);
			break;
		case R.id.follwer_tweets:
			twitterlist.setVisibility(View.GONE);
			facebookPostlayout.setVisibility(View.GONE);
			follwerListView.setVisibility(View.VISIBLE);
			break;
		case R.id.Classify:
			if(checkTheVisibility()){
				ClassifyMethod(FacebookPostList);	
			}else{
				ClassifyMethod(jobs);
			}
			break;


		case R.id.facebook:
			facebookPostlayout.setVisibility(View.VISIBLE);
			twitterlist.setVisibility(View.GONE);
			follwerListView.setVisibility(View.GONE);
			if (mSimpleFacebook.isLogin()) {
				getFaceBookPost();
			}else{
				mSimpleFacebook.login(onLoginListener);
			}
			break;
		default:
			break;

		}
	} 
	@Override
	protected void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}

	public boolean checkTheVisibility(){
		if(facebookPostlayout.getVisibility() == View.VISIBLE){
			Log.d(TAG, "Facebook is visible");
			System.out.println("Facebook is visible");
			return true;
		}else{
			Log.d(TAG, "Facebook is Not visible");
			System.out.println("Facebook is Not visible");
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
	}
	public static void showAlert(Activity  act,String Message){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(act);
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
	}


	private void ClassifyMethod(final ArrayList<?> tweets){
		if(!Selected_DatasetPath.equals("")){
			final ProgressDialog pd = new ProgressDialog(TweetsActivity.this);
			pd.setMessage("classifying Tweets please wait...");
			pd.setCanceledOnTouchOutside(false);
			pd.show();
			new Thread(){
				public void run(){
					try {
						MyFilteredLearner learner;
						learner = new MyFilteredLearner();

						learner.loadDataset(Selected_DatasetPath);
						// Evaluation mus be done before training
						// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
						learner.evaluate();
						learner.learn();

						//String str= TweetsActivity.this.getAssets().open("Model/ISEAR_Happy_Sad");
						learner.saveModel(modelurl);

						MyFilteredClassifier classifier = new MyFilteredClassifier();
						if(findInstanceOf(tweets,JSONObject.class)){
							generateNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",tweets);
							classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						}else if(findInstanceOf(tweets,String.class)){
							generateFacebookPostNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","FacebookPost.txt",FacebookPostList);
							classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/FacebookPost.txt");

						}else{
							generateNoteforFollowerOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",tweets);
							classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						}

						//classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						classifier.loadModel(modelurl);
						classifier.makeInstance();
						ClassifiedClass=classifier.classify(t.getScreenName(),mailID);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								showAlert(TweetsActivity.this,"You are "+ClassifiedClass);

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
		}else{
			showAlert(TweetsActivity.this,"Please Select the Dataset.");
		}
	}
	public <T> Boolean findInstanceOf(Collection<?> arrayList, Class<T> clazz)
	{
		for(Object o : arrayList)
		{
			if (o != null && o.getClass() == clazz)
			{
				return true;
			}else
			{
				return false;
			}
		}
		return null;    
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

		com.tmm.Twitter.GetTweets Obj = new com.tmm.Twitter.GetTweets(TweetsActivity.this);
		Obj.downloadTweets(TweetsActivity.this);

		/*Intent intent = new Intent(TweetsActivity.this,com.tmm.Twitter.MainActivity.class);
		intent.putExtra("ScreenName", FollwersNameList.get(index));
		startActivity(intent);*/
	}
	@SuppressLint("SdCardPath")
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if(ISERbtn.isChecked()){
			Selected_DatasetPath="/sdcard/New Folder/"+ISEARdatasetPathlist.get(position)+".arff";
			System.out.println("Selected_DatasetPath :-"+Selected_DatasetPath);

			modelurl=Selected_DatasetPath.substring(0, Selected_DatasetPath.lastIndexOf('.'))+".model";
			System.out.println("modelurl :-"+modelurl);
		}else{
			Selected_DatasetPath="/sdcard/New Folder/"+wordnetdatasetPathlist.get(position)+".arff";
			System.out.println("Selected_DatasetPath :-"+Selected_DatasetPath);

			modelurl=Selected_DatasetPath.substring(0, Selected_DatasetPath.lastIndexOf('.'))+".model";
			System.out.println("modelurl :-"+modelurl);
		}
		/*String finalmodelurl=modelurl.substring(0,modelurl.lastIndexOf('/'));
		System.out.println("finalmodelurl :- "+finalmodelurl);*/
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}



	public void generateNoteforFollowerOnSD(String Path,String sFileName, ArrayList<?> sBody)
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
				Log.e("tweet", ((Tweet)sBody.get(i)).getText());
				writer.append(i+1+". "+((Tweet)sBody.get(i)).getText());
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

	@Override
	public void onTaskCompleted(ArrayList<Tweet> Tweets_List) {
		// TODO Auto-generated method stub
		ClassifyMethod(Tweets_List);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		switch (buttonView.getId()) {
		case R.id.ISEARradioButton:
			if(isChecked){
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.facebookpostitem, ISEARdatasetPathlist);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				select_DataSet.setAdapter(dataAdapter);
			}
			break;
		case R.id.WordNetradioButton2:
			if(isChecked){
				ArrayAdapter<String> WordNetdataAdapter = new ArrayAdapter<String>(this, R.layout.facebookpostitem, wordnetdatasetPathlist);
				WordNetdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				select_DataSet.setAdapter(WordNetdataAdapter);
			}
			break;

		default:
			break;
		}
	}

	////Facebook Code
	public void getFaceBookPost(){
		SimpleFacebook.getInstance().getPosts(new OnPostsListener() {

			@Override
			public void onThinking() {
				showDialog();
			}

			@Override
			public void onException(Throwable throwable) {
				hideDialog();
				//mResult.setText(throwable.getMessage());
			}

			@Override
			public void onFail(String reason) {
				hideDialog();
				//mResult.setText(reason);
			}

			@Override
			public void onComplete(List<Post> response) {
				hideDialog();
				// make the result more readable.
				for (int i = 0; i < response.size(); i++) {
					FacebookPostList.add(response.get(i).getStory());
				}

				ArrayAdapter<String> facebookPostdataAdapter = new ArrayAdapter<String>(TweetsActivity.this, R.layout.facebookpostitem, FacebookPostList);
				//facebookPostdataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				facebookPostlistView.setAdapter(facebookPostdataAdapter);
				Pagenumber.setText("Page Number "+getPageNum());
				/*mAllPages += "<u>\u25B7\u25B7\u25B7 (paging) #" + getPageNum() + " \u25C1\u25C1\u25C1</u><br>";
				mAllPages += com.sromku.simple.fb.utils.Utils.join(response.iterator(), "<br>", new com.sromku.simple.fb.utils.Utils.Process<Post>() {
					@Override
					public String process(Post post) {
						return "\u25CF " + post.getStory() == null || post.getStory().equalsIgnoreCase("null") ? post.getId() : post.getStory() + " \u25CF";
					}
				});
				mAllPages += "<br>";*/
				//mResult.setText(Html.fromHtml(mAllPages));

				// check if more pages exist
				if (hasNext()) {
					enableLoadMore(getCursor());
				} else {
					disableLoadMore();
				}
			}
		});
	}

	private void enableLoadMore(final Cursor<List<Post>> cursor) {
		LoadMore.setVisibility(View.VISIBLE);
		LoadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAllPages += "<br>";
				cursor.next();
			}
		});
	}

	private void disableLoadMore() {
		LoadMore.setOnClickListener(null);
		LoadMore.setVisibility(View.INVISIBLE);
	}


	final OnLoginListener onLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
			//mTextStatus.setText(reason);
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable) {
			//mTextStatus.setText("Exception: " + throwable.getMessage());
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
			//mTextStatus.setText("Thinking...");
		}

		@Override
		public void onLogin() {
			// change the state of the button or do whatever you want
			//mTextStatus.setText("Logged in");
			//loggedInUIState();
			getFaceBookPost();
		}

		@Override
		public void onNotAcceptingPermissions(Permission.Type type) {
			//				toast(String.format("You didn't accept %s permissions", type.name()));
		}
	};


}
