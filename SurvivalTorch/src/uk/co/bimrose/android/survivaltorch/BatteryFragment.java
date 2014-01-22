package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class BatteryFragment extends SherlockFragment {

	AutoStopCleanup autoStopCleanup;
	int batteryPct;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Makes sure that the container activity has implemented the callback interface
		try {
			autoStopCleanup = (AutoStopCleanup) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement AlertReset");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.torchfrag, parent, false);
		// need to check if they want to save power on low battery or not, only register if they do***************
		IntentFilter f = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		getActivity().registerReceiver(onBattery, f);

		return (result);
	}

	public void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(onBattery);
		super.onDestroy();
	}

	BroadcastReceiver onBattery = new BroadcastReceiver() {
		// Receives a broadcast when the battery level drops
		public void onReceive(Context context, Intent intent) {
			int pct = 100 * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1)
					/ intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
			if (pct <= batteryPct) {
				autoStopCleanup.autoStopCleanup();
			}
		}
	};

	// Container Activity must implement this interface
	public interface AutoStopCleanup {
		public void autoStopCleanup();
	}
}