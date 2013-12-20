package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class BatteryFragment extends SherlockFragment {
	
	BatteryChargeListener bCListener;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        	bCListener = (BatteryChargeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement BatteryChargeListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.torchfrag, parent, false);
		
		return (result);
	}

	public void onResume() {
		super.onResume();
		IntentFilter f = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		getActivity().registerReceiver(onBattery, f);
	}

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(onBattery);
		super.onPause();
	}

	BroadcastReceiver onBattery = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int pct = 100 * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 1)
					/ intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
			bCListener.batteryCharge(pct);
		}
	};
	
	public interface BatteryChargeListener{
		public void batteryCharge(int pct);
	}
	
}