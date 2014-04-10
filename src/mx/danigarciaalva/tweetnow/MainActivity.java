package mx.danigarciaalva.tweetnow;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TWITTER_CONSUMER_KEY = "6iN7RDHDhKwqeB0xjw";
    private static final String TWITTER_CONSUMER_SECRET = "7EB3C8VmXIW3mK8ardrpyORMVgWdVLmgSIL1DLaXAQ";
    private static final String TWITTER_CALLBACK_URL = "http://dragonflylabs.com.mx/";

    private static final String PREF_ACCESS_TOKEN = "accessToken";
    private static final String PREF_ACCESS_TOKEN_SECRET = "accessTokenSecret";
     
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private SharedPreferences mPrefs;
    private Button login;
    private ImageButton tweet;
    private boolean isLogued = false;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mPrefs = getSharedPreferences("twitterPrefs", MODE_PRIVATE);
        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        login = (Button)findViewById(R.id.login);
        tweet = (ImageButton)findViewById(R.id.tweet_main);
        if (mPrefs.contains(PREF_ACCESS_TOKEN) && !TextUtils.isEmpty(mPrefs.getString(PREF_ACCESS_TOKEN, null))){
        	login.setText(getResources().getString(R.string.logout));
        	login.setBackground(getResources().getDrawable(R.drawable.logout_button));
        	isLogued = true;
        }
        login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClick_Login(v);
			}
		});
        tweet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isLogued){
					Intent i = new Intent(getActivity(), TweetNowActivity.class);
					startActivity(i);
				}
			}
		});
	}
	
	private void onClick_Login(View button){
		System.out.println("Entré al evento");
		if(!isLogued)
	        if (mPrefs.contains(PREF_ACCESS_TOKEN) && !TextUtils.isEmpty(mPrefs.getString(PREF_ACCESS_TOKEN, null)))
	            loginAuthorisedUser();
	        else
	            loginNewUser();
		else
			Logout();
	}
	
	@Override
	public void onBackPressed() { 
		super.onBackPressed();
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mPrefs.contains(PREF_ACCESS_TOKEN) && !TextUtils.isEmpty(mPrefs.getString(PREF_ACCESS_TOKEN, null)))
			finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.about_menu){
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.about));
			LayoutInflater inflater = getLayoutInflater();
			
			builder.setView(inflater.inflate(R.layout.credits, null)).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	private void loginNewUser() {
        final boolean[] prevendDoubleCallbackEvent = {false};
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                	mTwitter = new TwitterFactory().getInstance();
                    mTwitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
                    mRequestToken = mTwitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result){
                    String url = mRequestToken.getAuthenticationURL();
                    WebView wv = new WebView(getActivity());
                    wv.loadUrl(url);
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    dialog.setContentView(wv);
                    wv.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            return true;
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            if (!prevendDoubleCallbackEvent[0] && url.contains(TWITTER_CALLBACK_URL)){
                                prevendDoubleCallbackEvent[0] = true;
                                String verifier = Uri.parse(url).getQueryParameter("oauth_verifier");

                                new AsyncTask<String, Void, AccessToken>(){
                                    @Override
                                    protected AccessToken doInBackground(String... params) {
                                        try {
                                            return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                                        } catch (TwitterException e) {
                                            e.printStackTrace();
                                            return null;
                                        } catch (Exception e) {
											return null;
										}
                                    }
                                    @Override
                                    protected void onPostExecute(AccessToken result) {
                                        super.onPostExecute(result);
                                        if (result != null){
                                            mTwitter.setOAuthAccessToken(result);
                                            saveAccessToken(result);
                                        }
                                    }
                                }.execute(verifier);
                                dialog.dismiss();
                            } else
                                super.onPageStarted(view, url, favicon);
                        }
                    });
                    dialog.show();
                }
            }
        }.execute();
    }

    @SuppressLint("NewApi")
	private void loginAuthorisedUser() {
        String token = mPrefs.getString(PREF_ACCESS_TOKEN, null);
        String secret = mPrefs.getString(PREF_ACCESS_TOKEN_SECRET, null);
        AccessToken at = new AccessToken(token, secret);
        mTwitter.setOAuthAccessToken(at);
        isLogued = true;
        login.setText(getResources().getString(R.string.logout));
        login.setBackground(getResources().getDrawable(R.drawable.logout_button));
    }
     
    @SuppressLint("NewApi")
	private void saveAccessToken(AccessToken at) {
        String token = at.getToken();
        String secret = at.getTokenSecret(); 
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_ACCESS_TOKEN, token);
        editor.putString(PREF_ACCESS_TOKEN_SECRET, secret);
        editor.commit();
        Toast.makeText(getActivity(), getResources().getString(R.string.welcome), Toast.LENGTH_SHORT).show();
        isLogued = true;
        login.setText(getResources().getString(R.string.logout));
        login.setBackground(getResources().getDrawable(R.drawable.logout_button));
    }

    @SuppressLint("NewApi")
	public void Logout() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        mTwitter.setOAuthAccessToken(null);
        mTwitter.shutdown();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_ACCESS_TOKEN, "");
        editor.putString(PREF_ACCESS_TOKEN_SECRET, "");
        editor.commit();
        isLogued = false;
        login.setText(getResources().getString(R.string.login));
        login.setBackground(getResources().getDrawable(R.drawable.login_button));
        Toast.makeText(getActivity(), getString(R.string.back_soon), Toast.LENGTH_LONG).show();
    }

    public Activity getActivity() {
        return this;
    }

}
