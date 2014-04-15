package com.tmm.android.twitter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FollowerListAdapter extends BaseAdapter
{
	ArrayList<String> _follwerList =  new ArrayList<String>();
	Activity activity;
	public FollowerListAdapter(DashboardActivity dashboardActivity,
			ArrayList<String> follwersNameList) {
		this._follwerList=follwersNameList;
		this.activity= dashboardActivity;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return _follwerList.size();
	}

	@Override
	public Object getItem(int pos) {
		// TODO Auto-generated method stub
		return _follwerList.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View vi, ViewGroup groupView) {
		TextView Name = new TextView(activity);
		Name.setText(_follwerList.get(position));
		Name.setTextColor(activity.getResources().getColor(android.R.color.black));
		Name.setPadding(20, 20, 20, 20);
		return Name;
	}



}
