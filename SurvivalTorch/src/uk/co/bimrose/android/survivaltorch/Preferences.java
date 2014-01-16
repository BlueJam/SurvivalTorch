package uk.co.bimrose.android.survivaltorch;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Preferences extends SherlockPreferenceActivity {

	boolean lightSensor;
	PreferenceScreen preferenceScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// used to save the state of the TorchActivity for the notification
		// click, is it running, or has it been destroyed?
		preferenceScreen = getPreferenceScreen();
		Preference activityRunning = findPreference("activityrunning");
		preferenceScreen.removePreference(activityRunning);

		isThereALightSensor();
	}

	public void isThereALightSensor() {
		// passed from the TorchActivity, is there a lightsensor?
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			lightSensor = extras.getBoolean("lightSensor");
		}

		if (!lightSensor) {
			// If there isn't a light sensor then this preference is obsolete
			// IIt is deleted from the preference menu
			Preference lightSensitivityPrefs = findPreference("lightsensitivity");
			preferenceScreen.removePreference(lightSensitivityPrefs);
		}

	}

	/**
	 * @SuppressWarnings("deprecation")
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 *           super.onCreate(savedInstanceState); if (Build.VERSION.SDK_INT <
	 *           Build.VERSION_CODES.HONEYCOMB) {
	 *           addPreferencesFromResource(R.xml.pref_display); } }
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	 * @Override public void onBuildHeaders(List<Header> target) {
	 *           loadHeadersFromResource(R.xml.preference_headers, target); }
	 **/
}