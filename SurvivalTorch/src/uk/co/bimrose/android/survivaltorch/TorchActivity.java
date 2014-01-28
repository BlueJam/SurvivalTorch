package uk.co.bimrose.android.survivaltorch;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TorchActivity extends SherlockFragmentActivity implements LightFragment.DaytimeListener,
		TorchFragment.AlertResetListener, LightFragment.LightSensorListener, TorchFragment.ServiceListener,
		BatteryFragment.AutoStopCleanup, LightFragment.AutoStopCleanup, TimerFragment.AutoStopCleanup {

	private boolean keepScreenOn = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
	private BatteryFragment batteryFrag = null;
	private TimerFragment timerFrag = null;

	private boolean soundAlert = false;
	private boolean closeEverything = false;

	public static boolean lightOn;
	public static boolean keepRunning;

	private boolean timerOn;

	private SharedPreferences prefsEdit;
	private SharedPreferences prefs;

	private boolean notifications;
	private boolean nSound;
	private boolean autoOn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//these two need to keep there state, the others need refreshing when the activity resumes
		boolean activityRunning = prefs.getBoolean("activityrunning", true);
		autoOn = prefs.getBoolean("autoon", false);
		getPrefs();

		// this means that the Notification has been clicked to stop the service it also closes this activity so the app
		// is no longer running
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			closeEverything = extras.getBoolean("closeActivity");
			if (closeEverything) {
				keepRunning = false;
				;
				if (!activityRunning) {
					finish();
				}
			}
			// makes sure the torch only gets turned on the first time the activity is created
			autoOn = false;
		}
		
		if (keepScreenOn) {
			// stops main screen closing
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		torchFrag = (TorchFragment) getSupportFragmentManager().findFragmentById(R.id.torchfrag);
		if (torchFrag == null) {
			torchFrag = new TorchFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.torchfrag, torchFrag).commit();
		}

		lightFrag = (LightFragment) getSupportFragmentManager().findFragmentById(R.id.lightfrag);
		if (lightFrag == null) {
			lightFrag = new LightFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.lightfrag, lightFrag).commit();
		}

		batteryFrag = (BatteryFragment) getSupportFragmentManager().findFragmentById(R.id.batteryfrag);
		if (batteryFrag == null) {
			batteryFrag = new BatteryFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.batteryfrag, batteryFrag).commit();
		}

		timerFrag = (TimerFragment) getSupportFragmentManager().findFragmentById(R.id.timerfrag);
		if (timerFrag == null) {
			timerFrag = new TimerFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.timerfrag, timerFrag).commit();
		}

		// the TorchActivity is running!!!
		prefsEdit = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (autoOn) {
			alertReset();
			TorchFragment.serviceCount++;
			startService("on");
			autoOn = false;
		}
		activityOpen(true);
	}

	@Override
	public void onResume() {
		getPrefs();
		super.onResume();
	}

	// Tracks if the activity was most recently open or closed, this is used when the notification strip is touched to
	// see if it leaves the activity open or calls finish() on it.
	private void activityOpen(boolean b) {
		prefsEdit.edit().putBoolean("activityrunning", b).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.options, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs:
			Intent i = new Intent(this, Preferences.class);
			i.putExtra("lightSensor", torchFrag.getIsThereALightSensor());
			startActivity(i);
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onlightChanged(float lux, int lightSensitivity) {
		torchFrag.setMessage(lux);
	}

	@Override
	public void autoStopCleanup() {
		// used when the transmition is interrupted by the light or battery sensor.
		toast("Cleanup");
		keepRunning = false;
		CancelNotification(TorchActivity.this, TorchActivityService.NOTIFY_ID);
		playNotification();
		enableScreenTimeout();
	}

	private void playNotification() {
		if (nSound) {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (soundAlert) {
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				r.play();
				soundAlert = false;
			}
		}
	}

	private void enableScreenTimeout() {
		if (keepScreenOn) {
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			keepScreenOn = false;
		}
	}

	@Override
	public void alertReset() {
		soundAlert = true;
	}

	@Override
	public void lightSensorCheck(boolean isThereALightSensor) {
		torchFrag.setIsThereALightSensor(isThereALightSensor);
	}

	@Override
	public void startService(String s) {
		stopNotification();
		startTimer();
		Intent i = new Intent(this, TorchActivityService.class);
		Bundle extras = new Bundle();
		// which button was clicked
		extras.putString("click", s);
		i.putExtras(extras);
		this.startService(i);
	}

	private void startTimer() {
		if (timerOn) {
			timerFrag.setTimerGoing();
		}
	}

	@Override
	public void stopNotification() {
		if(notifications){
			CancelNotification(this, TorchActivityService.NOTIFY_ID);
		}
	}

	private void CancelNotification(Context ctx, int notifyId) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
		nMgr.cancel(notifyId);
	}
	
	private void getPrefs(){
		nSound = prefs.getBoolean("nsound", false);
		timerOn = prefs.getBoolean("timeron", false);
		keepScreenOn = prefs.getBoolean("keepscreenon", false);
		notifications = prefs.getBoolean("notification", true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// toast("destroy");
		activityOpen(false);
		stopNotification();
	}

	public void toast(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
	}

}
