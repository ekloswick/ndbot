package nd.reu.ndbot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
	private static int count = 0;
    private WifiManager wifiManager;
    DataInputStream input;
    DataOutputStream output;
    private String command = "";
    private String serverIpAddress = null;// Im changing this
    private boolean connected = false;
    private ArrayList <Boolean> sendData = new ArrayList<Boolean>();
   // private ArrayList <Boolean> connections = new ArrayList<Boolean>();
    private ArrayList <AsyncTask<Void, Void, Void> > connections = new ArrayList<AsyncTask<Void, Void, Void> >();
    
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
    
    //text fields to show speed values of accelerometer
    private TextView sensorX;
    private TextView sensorY;
    
    //Sensor
    private SensorManager mSensorManager;
    private float mSensorX;
    private float mSensorY;
	
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
		
		//Accelerometer
		sensorX = (TextView) findViewById(R.id.sensorX_id);
		sensorY = (TextView) findViewById(R.id.sensorY_id);
		
		
		
		
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(listener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		//Text view tests
		
		// Initialize the buttons with listeners for click events
        mForwardButton = (Button) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "w";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mReverseButton = (Button) findViewById(R.id.reverseButton);
        mReverseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "s";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mTurnLeftButton = (Button) findViewById(R.id.turnLeftButton);
        mTurnLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "a";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mTurnRightButton = (Button) findViewById(R.id.turnRightButton);
        mTurnRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "d";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mForwardLeftButton = (Button) findViewById(R.id.forwardLeftButton);
        mForwardLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "q";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mForwardRightButton = (Button) findViewById(R.id.forwardRightButton);
        mForwardRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "e";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mStopButton = (Button) findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "x";
                	for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);
            }
        });
        
        mWifiButton = (Button) findViewById(R.id.connectBotButton);
        mWifiButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
           		 toast("Connecting...");
                    serverIpAddress = mIPAddress.getText().toString();
                    if (!serverIpAddress.equals("")) {
                    	AsyncThread blah = new AsyncThread();
                    	connections.add(blah);
                    	((AsyncThread) connections.get(connections.size()-1)).setSend(false);
                    	//sendData.add(new Boolean(false));
                    	blah.execute();
                    }
            }
        });
	}
	private SensorEventListener listener=new SensorEventListener() {
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// not used
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
			count++;
			/*if(count <6)
			{
				count++;
				return;
			}
			*/	
			mSensorX = event.values[0];
			mSensorY = event.values[1];
			DecimalFormat df = new DecimalFormat("#.##");
			String mSensorXString = df.format(mSensorX);
			String mSensorYString = df.format(mSensorY);
			sensorX.setText(mSensorXString);
			sensorY.setText(mSensorYString);
			command = mSensorXString+","+ mSensorYString;
        	for(int i =0; i < connections.size(); i++)
        		((AsyncThread) connections.get(i)).setSend(true);
			count =0;
		}
	};
	
	
	
	public void sendMessage(){
		
		
		
		
	}
	
	class AsyncThread extends AsyncTask<Void, Void, Void> {
    	private boolean send;
    	//String bacon = "Happy happy joy joy";
    	int state = 0;
		@Override
		protected Void doInBackground(Void... params) {			
			try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                PrintWriter output = new PrintWriter(socket.getOutputStream());
               // BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                state = 0;
                
               // connected = socket.isConnected();
                publishProgress();
                
                while (!connections.isEmpty()) {
                    try {
                        state = 1;
                        if (send) {
                        	state = 3;
	                        send = false;
	                        output.println(command);
	                        output.flush();
                        }
                        
                        if (connections.indexOf(this) == -1) {
                            state = 2;
                            output.close();
                           // input.close();
                            break;
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
                connections.remove(this);
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
		public void setSend(boolean b){
			send=b;
		}
    }
	
	public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
	
	public void onStop(){
		mSensorManager.unregisterListener(listener);
		super.onStop();
	}
}

