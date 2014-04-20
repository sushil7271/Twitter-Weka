package com.tmm.android.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.tmm.android.facebook.HelloFacebookSampleActivity;
import com.tmm.android.twitter.appliaction.TwitterApplication;
import com.tmm.android.twitter.reader.TweetReader;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;

public class TweetsActivity extends ListActivity implements OnClickListener, OnItemSelectedListener {

	private TwitterListAdapter adapter;
	ArrayList<JSONObject> jobs ;
	String ClassifiedClass="";
	Twitter t;
	Spinner select_DataSet;
	String Selected_DatasetPath="";
	ArrayList<String> datasetPathlist =new ArrayList<String>();
	String modelurl="";
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
/*			datasetPathlist.add(getStringFromInputStream(this.getAssets().open("dataset/ISEAR_Happy_Sad_200.arff")));
			datasetPathlist.add(getStringFromInputStream(this.getAssets().open("dataset/ISEAR_Happy_Sad_250.arff")));
			datasetPathlist.add(getStringFromInputStream(this.getAssets().open("dataset/ISEAR_Happy_Sad_300.arff")));
			datasetPathlist.add(getStringFromInputStream(this.getAssets().open("dataset/ISEAR_Happy_Sad_400.arff")));
			datasetPathlist.add(getStringFromInputStream(this.getAssets().open("dataset/ISEAR_Happy_Sad_700.arff")));*/
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datasetPathlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		select_DataSet.setAdapter(dataAdapter);
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
		//Toast.makeText(TweetsActivity.this, ClassifiedClass, Toast.LENGTH_LONG);
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
							//learner.loadDataset("/sdcard/New Folder/smsspam.small.arff");
							learner.loadDataset(Selected_DatasetPath);
							// Evaluation mus be done before training
							// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
							learner.evaluate();
							learner.learn();

							//String str= TweetsActivity.this.getAssets().open("Model/ISEAR_Happy_Sad");
							learner.saveModel(modelurl);

							MyFilteredClassifier classifier = new MyFilteredClassifier();
							generateNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","spamtextfile.txt",jobs);
							classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/spamtextfile.txt");
							classifier.loadModel(modelurl);
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
			}else{
				showAlert("Please Select the Dataset.");
			}
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
		private static String getStringFromInputStream(InputStream is) {
	 
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
	 
		}

}
