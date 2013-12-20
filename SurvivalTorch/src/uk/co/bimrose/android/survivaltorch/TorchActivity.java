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
		LightFragment.DaytimeListener, TorchFragment.AlertResetListener {

	boolean keepScreenOn = false;
	private TorchFragment torchFrag = null;
	private LightFragment lightFrag = null;
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
			// stops screen closing, should be an option
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
		torchFrag.message.setText(Float.toString(lux));
		if (lux >= lightSensitivity) {
			torchFrag.stop = true;
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (soundAlert) {
				Ringtone r = RingtoneManager.getRingtone(
						getApplicationContext(), notification);
				r.play();
			}
			soundAlert = false;
		}
	}

	@Override
	public void lightAlertReset() {
		soundAlert = true;
	}

}
