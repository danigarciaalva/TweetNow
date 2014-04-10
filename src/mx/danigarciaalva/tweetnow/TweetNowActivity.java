package mx.danigarciaalva.tweetnow;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TweetNowActivity extends Activity implements RecognitionListener{

	private SpeechRecognizer speech;
	private Button send,retry;
	private EditText tweet;
	private boolean isSuccess;
	private boolean isRecording;
	private Context context = this;
	private RecognitionListener listener = this;
    private static final String PREF_ACCESS_TOKEN = "accessToken";
    private static final String TWITTER_CONSUMER_KEY = "6iN7RDHDhKwqeB0xjw";
    private static final String TWITTER_CONSUMER_SECRET = "7EB3C8VmXIW3mK8ardrpyORMVgWdVLmgSIL1DLaXAQ";
    private static final String PREF_ACCESS_TOKEN_SECRET = "accessTokenSecret";
    private Twitter mTwitter; 
    private SharedPreferences mPrefs;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(checkVoiceRecognition() && checkInternetConnection()){
			setContentView(R.layout.activity_tweetnow);
			speech = SpeechRecognizer.createSpeechRecognizer(this);
		    speech.setRecognitionListener(this);
		    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, getString(R.string.locale));
		    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
		    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		    send = (Button)findViewById(R.id.send);
		    retry = (Button)findViewById(R.id.retry);
		    send.setEnabled(false);
		    retry.setEnabled(false); 
		    send.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					String twt = tweet.getText().toString().trim();
					if(twt.length() > 140){
						Toast.makeText(context, getString(R.string.too_long), Toast.LENGTH_LONG).show();
					}else{
						postTweet();
					}
				}
			});
		    retry.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					speech.stopListening();
					speech.cancel();
					speech.destroy();
					speech = null;
					speech = SpeechRecognizer.createSpeechRecognizer(context);
				    speech.setRecognitionListener(listener);
					speech.startListening(intent);
					isSuccess = false;
					isRecording = true;
					updateButtons();
				}
			});
		    tweet = (EditText)findViewById(R.id.tweet);
		    mPrefs = getSharedPreferences("twitterPrefs", MODE_PRIVATE);
	        mTwitter = new TwitterFactory().getInstance();
	        mTwitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
	        if (mPrefs.contains(PREF_ACCESS_TOKEN) && !TextUtils.isEmpty(mPrefs.getString(PREF_ACCESS_TOKEN, null))){
	        	String token = mPrefs.getString(PREF_ACCESS_TOKEN, null);
	            String secret = mPrefs.getString(PREF_ACCESS_TOKEN_SECRET, null);
	            AccessToken at = new AccessToken(token, secret);
	            mTwitter.setOAuthAccessToken(at);
	        	speech.startListening(intent);
	        }else{
	        	Intent i = new Intent(this, MainActivity.class);
	        	startActivity(i);
	        	finish();
	        }
		    
		}
	}
	
	public boolean checkVoiceRecognition() {
		  PackageManager pm = getPackageManager();
		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		  if (activities.size() == 0) {
			  Toast.makeText(this.getApplicationContext(), getString(R.string.voice_unavailable),Toast.LENGTH_SHORT).show();
			  return false;
		  }
		  return true;
	  }

	public boolean checkInternetConnection(){
		ConnectivityManager conexion= (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net=conexion.getActiveNetworkInfo();
        if(net!=null&&net.getState()== NetworkInfo.State.CONNECTED)
            return true;
        Toast.makeText(context,getString(R.string.no_connection), Toast.LENGTH_LONG).show();
        return false;
	}
	@Override
	public void onBeginningOfSpeech() {
		isSuccess = false;
		isRecording = true;
		updateButtons();
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndOfSpeech() {
		isRecording = false;
		updateButtons();
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResults(Bundle results) {
		tweet.setText("");
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if(matches.size() > 0 && matches.get(0).trim().length() > 0){
			tweet.setText(matches.get(0));
			isSuccess = true;
		}else{
			Toast.makeText(this, getString(R.string.no_results),  Toast.LENGTH_SHORT).show();
		}
		updateButtons();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateButtons(){
		retry.setEnabled((isRecording) ? false : true);
		send.setEnabled((isSuccess) ? true : false);
	}
	
	@Override
	protected void onDestroy() {
		if(speech != null){
			speech.stopListening();
			speech.cancel();
			speech.destroy();
		}
		super.onDestroy();

	}
	
	private void postTweet() {
        new AsyncTask<Void, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mTwitter.updateStatus(tweet.getText().toString().trim());
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                showTweetPosted(result);
                finish();
            }
        }.execute();
    }

    private void showTweetPosted(boolean value){
        if (value){
        	tweet.setText("");
            Toast.makeText(this,getString(R.string.twt_posted), Toast.LENGTH_LONG).show();
            MediaPlayer player = MediaPlayer.create(this, R.raw.bird);
            player.start();
        } else {
            Toast.makeText(this, getString(R.string.twt_no_posted), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	onDestroy();
    }
}
