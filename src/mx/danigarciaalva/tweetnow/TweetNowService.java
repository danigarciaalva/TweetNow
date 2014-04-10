package mx.danigarciaalva.tweetnow;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class TweetNowService extends Service{

	@Override
	  public void onStart(Intent intent, int startId) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
			int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.widget_layout);
			Intent i=new Intent(this, TweetNowActivity.class);
			PendingIntent pi= PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, pi);
			appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
			stopSelf();
	  }
 
	  @Override
	  public IBinder onBind(Intent intent) {
	    return null;
	  }
}
