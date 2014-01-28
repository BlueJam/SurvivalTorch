package uk.co.bimrose.android.survivaltorch;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class TimerFragment extends SherlockFragment {

	private SharedPreferences prefs;
	private int timer;

	AutoStopCleanup autoStopCleanup;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		getPreferences();

		// Cleanup service / notification once the timer is done
		try {
			autoStopCleanup = (AutoStopCleanup) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement AlertReset");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.timerfrag, parent, false);

		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		// gets the timer time onResume in case the user has changed it in preferences
		getPreferences();
	}

	/**
	public void setTimerGoing() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				autoStopCleanup.autoStopCleanup();
			}
		}, timer * 10000);
	}
	**/
	
	public void setTimerGoing() {
	final Handler handler = new Handler();
	handler.postDelayed(new Runnable() {
	  @Override
	  public void run() {
		  autoStopCleanup.autoStopCleanup();
	  }
	}, timer * 10000);
	}

	private void getPreferences() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		timer = Integer.valueOf(prefs.getString("timer", "1"));
	}

	// Container Activity must implement this interface
	public interface AutoStopCleanup {
		public void autoStopCleanup();
	}

	public void toast(String toastMsg) {
		Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
	}

}