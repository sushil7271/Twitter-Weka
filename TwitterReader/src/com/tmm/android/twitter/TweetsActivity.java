package com.tmm.android.twitter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;


import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.samples.graphapi.GraphApiSampleActivity;
import com.tmm.android.facebook.HelloFacebookSampleActivity;
import com.tmm.android.twitter.appliaction.TwitterApplication;
import com.tmm.android.twitter.reader.TweetReader;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;

public class TweetsActivity extends ListActivity implements OnClickListener {

	private TwitterListAdapter adapter;
	ArrayList<JSONObject> jobs ;
	String ClassifiedClass="";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button buttonDashBoard =(Button)findViewById(R.id.DashBoard);
		buttonDashBoard.setOnClickListener(this);
		Button buttonClassify =(Button)findViewById(R.id.Classify);
		buttonClassify.setOnClickListener(this);
		Button buttonfacebook =(Button)findViewById(R.id.facebook);
		buttonfacebook.setOnClickListener(this);
		Twitter t = ((TwitterApplication)getApplication()).getTwitter();
		jobs = TweetReader.retrieveSpecificUsersTweets(t);
		adapter = new TwitterListAdapter(this,jobs);
		//generateNoteOnSD("TweetsTxtFile.txt",jobs);
		setListAdapter(adapter);
		Toast.makeText(TweetsActivity.this, ClassifiedClass, Toast.LENGTH_LONG);
	}

	public void generateNoteOnSD(String Path,String sFileName, ArrayList<JSONObject> sBody)
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
				Log.e("tweet", sBody.get(i).getString("tweet"));
				writer.append(i+1+". "+sBody.get(i).getString("tweet"));
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
		case R.id.DashBoard:
			startActivity(new Intent(TweetsActivity.this,DashboardActivity.class));

			break;
		case R.id.Classify:
			new Thread(){
				public void run(){
					try {
						MyFilteredLearner learner;
						learner = new MyFilteredLearner();
						//learner.loadDataset("/sdcard/New Folder/smsspam.small.arff");
						learner.loadDataset("/sdcard/New Folder/happy_sad.arff");
						// Evaluation mus be done before training
						// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
						learner.evaluate();
						learner.learn();
						learner.saveModel("/sdcard/New Folder/happy_sad.model");

						MyFilteredClassifier classifier = new MyFilteredClassifier();
						generateNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",jobs);
						classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
						classifier.loadModel("/sdcard/New Folder/happy_sad.model");
						classifier.makeInstance();
						String ClassifiedClass =classifier.classify();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			
			break;
		case R.id.facebook:
			startActivity(new Intent(TweetsActivity.this,GraphApiSampleActivity.class));

			break;
		default:
			break;
		}
	} 
	
	
	
	
}