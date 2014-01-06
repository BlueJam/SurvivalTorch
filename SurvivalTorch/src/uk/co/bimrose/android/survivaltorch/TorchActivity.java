package uk.co.bimrose.android.survivaltorch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TorchActivity extends SherlockFragmentActivity implements
		LightFragment.DaytimeListener, TorchFragment.BatteryLowListener,
		TorchFragment.AlertResetListener,
		BatteryFragment.BatteryChargeListener,
		LightFragment.IsThereALightSensor, LightFragment.GetIsThereALightSensor {

	boolean keepScreenOn = false;
	private boolean isThereALightSensor = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
	private BatteryFragment batteryFrag = null;
	// used to stop the alert repeating when the light resonates above and below
	// the
	// threshold that has been set
	private boolean soundAlert = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// have they selected to keep the screen on?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
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
			torchFrag.stop = true;
			startActivity(new Intent(this, Preferences.class));
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onlightChanged(float lux, int lightSensitivity) {
		// checks if the light level is above or equal to the threshold set
		torchFrag.message.setText(Float.toString(lux));
		if (lux >= lightSensitivity) {
			torchFrag.stop = true;
			playNotification();
			// stops the alert playing over and over as the lighting changes
			soundAlert = false;
			enableScreenTimeout();
		}
	}

	public void playNotification() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (soundAlert) {
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		}
	}

	public void enableScreenTimeout() {
		if (keepScreenOn) {
			// sets the screen to be able to timeout
			getWindow()
					.clearFlags(
							android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public void lightAlertReset() {
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
		// plays notification
		playNotification();
	}

	@Override
	public void sensorCheck(boolean isThereALightSensor) {
		torchFrag.isThereALightSensor = isThereALightSensor;
	}

	@Override
	public boolean getIsThereALightSensor() {
		return torchFrag.isThereALightSensor;
	}

}
