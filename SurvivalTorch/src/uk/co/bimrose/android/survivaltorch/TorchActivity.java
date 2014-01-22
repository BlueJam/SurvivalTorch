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
		TorchFragment.BatteryLowListener, TorchFragment.AlertResetListener, BatteryFragment.BatteryChargeListener,
		LightFragment.LightSensorListener, TorchFragment.ServiceListener {

	boolean keepScreenOn = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
	private BatteryFragment batteryFrag = null;

	private boolean soundAlert = false;
	boolean closeEverything = false;
	Intent i;
	
	public static boolean lightOn;
	public static boolean keepRunning;

	SharedPreferences prefsEdit;
	String activityRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// have they selected to keep the screen on?
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean activityCheck = prefs.getBoolean("activityrunning", true);

		// this means that the Notification has been clicked to stop the service it also closes this activity so the app
		// is no longer running
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			closeEverything = extras.getBoolean("closeActivity");
			if (closeEverything) {
				keepRunning = false;
				if (!activityCheck) {
					finish();
				}
			}
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

	public void toast(String toastMsg) {
		Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
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
		if (lightSensitivity < 1000) {
			if (lux >= lightSensitivity) {
				sendChange();
				CancelNotification(TorchActivity.this, TorchActivityService.NOTIFY_ID);
				playNotification();
				enableScreenTimeout();
			}
		}
	}

	private void sendChange() {
		Intent intent = new Intent("lightChange");
		intent.putExtra("dayLight", true);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
		// lets the alert be used again, called by the TorchFragment when one of the buttons is pressed
		soundAlert = true;
	}

	@Override
	// sets the battery percentage
	public void batteryCharge(int pct) {
		torchFrag.setBatteryMessage(pct);
	}

	@Override
	public void onLowBattery() {
		CancelNotification(TorchActivity.this, TorchActivityService.NOTIFY_ID);
		enableScreenTimeout();
		playNotification();
	}

	@Override
	public void lightSensorCheck(boolean isThereALightSensor) {
		torchFrag.isThereALightSensor = isThereALightSensor;
	}

	@Override
	public void startService(String s) {
		stopService();
		i = new Intent(this, TorchActivityService.class);
		Bundle extras = new Bundle();
		// which button was clicked
		extras.putString("click", s);
		i.putExtras(extras);
		this.startService(i);
	}

	//An array to store all of the previous services? run through and kill them all?
	@Override
	public void stopService() {
		if (i != null) {
			keepRunning = false;
			//stopService(i);
			i = null;
		}
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
