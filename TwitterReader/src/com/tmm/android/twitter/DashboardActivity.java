
package com.tmm.android.twitter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.tmm.Twitter.MainActivity;
import com.tmm.android.twitter.appliaction.TwitterApplication;

public class DashboardActivity extends Activity implements OnClickListener, OnItemClickListener{

	ArrayList<String> FollwersNameList = new ArrayList<String>();
	ListView follwerListView;
	String ScreenName="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		if(getIntent().hasExtra("ScreenName")){
			ScreenName= getIntent().getStringExtra("ScreenName");
		}
		follwerListView= (ListView)findViewById(R.id.follwerListView);
		follwerListView.setOnItemClickListener(this);
		Button Get_Follwer_List=(Button)findViewById(R.id.follwer_tweets);
		Get_Follwer_List.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.follwer_tweets:
			Twitter t= ((TwitterApplication)getApplication()).getTwitter();
			try {
				PagableResponseList<User> usersResponse;
				usersResponse = t.getFollowersList(ScreenName, -1);
				System.out.println(usersResponse);
				for (int index = 0; index < usersResponse.size(); index++) {
					FollwersNameList.add(usersResponse.get(index).getScreenName());
				}
				FollowerListAdapter adapter = new FollowerListAdapter(this,FollwersNameList);
				follwerListView.setAdapter(adapter);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

		Intent intent = new Intent(DashboardActivity.this,MainActivity.class);
		intent.putExtra("ScreenName", FollwersNameList.get(index));
		startActivity(intent);
	}

}
