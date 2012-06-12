package nd.reu.ndbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{
	// Buttons
	private Button mControlsButton;
	private Button mBluetoothButton;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(R.string.app_name);
	}
	
	public void onStart() {
		super.onStart();
		
		 // Initialize the buttons with listeners for click events
        mControlsButton = (Button) findViewById(R.id.controlsActivityButton);
        mControlsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //TextView view = (TextView) findViewById(R.id.edit_text_out);
                //String message = view.getText().toString();
                //sendMessage("w");
            	Intent myIntent = new Intent(v.getContext(), ControlsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
        
        mBluetoothButton = (Button) findViewById(R.id.bluetoothActivityButton);
        mBluetoothButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //sendMessage("s");
            	Intent myIntent = new Intent(v.getContext(), BluetoothActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
	}
	
	public synchronized void onResume() {
		super.onResume();
	}
	
	public synchronized void onPause() {
		super.onPause();
	}
	
	public void onStop() {
		super.onStop();
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
