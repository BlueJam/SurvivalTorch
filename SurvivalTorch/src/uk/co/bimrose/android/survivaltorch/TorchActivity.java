package uk.co.bimrose.android.survivaltorch;

import android.os.Bundle;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TorchActivity extends SherlockFragmentActivity {
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //stops screen closing, should be an option
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
	    if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
	      getSupportFragmentManager().beginTransaction()
	                                 .add(android.R.id.content,
	                                      new TorchFragment()).commit();
	    }
	  }
	}
