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
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TorchActivity extends SherlockFragmentActivity implements LightFragment.DaytimeListener,
		TorchFragment.AlertResetListener, LightFragment.LightSensorListener, TorchFragment.ServiceListener,
		BatteryFragment.AutoStopCleanup, LightFragment.AutoStopCleanup {

	boolean keepScreenOn = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
	private BatteryFragment batteryFrag = null;

	private boolean soundAlert = false;
	boolean closeEverything = false;

	public static boolean lightOn;
	public static boolean keepRunning;
	public static boolean running = false;

	SharedPreferences prefsEdit;
	String activityRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean activityCheck = prefs.getBoolean("activityrunning", true);
		boolean autoOn = Boolean.valueOf(prefs.getBoolean("autoon", false));
		// this means that the Notification has been clicked to stop the service it also closes this activity so the app
		// is no longer running
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			closeEverything = extras.getBoolean("closeActivity");
			if (closeEverything) {
				keepRunning = false;;
				if (!activityCheck) {
					finish();
				}
			}
			//makes sure the torch only gets turned on the first time the activity is created
			autoOn = false;
		}
		
		if (autoOn) {
			startService("on");
			TorchFragment.serviceCount++;
			autoOn = false;
		}
		
		keepScreenOn = Boolean.valueOf(prefs.getBoolean("keepscreenon", false));
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

		// the TorchActivity is running!!!
		prefsEdit = PreferenceManager.getDefaultSharedPreferences(this);
		activityOpen(true);
	}

	public void activityOpen(boolean b) {
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
			i.putExtra("lightSensor", torchFrag.isThereALightSensor);
			startActivity(i);
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onlightChanged(float lux, int lightSensitivity) {
		torchFrag.message.setText("LUX = " + Float.toString(lux));
	}

	@Override
	public void autoStopCleanup() {
		// used when the transmition is interrupted by the light or battery sensor.
		keepRunning = false;
		CancelNotification(TorchActivity.this, TorchActivityService.NOTIFY_ID);
		playNotification();
		enableScreenTimeout();
	}

	public void playNotification() {
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (soundAlert) {
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
			soundAlert = false;
		}
	}

	public void enableScreenTimeout() {
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
		torchFrag.isThereALightSensor = isThereALightSensor;
	}

	@Override
	public void startService(String s) {
		stopService();
		Intent i = new Intent(this, TorchActivityService.class);
		Bundle extras = new Bundle();
		// which button was clicked
		extras.putString("click", s);
		i.putExtras(extras);
		this.startService(i);
	}

	@Override
	public void stopService() {
		CancelNotification(this, TorchActivityService.NOTIFY_ID);
	}

	public void CancelNotification(Context ctx, int notifyId) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
		nMgr.cancel(notifyId);
	}

	@Override
	public void onStart() {
		super.onStart();
		// toast("start");
		activityOpen(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		// toast("resume");
		activityOpen(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		// toast("pause");
		activityOpen(true);
	}

	@Override
	public void onStop() {
		super.onStop();
		// toast("stop");
		activityOpen(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// toast("destroy");
		activityOpen(false);
		CancelNotification(this, TorchActivityService.NOTIFY_ID);
	}

}
