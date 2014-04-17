package com.tmm.android.twitter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
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
	Twitter t;
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
		buttonfacebook.setVisibility(View.GONE);
		buttonfacebook.setOnClickListener(this);
		t = ((TwitterApplication)getApplication()).getTwitter();

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
			try {
				Intent intent=new Intent(TweetsActivity.this,DashboardActivity.class);
				intent.putExtra("ScreenName", t.getScreenName());
				startActivity(intent);
			} catch (IllegalStateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TwitterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			

			break;
		case R.id.Classify:
			final ProgressDialog pd = new ProgressDialog(TweetsActivity.this);
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
		case R.id.facebook:
			startActivity(new Intent(TweetsActivity.this,HelloFacebookSampleActivity.class));

			break;
		default:
			break;

		}
	} 
	private void showAlert(String Message){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(TweetsActivity.this);
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



}