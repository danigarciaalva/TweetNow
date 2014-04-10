package mx.danigarciaalva.tweetnow;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

public class TweetNowProvider extends AppWidgetProvider{

	  public static final String LOCKSCREEN = "lockscreen";
	  @SuppressLint("NewApi")
	  @Override
	  public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		  Intent i = new Intent(context.getApplicationContext(),
		            TweetNowService.class);
		  i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		  context.startService(i);
	  }
	  
	  @Override
	  public void onReceive(Context context, Intent intent) {
		if(intent.getAction() == null){
			Intent i = new Intent(context.getApplicationContext(),
		            TweetNowService.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        context.startService(i);
		}else{
			super.onReceive(context, intent);
		}
	}
}
