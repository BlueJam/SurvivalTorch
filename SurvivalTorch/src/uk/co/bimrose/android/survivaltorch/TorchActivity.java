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

public class TorchActivity extends SherlockFragmentActivity implements
		LightFragment.DaytimeListener, TorchFragment.BatteryLowListener,
		TorchFragment.AlertResetListener,
		BatteryFragment.BatteryChargeListener,
		LightFragment.LightSensorListener, TorchFragment.ServiceListener {

	boolean keepScreenOn = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
	private BatteryFragment batteryFrag = null;
	// used to stop the alert repeating when the light resonates above and below
	// the
	// threshold that has been set
	private boolean soundAlert = false;
	boolean closeEverything = false;
	Intent i;

	SharedPreferences prefsEdit;
	String activityRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// have they selected to keep the screen on?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean activityCheck = prefs.getBoolean("activityrunning", true);

		// this means that the Notification has been clicked to stop the service
		// it also closes this activity so the app is no longer running
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			closeEverything = extras.getBoolean("closeActivity");
			if (closeEverything) {
				stopService();
				if (!activityCheck) {
					finish();
				}
			}
		}

		keepScreenOn = Boolean.valueOf(prefs.getBoolean("keepscreenon", false));
		if (keepScreenOn) {
			// stops main screen closing
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		torchFrag = (TorchFragment) getSupportFragmentManager()
				.findFragmentById(R.id.torchfrag);

		if (torchFrag == null) {
			torchFrag = new TorchFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.torchfrag, torchFrag).commit();
		}

		lightFrag = (LightFragment) getSupportFragmentManager()
				.findFragmentById(R.id.lightfrag);

		if (lightFrag == null) {
			lightFrag = new LightFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.lightfrag, lightFrag).commit();
		}

		batteryFrag = (BatteryFragment) getSupportFragmentManager()
				.findFragmentById(R.id.batteryfrag);

		if (batteryFrag == null) {
			batteryFrag = new BatteryFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.batteryfrag, batteryFrag).commit();
		}

		// the TorchActivity is running!!!
		prefsEdit = PreferenceManager.getDefaultSharedPreferences(this);
		activityOpen(true);
	}

	public void toast(String toastMsg) {
		Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT)
				.show();
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
			// let the preferences know if the light sensor is present
			// if it is then don't show the option
			Intent i = new Intent(this, Preferences.class);
			i.putExtra("lightSensor", torchFrag.isThereALightSensor);
			startActivity(i);
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onlightChanged(float lux, int lightSensitivity) {
		// checks if the light level is above or equal to the threshold set
		torchFrag.message.setText(Float.toString(lux));
		if (torchFrag.lightSensitivity < 1000) {
			if (lux >= lightSensitivity) {

				playNotification();
				enableScreenTimeout();
			}
		}
	}

	public void playNotification() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (soundAlert) {
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
			// stops the alert playing over and over as the lighting / battery
			// charge changes
			soundAlert = false;
		}
	}

	public void enableScreenTimeout() {
		if (keepScreenOn) {
			// sets the screen to be able to timeout
			getWindow()
					.clearFlags(
							android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			keepScreenOn = false;
		}
	}

	@Override
	public void alertReset() {
		// lets the alert be used again, called by the TorchFragment when one of
		// it's buttons is clicked
		soundAlert = true;
	}

	@Override
	// sets the battery percentage
	public void batteryCharge(int pct) {
		torchFrag.setBatteryMessage(pct);
	}

	@Override
	public void onLowBattery() {
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

	@Override
	public void stopService() {
		if (i != null) {
			TorchActivityService.keepRunning = false;
			stopService(i);
			i = null;
		}
		CancelNotification(this, TorchActivityService.NOTIFY_ID);
	}

	public static void CancelNotification(Context ctx, int notifyId) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nMgr = (NotificationManager) ctx
				.getSystemService(ns);
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
