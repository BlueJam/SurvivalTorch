package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class CloseService extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TorchActivityService.keepRunning = false;
		Toast.makeText(getApplicationContext(), "this is my Toast message!!! =)",
				   Toast.LENGTH_LONG).show();
		finish();
	}
	
}