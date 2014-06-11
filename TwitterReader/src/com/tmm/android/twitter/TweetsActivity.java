package com.tmm.android.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sromku.simple.fb.example.MainActivity;
import com.tmm.Twitter.Tweet;
import com.tmm.android.facebook.HelloFacebookSampleActivity;
import com.tmm.android.twitter.appliaction.TwitterApplication;
import com.tmm.android.twitter.reader.TweetReader;
import com.tmm.android.twitter.util.GMailSender;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;

public class TweetsActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnTaskCompleted,OnItemClickListener {

	private TwitterListAdapter adapter;
	ArrayList<JSONObject> jobs ;
	String ClassifiedClass="";
	Twitter t;
	ArrayList<String> FollwersNameList = new ArrayList<String>();
	ListView follwerListView,twitterlist;
	String ScreenName="";
	Spinner select_DataSet;
	public static String Selected_DatasetPath="";
	ArrayList<String> datasetPathlist =new ArrayList<String>();
	public static String modelurl="";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		select_DataSet=(Spinner)findViewById(R.id.select_DataSet);
		select_DataSet.setOnItemSelectedListener(this);

		datasetPathlist.add("ISEAR_Happy_Sad_150");
		datasetPathlist.add("ISEAR_Happy_Sad_200");
		datasetPathlist.add("ISEAR_Happy_Sad_250");
		datasetPathlist.add("ISEAR_Happy_Sad_300");
		datasetPathlist.add("ISEAR_Happy_Sad_400");
		datasetPathlist.add("ISEAR_Happy_Sad_700");


		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datasetPathlist);
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
		t = ((TwitterApplication)getApplication()).getTwitter();

		jobs = TweetReader.retrieveSpecificUsersTweets(t);
		adapter = new TwitterListAdapter(this,jobs);
		//generateNoteOnSD("TweetsTxtFile.txt",jobs);
		twitterlist.setAdapter(adapter);
		try {
			PagableResponseList<User> usersResponse  = t.getFollowersList(t.getScreenName(), -1);
			System.out.println(usersResponse);
			for (int index = 0; index < usersResponse.size(); index++) {
				FollwersNameList.add(usersResponse.get(index).getScreenName());
			}
			FollowerListAdapter adapter = new FollowerListAdapter(TweetsActivity.this,FollwersNameList);
			follwerListView.setAdapter(adapter);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Toast.makeText(TweetsActivity.this, ClassifiedClass, Toast.LENGTH_LONG);
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

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.Tweets:
			twitterlist.setVisibility(View.VISIBLE);
			follwerListView.setVisibility(View.GONE);

			break;
		case R.id.follwer_tweets:
			twitterlist.setVisibility(View.GONE);
			follwerListView.setVisibility(View.VISIBLE);
			break;
		case R.id.Classify:
			
			ClassifyMethod(jobs);
			break;


			/*case R.id.facebook:
			startActivity(new Intent(TweetsActivity.this,MainActivity.class));

			break;*/
		default:
			break;

		}
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

	public void SendEmail(String username,String to,String from){
		String Subject="Emotional Support to your friend.";
		Spanned marked_up = Html.fromHtml("Hello Dear,<br><p>This is auto generated mail from "+ username+" regarding his/her current emotional state."
				+"Your friend’s Emotional Level is beyond 80% ,so he /she need support to prevent any wrong step.</p>"
				+"<b>HOW CAN YOU HELP YOUR FRIEND?</b><br>"
				+"<br>When do people need emotional support?"
				+"<br><p>People become upset for any number of different reasons. Distress can be a reaction to a common but disturbing life experience – an accident, a child hurt in a playground, someone injured in traffic – or after receiving bad news. Or it could be as a result of a very exceptional event, a plane crash, train derailment, major weather event or act of violence. Or it could be a build-up of many events, causing overload and stress."
				+"Whatever the cause of the emotional upset, the principles of helping are broadly the same. And they hold good whether you are helping a stranger in a first-aid situation, or a friend, colleague or relative.</p>"
				+"<b>What is the first step?</b>"
				+"<p>Carry out a quick but thoughtful assessment of the situation. What is happening? Are there any hazards? Notice who else is around. Are they likely to be helpful, or otherwise?"
				+"Then, crucially, check yourself. Think about what shape you are in. How have you been affected by the situation? The aim is to be calm. If you are calm, you can help others. If you aren\'t, you probably can\'t, at the moment."
				+"If you are calm enough to help someone else, that\'s good. If you are not, you might look for help for yourself.</p>"
				+"<b>How do you help someone who is upset?</b>"
				+"<p>Good listening is a very good start. It is harder, and rarer, than a lot of people think. Give people time to talk. Give them space, too – don\'t crowd them. Make eye contact appropriately, but don\'t stare. Be physically still and relaxed, not agitated or using sudden body movements. When you talk, use a calm voice. Don’t shout and don’t whisper. Don\'t interrupt."
				+"It is best to avoid false reassurance, such as, \"everything will be okay\". After all, it might not be. And even if it is, that is not how the person is feeling at that moment."
				+"Offer non-verbal encouragement—\"mmm\" and so on. That can indicate that you are listening, and are happy to hear what the person has to say. A good way to show you have understood is to to reflect out loud on what the person has said: “so, you’re very worried about that,” for instance."
				+"All the time, watch how the person is responding. Listen and learn from what they tell you about how they are feeling. Adapt your style to suit them."
				+"Accept their response – don’t argue or disagree with them. If you think something else is advisable, such as a medical check-up, calmly explain why.</p>"

				+"<b>What are things to avoid?</b>"
				+"Here are some basic mistakes to steer clear of:<br>"
				+"<br>- Don\'t try to jolly people up and get them to look at the funny side. They might do that later, but your task is to respect how they\'re feeling now and help them deal with it, not suppress it."
				+"<br>- Don\'t say things like, \"I know just how you are feeling, just the same happened to me\". This isn\'t empathy, it is more like boasting. It is very alienating and irritating."
				+"<br>- Don\'t hurry the next action. Always remember that a person who is upset is vulnerable and probably not in a state for successful decision-making.");

		try {   
			GMailSender sender = new GMailSender("Pallavi.phalke15@gmail.com", "Ganesha * 15");
			sender.sendMail(Subject, marked_up.toString(),  to, from);   
		} catch (Exception e) {   
			Log.e("SendMail", e.getMessage(), e);   
		} 
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
						}else{
							generateNoteforFollowerOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",tweets);
						}
						
						classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						classifier.loadModel(modelurl);
						classifier.makeInstance();
						ClassifiedClass=classifier.classify();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								showAlert(TweetsActivity.this,"You are "+ClassifiedClass);
								try {
									SendEmail(t.getScreenName(),"sushil7271@gmail.com","sushil7271@gmail.com");
								} catch (IllegalStateException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (TwitterException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
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
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		Selected_DatasetPath="/sdcard/New Folder/"+datasetPathlist.get(position)+".arff";
		System.out.println("Selected_DatasetPath :-"+Selected_DatasetPath);

		modelurl=Selected_DatasetPath.substring(0, Selected_DatasetPath.lastIndexOf('.'))+".model";
		System.out.println("modelurl :-"+modelurl);
		/*String finalmodelurl=modelurl.substring(0,modelurl.lastIndexOf('/'));
		System.out.println("finalmodelurl :- "+finalmodelurl);*/
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	// convert InputStream to String
	/*private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}*/

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
}
