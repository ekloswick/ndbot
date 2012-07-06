/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nd.reu.ndbot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

//import nd.reu.ndbot.BluetoothActivity.AsyncThread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothActivity extends Activity {
	
    public static String SERVERIP = null;//"10.24.189.99";
    public static final int SERVERPORT = 8080;
    private ServerSocket ss;
    public Boolean isCancelled = false;
    DataInputStream input;
    DataOutputStream output;
    private TextView wifiStatus;
    private TextView bluetoothStatus;
    private TextView receivedMessage;
    private SurfaceView viewToSend;
	private SurfaceHolder surfaceHolder;
	private boolean previewRunning;
	private String frameToSend = "";
    
    // Debugging
    private static final String TAG = "NDBotBluetooth";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private TextView bluetoothMessage;
    private Button mWifiButton;
    private Button mBluetoothButton;
    private Button mResetButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothActivityService mChatService = null;
    // Wifi Manager
    private WifiManager wifiManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.bluetooth);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.bluetooth_string);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        bluetoothMessage = (TextView) findViewById(R.id.bluetoothMessage_id);
        
        wifiStatus = (TextView) findViewById(R.id.wifiStatus_id);
        bluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus_id);
        receivedMessage = (TextView) findViewById(R.id.receivedMessage_id);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        //new AsyncCameraThread().execute();
        
        mWifiButton = (Button) findViewById(R.id.wifiButton_id);
        mWifiButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SERVERIP = getLocalIpAddress();
                new AsyncThread().execute();
            }
        });
        
        mBluetoothButton = (Button) findViewById(R.id.bluetoothButton_id);
        mBluetoothButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	connectToBluetooth();
            }
        });
        
        mResetButton = (Button) findViewById(R.id.resetButton_id);
        mResetButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	restartActivity();
            }
        });
        
        mResetButton.setEnabled(false);
        
        // enable wifi connection if not already on
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
        	toast("Enabling Wifi...");
        	wifiManager.setWifiEnabled(true);
        }
        
        // If BT is not on, request that it be enabled.
        // setupBluetoothConnection() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupBluetoothConnection();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothActivityService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupBluetoothConnection() {
        Log.d(TAG, "setupBluetoothConnection()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothActivityService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothActivityService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        message = message + "r";
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
          //  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
          //  mChatService.
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
           // mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The Handler that gets information back from the BluetoothActivityService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothActivityService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    bluetoothStatus.setText("Connected");
                   // mConversationArrayAdapter.clear();
                    break;
                case BluetoothActivityService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    bluetoothStatus.setText("Connecting...");
                    break;
                case BluetoothActivityService.STATE_LISTEN:
                case BluetoothActivityService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    bluetoothStatus.setText("Not Connected");
                    break;
                }

                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
               // String readMessage = new String(readBuf, 0, msg.arg1);
                String readMessage = new String(readBuf);
               // bluetoothMessage.setText("");
                bluetoothMessage.setText(readMessage);
                //Toast.makeText(getApplicationContext(), readMessage,
                //        Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupBluetoothConnection();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    
    public void connectToBluetooth() {
    	Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    class AsyncThread extends AsyncTask<Void, Void, Void> {
    	
    	String bacon = "0,0";
    	int state = 0;
    	
		@Override
		protected Void doInBackground(Void... params) {
			try {
                if (SERVERIP != null) {
                	state = 0;
                    ss = new ServerSocket(SERVERPORT);
                	publishProgress();
                	
                    Socket client = ss.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter output = new PrintWriter(client.getOutputStream(), true);
                	publishProgress();
                	
                    while (!isCancelled) {
                    	state = 1;
                    	publishProgress();
                        bacon = input.readLine();
                        output.println(frameToSend);
                        frameToSend = "";
                    }
                    
                    input.close();
                    output.close();
                    ss.close();
                } else {
                	state = 3;
                	publishProgress();
                }
            } catch (Exception e) {
            	state = 4;
            	publishProgress();
                e.printStackTrace();
            }
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			switch (state) {
				case 0:
                    wifiStatus.setText("Listening on IP: " + SERVERIP);
                    mResetButton.setEnabled(true);
					break;
				case 1:
                    wifiStatus.setText("Connected.");
                    mResetButton.setEnabled(true);
					break;
				case 2:
					wifiStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
					break;
				case 3:
                    wifiStatus.setText("Couldn't detect internet connection.");
					break;
				case 4:
	                wifiStatus.setText("Error");
					break;
				default:
					wifiStatus.setText("Not Connected");
					break;
			}
			interpretAccelerometer(bacon);
			//sendMessage(bacon);
			receivedMessage.setText(bacon);
		}
    }
    
    private void interpretAccelerometer(String xy){
    	String[] strArr = xy.split(",");
    	//sendMessage(strArr[0]);
    	//float x = Float.valueOf(strArr[0].trim()).floatValue();
    	float x = Float.parseFloat(strArr[0]);
    	float y = new Float(strArr[1]);

    	float xVar=0, yVar=0;
    	int m = 0, ix = 0, iy = 0;
    	
    	if(x>3 && x<10)
    	{
    		xVar= (float) ((x-3)/7.0*255);
    		ix = Math.round(xVar);
    	}
    	else if(x<-3 && x>-10)
    	{
    		xVar = (float) ((-x-3)/7.0*255);
    		ix = Math.round(xVar);
    		Math.abs(xVar);
    	}
    	
    	if(y>3 && y<10)
    	{
    		yVar= (float) ((y-3)/7.0*255);
        	iy = Math.round(yVar);
    	}
    	else if(y<-3 && y>-10)
    	{
    		yVar = (float) ((-y-3)/7.0*255);
        	iy = Math.round(yVar);
    		Math.abs(yVar);
    	}
    	
    	if(xVar> 255)
    		xVar = 255;
    	if(yVar > 255)
    		yVar = 255;

    	if(x < -3)
    	{
    		if(y>3)
    			m =3;
    		else if(y < -3)
    			m = 1;
    		else
    			m=2;
    	}
    	else if(x >3)
    	{
    		if(y>3)
    			m=8;
    		else if(y < -3)
    			m=6;
    		else
    			m = 7;
    	}
    	else
    	{
    		if(y>3)
    			m =5;
    		else if(y < -3)
    			m =4;
    		else
    			m=0;
    	}
    	
    	String message = Integer.toString(m)+","+Integer.toString(ix)+","+Integer.toString(iy);
    	sendMessage(message);
    }
    
    private String getLocalIpAddress()
    {
           try
           {
             for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces()))
             {
                 for (InetAddress addr : Collections.list(intf.getInetAddresses()))
                 {
                     if (!addr.isLoopbackAddress())
                         return addr.getHostAddress();
                 }
             }
             throw new RuntimeException("No network connections found.");
           }
           catch (Exception ex)
           {
                toast("Error getting IP address: " + ex.getLocalizedMessage());
                return "Unknown";
           }
    }

    public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    public void restartActivity() {
    	try
		{
			ss.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent intent = getIntent();
		finish();
		startActivity(intent);
    }

	
    
    /*
    
    class AsyncCameraThread extends AsyncTask<Void, Void, Void> implements Callback{

    	int state = 0;
    	private Camera camera;
    	
    	@Override
		protected Void doInBackground(Void... params) {
			try {
				if (state == 0)
					publishProgress();
				
            } catch (Exception e) {
            	publishProgress();
                e.printStackTrace();
            }
			return null;
		}
    	
    	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    		if (previewRunning){
    			camera.stopPreview();
    		}
    		Camera.Parameters p = camera.getParameters();
    		List<Camera.Size> sizesList = p.getSupportedPreviewSizes();
    		Camera.Size previewSize = sizesList.get(0);
    		
    		// for logging the allowable screen resolutions, highest to lowest
    		for (int i = 0; i < sizesList.size(); i++){
    	        previewSize = (Size) sizesList.get(i);
    	        Log.i("PictureSize", "Supported Size. Width: " + previewSize.width + " Height: " + previewSize.height); 
    	    }
    		
    		p.setPreviewSize(previewSize.width, previewSize.height);
    		//p.setPreviewFormat(PixelFormat.RGB_565); // causes runtime error
    		camera.setParameters(p);

    		try {
    			camera.setPreviewDisplay(holder);
    			camera.setPreviewCallback( mPreviewCallback );
    			camera.startPreview();
    			previewRunning = true;
    		}
    		catch (IOException e) {
    			Log.e(TAG,e.getMessage());
    			e.printStackTrace();
    		}
    	}
        
        public void surfaceCreated(SurfaceHolder holder) {
    		camera = Camera.open();
    		
    		if (camera != null){
    			Camera.Parameters params = camera.getParameters();
    			camera.setParameters(params);
    			camera.setDisplayOrientation(90);
    		}
    		else {
    			Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
    			finish();
    		}
    	}
        
        public void surfaceDestroyed(SurfaceHolder holder) {
        	if (camera != null) {
        		camera.stopPreview();
        		previewRunning = false;
        		camera.release();
        	}
    	}
        
        Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
    	{
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
            	for (int i = 0; i < data.length - 1; i++){
            		frameToSend = frameToSend + data[i];
            	}
            	
            	//Toast.makeText(VideoStreamActivity.this, "Toast", Toast.LENGTH_SHORT).show();
            	Log.d(TAG, "onPreviewFrame! data is " + (data == null ? "null!" : "ok!"));
            }
        };
		
		
		@Override
		protected void onProgressUpdate(Void... values) {
			switch (state) {
				case 0:
			        // sets up the camera for output
			        viewToSend = (SurfaceView) findViewById(R.id.surfaceView_id);
			        surfaceHolder = viewToSend.getHolder();
			        surfaceHolder.addCallback(this);
			        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			        state = 1;
                    break;
				case 1:
					break;
				case 2:
					break;
				case 3:
                    break;
				case 4:
	                break;
				default:
					break;
			}
		}
    }
    */
    
}