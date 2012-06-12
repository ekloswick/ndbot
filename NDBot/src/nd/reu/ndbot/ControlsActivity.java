package nd.reu.ndbot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ControlsActivity extends Activity
{
    private WifiManager wifiManager;
    DataInputStream input;
    DataOutputStream output;
    private String command = "";
    private String serverIpAddress = "10.24.189.99";
    private boolean connected = false;
    private boolean sendData = false;
	
	// Buttons and Fields
	private EditText mIPAddress;
    private TextView wifiStatus;
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
		
		wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
        	toast("Enabling Wifi...");
        	wifiManager.setWifiEnabled(true);
        }
		
		mIPAddress = (EditText) findViewById(R.id.ipAddress_id);
		wifiStatus = (TextView) findViewById(R.id.wifiConnected_id);
		
		// Initialize the buttons with listeners for click events
        mForwardButton = (Button) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "w";
                if (connected)
                	sendData = true;
            }
        });
        
        mReverseButton = (Button) findViewById(R.id.reverseButton);
        mReverseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "s";
                if (connected)
                	sendData = true;
            }
        });
        
        mTurnLeftButton = (Button) findViewById(R.id.turnLeftButton);
        mTurnLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "a";
                if (connected)
                	sendData = true;
            }
        });
        
        mTurnRightButton = (Button) findViewById(R.id.turnRightButton);
        mTurnRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "d";
                if (connected)
                	sendData = true;
            }
        });
        
        mForwardLeftButton = (Button) findViewById(R.id.forwardLeftButton);
        mForwardLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "q";
                if (connected)
                	sendData = true;
            }
        });
        
        mForwardRightButton = (Button) findViewById(R.id.forwardRightButton);
        mForwardRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "e";
                if (connected)
                	sendData = true;
            }
        });
        
        mStopButton = (Button) findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "x";
                if (connected)
                	sendData = true;
            }
        });
        
        mWifiButton = (Button) findViewById(R.id.connectBotButton);
        mWifiButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (!connected) {
           		 toast("Connecting...");
                    //serverIpAddress = mIPAddress.getText().toString();
                    if (!serverIpAddress.equals("")) {
                    	new AsyncThread().execute();
                    }
                }
            }
        });
	}
	
	class AsyncThread extends AsyncTask<Void, Void, Void> {
    	
    	//String bacon = "Happy happy joy joy";
    	int state = 0;
    	
		@Override
		protected Void doInBackground(Void... params) {			
			try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                state = 0;
                
                connected = socket.isConnected();
                publishProgress();
                
                while (connected) {
                    try {
                        state = 1;
                        if (sendData) {
                        	state = 3;
	                        sendData = false;
	                        output.println(command);
	                        output.flush();
                        }
                        
                        if (!connected) {
                            state = 2;
                            output.close();
                            input.close();
                        }

                        publishProgress();
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
            }
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {            
            switch (state) {
				case 0:
					wifiStatus.setText("Not connected");
					break;
				case 1:
					wifiStatus.setText("Connected");
					break;
				case 2:
					wifiStatus.setText("Disconnected");
					mWifiButton.setEnabled(true);
					break;
				case 3:
					wifiStatus.setText("Sending message...");
					break;
				default:
					wifiStatus.setText("WTF");
					break;
			}
		}
    }
	
	public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

