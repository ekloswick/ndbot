package nd.reu.ndbot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ControlsActivity extends Activity
{
	// Buttons
	private Button mForwardButton;
    private Button mReverseButton;
    private Button mTurnLeftButton;
    private Button mTurnRightButton;
    private Button mForwardLeftButton;
    private Button mForwardRightButton;
    private Button mStopButton;
    private Button mWifiButton;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controls);
		setTitle(R.string.controls_string);
	}
	
	public void onStart() {
		super.onStart();
		
		// Initialize the buttons with listeners for click events
        mForwardButton = (Button) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("w");
            }
        });
        
        mReverseButton = (Button) findViewById(R.id.reverseButton);
        mReverseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("s");
            }
        });
        
        mTurnLeftButton = (Button) findViewById(R.id.turnLeftButton);
        mTurnLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("a");
            }
        });
        
        mTurnRightButton = (Button) findViewById(R.id.turnRightButton);
        mTurnRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("d");
            }
        });
        
        mForwardLeftButton = (Button) findViewById(R.id.forwardLeftButton);
        mForwardLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("q");
            }
        });
        
        mForwardRightButton = (Button) findViewById(R.id.forwardRightButton);
        mForwardRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("e");
            }
        });
        
        mStopButton = (Button) findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage("x");
            }
        });
        
        mWifiButton = (Button) findViewById(R.id.connectBotButton);
        mWifiButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
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
	
	private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        
        

        // Check that there's actually something to send
        
            // Get the message bytes and tell the BluetoothChatService to write
            

            // Reset out string buffer to zero and clear the edit text field
            
           // mOutEditText.setText(mOutStringBuffer);
    }
}

