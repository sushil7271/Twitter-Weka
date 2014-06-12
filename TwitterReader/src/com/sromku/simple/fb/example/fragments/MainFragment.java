package com.sromku.simple.fb.example.fragments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.actions.Cursor;
import com.sromku.simple.fb.entities.Post;

import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnPostsListener;
import com.sromku.simple.fb.utils.Utils;
import com.tmm.android.twitter.R;
import com.tmm.android.twitter.TweetsActivity;
import com.tmm.android.weka.MyFilteredClassifier;
import com.tmm.android.weka.MyFilteredLearner;

public class MainFragment extends BaseFragment {

	protected static final String TAG = MainFragment.class.getName();
	private final static String EXAMPLE = "Get posts";
	private Button mButtonLogin;
	private Button mButtonLogout;
	private TextView mTextStatus;
	//private ListView mListView;
	String ClassifiedClass="";
	private TextView mResult;
	private Button mGetButton;
	private TextView mMore;
	private String mAllPages = "";
	//private ArrayList<Example> mExamples;

	private SimpleFacebook mSimpleFacebook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSimpleFacebook = SimpleFacebook.getInstance();

		//mExamples = new ArrayList<Example>();

		//mExamples.add(new Example("Get posts", GetPostsFragment.class));

	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle("Simple Facebook Sample");
	}


	private void enableLoadMore(final Cursor<List<Post>> cursor) {
		mMore.setVisibility(View.VISIBLE);
		mMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAllPages += "<br>";
				cursor.next();
			}
		});
	}

	private void disableLoadMore() {
		mMore.setOnClickListener(null);
		mMore.setVisibility(View.INVISIBLE);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		mButtonLogin = (Button) view.findViewById(R.id.button_login);
		mButtonLogout = (Button) view.findViewById(R.id.button_logout);
		mTextStatus = (TextView) view.findViewById(R.id.text_status);
		//mListView = (ListView) view.findViewById(R.id.list);

		setLogin();
		setLogout();
		mResult = (TextView) view.findViewById(R.id.result);
		mMore = (TextView) view.findViewById(R.id.load_more);
		mMore.setPaintFlags(mMore.getPaint().getFlags() | Paint.UNDERLINE_TEXT_FLAG);
		mGetButton = (Button) view.findViewById(R.id.button);
		//mGetButton.setText(EXAMPLE);
		mAllPages = "";
		mResult.setText(mAllPages);
		SimpleFacebook.getInstance().getPosts(new OnPostsListener() {

			@Override
			public void onThinking() {
				showDialog();
			}

			@Override
			public void onException(Throwable throwable) {
				hideDialog();
				mResult.setText(throwable.getMessage());
			}

			@Override
			public void onFail(String reason) {
				hideDialog();
				mResult.setText(reason);
			}

			@Override
			public void onComplete(List<Post> response) {
				hideDialog();
				// make the result more readable.
				mAllPages += "<u>\u25B7\u25B7\u25B7 (paging) #" + getPageNum() + " \u25C1\u25C1\u25C1</u><br>";
				mAllPages += Utils.join(response.iterator(), "<br>", new Utils.Process<Post>() {
					@Override
					public String process(Post post) {
						return "\u25CF " + post.getStory() == null || post.getStory().equalsIgnoreCase("null") ? post.getId() : post.getStory() + " \u25CF";
					}
				});
				mAllPages += "<br>";
				mResult.setText(Html.fromHtml(mAllPages));
				generateNoteOnSD(Environment.getExternalStorageDirectory()+"/New Folder/","FacebookPost.txt",Html.fromHtml(mAllPages));
				// check if more pages exist
				if (hasNext()) {
					enableLoadMore(getCursor());
				} else {
					disableLoadMore();
				}
			}
		});

		mGetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*final ProgressDialog pd = new ProgressDialog(MainFragment.this);
				pd.setMessage("classifying Tweets please wait...");
				pd.setCanceledOnTouchOutside(false);
				pd.show();*/
				showDialog();
				new Thread(){
					public void run(){
						try {
							MyFilteredLearner learner;
							learner = new MyFilteredLearner();
							//learner.loadDataset("/sdcard/New Folder/smsspam.small.arff");
							learner.loadDataset(TweetsActivity.Selected_DatasetPath);
							// Evaluation mus be done before training
							// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
							learner.evaluate();
							learner.learn();

							//String str= TweetsActivity.this.getAssets().open("Model/ISEAR_Happy_Sad");
							learner.saveModel(TweetsActivity.modelurl);

							MyFilteredClassifier classifier = new MyFilteredClassifier();
							classifier.load(Environment.getExternalStorageDirectory()+"/New Folder/FacebookPost.txt");
							classifier.loadModel(TweetsActivity.modelurl);
							classifier.makeInstance();
							//ClassifiedClass=classifier.classify();
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									TweetsActivity.showAlert(getActivity(),"You are "+ClassifiedClass);
								}
							});
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							//pd.dismiss();
							hideDialog();
						}
					}
				}.start();
			
		}
	});
		/*mListView.setAdapter(new ExamplesAdapter(mExamples));*/


		setUIState();
		return view;
}
public void generateNoteOnSD(String Path,String sFileName, Spanned spanned)
{
	try
	{
		File root = new File(Path);
		if (!root.exists()) {
			root.mkdirs();
		}
		File gpxfile = new File(root, sFileName);
		FileWriter writer = new FileWriter(gpxfile);
		writer.append(spanned);
		writer.flush();
		writer.close();
		//Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
}
/*@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Class<? extends Fragment> fragment = mExamples.get(position).getFragment();
		if (fragment != null) {
			addFragment(GetPostsFragment.class);
		}
	}
 */
private void addFragment(Class<? extends Fragment> fragment) {
	try {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.frame_layout, fragment.newInstance());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	catch (Exception e) {
		Log.e(TAG, "Failed to add fragment", e);
	}
}

/**
 * Login example.
 */
private void setLogin() {
	// Login listener
	final OnLoginListener onLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
			mTextStatus.setText(reason);
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable) {
			mTextStatus.setText("Exception: " + throwable.getMessage());
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
			mTextStatus.setText("Thinking...");
		}

		@Override
		public void onLogin() {
			// change the state of the button or do whatever you want
			mTextStatus.setText("Logged in");
			loggedInUIState();
		}

		@Override
		public void onNotAcceptingPermissions(Permission.Type type) {
			//				toast(String.format("You didn't accept %s permissions", type.name()));
		}
	};

	mButtonLogin.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mSimpleFacebook.login(onLoginListener);
		}
	});
}

/**
 * Logout example
 */
private void setLogout() {
	final OnLogoutListener onLogoutListener = new OnLogoutListener() {

		@Override
		public void onFail(String reason) {
			mTextStatus.setText(reason);
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable) {
			mTextStatus.setText("Exception: " + throwable.getMessage());
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
			mTextStatus.setText("Thinking...");
		}

		@Override
		public void onLogout() {
			// change the state of the button or do whatever you want
			mTextStatus.setText("Logged out");
			loggedOutUIState();
		}

	};

	mButtonLogout.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mSimpleFacebook.logout(onLogoutListener);
		}
	});
}

private void setUIState() {
	if (mSimpleFacebook.isLogin()) {
		loggedInUIState();
	}
	else {
		loggedOutUIState();
	}
}

private void loggedInUIState() {
	mButtonLogin.setEnabled(false);
	mButtonLogout.setEnabled(true);
	//mListView.setVisibility(View.VISIBLE);
	mTextStatus.setText("Logged in");
	//addFragment(GetPostsFragment.class);
}

private void loggedOutUIState() {
	mButtonLogin.setEnabled(true);
	mButtonLogout.setEnabled(false);
	//mListView.setVisibility(View.INVISIBLE);
	mTextStatus.setText("Logged out");
}

}
