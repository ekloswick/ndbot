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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends Activity {
	
    public static String SERVERIP = null;//"10.24.189.99";
    public static final int CONTROLSERVERPORT = 8080;
    public static final int STREAMSERVERPORT = 8888;
    private ServerSocket ssControls, ssStream, sendSocket;
    public static final int BTSERVERPORT = 8008;

    //public Boolean isCancelled = false;
    public Boolean isBTCancelled = false;
    public Boolean isStreamCancelled = false;
    public Boolean isCameraCancelled = false;
    public Boolean isControlsCancelled = false;
    
    DataInputStream input;
    DataOutputStream output;
    
    private TextView wifiStatus;
    private TextView bluetoothStatus;
    private TextView receivedMessage;
    private TextView compassReading;
    
    private SurfaceHolder surfaceHolder;
    private boolean route = false;
    private boolean previewRunning;
    private int bufferSize = 4096;
    private byte[] frameToSend = new byte[bufferSize];
    private SurfaceView viewSurface;
    
    // Debugging
    private static final String TAG = "NDBotBluetooth";
    private static final boolean D = false;

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
    private Button mWifiButton;
    private Button mBluetoothButton;
    private Button mResetButton;
    private Camera camera;
    
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
    
    //Sensors
    SensorManager mSensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
     
    //private float[][] values;
    private float highValue = -10;
    private float lowValue = 10;
    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float[] gravity;
    private float[] linear_acceleration;
     
    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;
    private static float smoothed[] = new float[3];
    TextView readingAzimuth, readingPitch, readingRoll;
    
    //Threads
    
    AsyncControlsThread conThread;
    ArrayList<routeThread> routeList = new ArrayList<routeThread>();
    AsyncStreamThread strThread;
    AsyncCameraThread camThread;
  	private Timer timer;
  	
  	//AI
  	AI ai;
  	String current;
    String override;
    private boolean collision = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.bluetooth);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.bluetooth_string);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
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
        
        ai = new AI();	
		 
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		    
		valuesAccelerometer = new float[3];
		valuesMagneticField = new float[3];
		gravity = new float[3];
		linear_acceleration = new float[3];
		  
		matrixR = new float[9];
		matrixI = new float[9];
		matrixValues = new float[3];
        
       // Intent intent=new Intent("CompassService");
       // startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        mWifiButton = (Button) findViewById(R.id.wifiButton_id);
        mWifiButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SERVERIP = getLocalIpAddress();

               conThread =  new AsyncControlsThread();
               conThread.execute();
                strThread = new AsyncStreamThread();
                strThread.execute();
                camThread = new AsyncCameraThread();
                camThread.execute();
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
                isBTCancelled = true;
                isStreamCancelled = true;
                isCameraCancelled = true;
                isControlsCancelled = true;
            	restartActivity();
            	mWifiButton.setEnabled(true);
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
        
        viewSurface = (SurfaceView) findViewById(R.id.surfaceView_id);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

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
        mSensorManager.registerListener(listener,sensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
       // mSensorManager.registerListener(listener,sensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
        timer = new Timer();
        TimerTask timertask = new TimerTask(){
        	public void run(){
        		long startedAt = System.currentTimeMillis();
       // 		boolean finishedCorrectly = true;
        		highValue = valuesAccelerometer[2];
        		lowValue = valuesAccelerometer[2];
        		 while ((System.currentTimeMillis() - startedAt) < 100) {
        			 if(valuesAccelerometer[2]> highValue)
         				{
        				 highValue = valuesAccelerometer[2];
        				 Log.d("high", Float.toString(highValue));
         				}
         			else if(valuesAccelerometer[2] < lowValue)
         			{
         				lowValue = valuesAccelerometer[2];
         				Log.d("low", Float.toString(lowValue));
         			}
         			if(highValue - lowValue > 4.5)
         			{
         				if(valuesAccelerometer[2] == highValue)
         					collision = ai.isCollision(lowValue, highValue);
         				else
         					collision = ai.isCollision(highValue, lowValue);
         				if(collision)
         				{
         					Log.d("in collision", "blah");
         					if(routeList.get(routeList.size()-1).isAlive())
         					{
         						String[] newRoute = ai.reRoute(((routeThread) routeList.get(routeList.size()-1)).getI());
         						int[] newTime = ai.reTime(((routeThread) routeList.get(routeList.size()-1)).getI(), ((routeThread) routeList.get(routeList.size()-1)).getJ());
         						routeList.get(routeList.size()-1).interrupt();
         						routeList.add(new routeThread(newRoute, newTime));
         						route = true;
         						routeList.get(routeList.size()-1).start();
         						Log.d("new Route", "route");
         					}
         				}
         				Log.d("3", Float.toString(lowValue)+ Float.toString(highValue));
         				break;
         			}
        		}
        			
        	}
        };
        timer.scheduleAtFixedRate(timertask,0,1000);
    }

    private void setupBluetoothConnection() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothActivityService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        mSensorManager.unregisterListener(listener);
        timer.cancel();
    }

    
    @Override
    public void onStop() {
        super.onStop();
        isBTCancelled = true;
        isStreamCancelled = true;
        isCameraCancelled = true;
        isControlsCancelled = true;
        //closing threads and sockets
    	//	if(routeToTravel1 != null)
    		//	if(routeToTravel1.isAlive())
    		//		routeToTravel1.interrupt();
        if(!routeList.isEmpty())
        {
        	for(int i=0; i< routeList.size(); i++)
        	{
        		if(routeList.get(i).isAlive())
        			routeList.get(i).interrupt();
        	}
        }
    		conThread.cancel(true);
    		strThread.cancel(true);
    		if(!ssControls.isClosed())
    			try {
    				ssControls.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        try {
					ssStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        try {
					sendSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth service
        if (mChatService != null) mChatService.stop();
    }
    

    private void ensureDiscoverable() {
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
           // toast("not connected");
            return;
        }

        message = message + "r";
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }
    
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
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
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

    
    class AsyncControlsThread extends AsyncTask<Void, Void, Void> {
    	
    	String bacon = "0,0";
    	int state = 0;
    	
		@Override
		protected Void doInBackground(Void... params) {
			try {
                if (SERVERIP != null) {
                	state = 0;
                    ssControls = new ServerSocket(CONTROLSERVERPORT);
                	publishProgress();
                	BluetoothSocket bluetooth = new BluetoothSocket();
                	bluetooth.execute();
                    Socket client = ssControls.accept();
                    state = 1;
                    BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                	publishProgress();
                	Log.d("ssControls", "beforewhile");
                    while (!isControlsCancelled) {
                    	if(isCancelled())
                    	{
                    		bluetooth.cancel(true);
                    		break;
                    	}
                    	state = 1;

                    //	Log.d("ssControls", "beforeRead");
                    	if(input.ready())
                    	{
                    		bacon = input.readLine();
                      	publishProgress();
                    	}
                    }
                    
                    input.close();
                    output.close();
                    ssControls.close();
                    bluetooth.cancel(true);
                   // if(ssControls.isClosed())
                 //   	Log.d("ssControls", "true");
                   // else
                 //   	Log.d("ssControls", "false");
                    
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
			if(bacon.substring(0,2).equals("AI"))
			{
				if(!route)
				{
					Log.d("route to parse", bacon);
					parseRoute(bacon);
				}
				
				
			}
			else
			{
				current = interpretAccelerometer(bacon);
				sendMessage(current);
				receivedMessage.setText(bacon);
				route = false;
			}

		}
   }
    
    class BluetoothSocket extends AsyncTask<Void, Void, Void>{
  		InetAddress serverAddr;
    	String accelReadings = "hi";
    	boolean run =true;
  		@Override
  		protected Void doInBackground(Void... params) {
  			try {
  	      ServerSocket sendSocket = new ServerSocket(BTSERVERPORT); 
  	      //Log.d("before", "hi");
  	      Socket client = sendSocket.accept();
  	      //Log.d("after", "bye");
  	      PrintWriter output = new PrintWriter(client.getOutputStream(), true);

  	     // BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  	     while(!isBTCancelled){
  	    	 if(isCancelled())
  	    	 {
  	    		 break;
  	    	 }
  	    	 output.println(Float.toString(valuesAccelerometer[0])+ " " + Float.toString(valuesAccelerometer[1])+ " " + Float.toString(valuesAccelerometer[2]));
  	    	 //Log.d("accel Reading", accelReadings);
  	    	// publishProgress();
  	    	 Thread.sleep(100);
  	     }
  	     output.close();
  	     sendSocket.close();

  	     
  			}
  			catch (Exception e) {
  	      Log.e("ClientActivity", "S: Error", e);
  	  }
  					
  			return null;
  		}
  		
  		protected void onProgressUpdate(Void... values) {  
  		}	
  		
  	}
    
    class AsyncStreamThread extends AsyncTask<Void, Void, Void> {

    	int state = 0;
    	
		@Override
		protected Void doInBackground(Void... params) {
			try {
                if (SERVERIP != null) {
                	state = 0;
                    publishProgress();
                	ssStream = new ServerSocket(STREAMSERVERPORT);
                    Socket client = ssStream.accept();
                    DataInputStream dis = new DataInputStream(client.getInputStream());
                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                	state = 1;
                	publishProgress();
                	
                    while (!isStreamCancelled) {                    	
                    	if (frameToSend != null && frameToSend.length > 0) {
                    		// write the number of bytes to read
                    		dos.writeInt(frameToSend.length);
                    		dos.flush();
                    		
                    		// get first handshake
                    		dis.readInt();
                    		
                    		// write the actual byte array
                    		if (frameToSend != null) {
                    			dos.write(frameToSend, 0, frameToSend.length);
                    			dos.flush();
                    		}
                    	
                    		// get second handshake
                    		dis.readInt();
                    		
                    		// cleanup
	                    	publishProgress();
	                    	frameToSend = null;
	                    	Thread.sleep(100);
                    	}
                    }
                    dos.close();
                    dis.close();
                    client.close();
                    ssStream.close();
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
    }
    
    class AsyncCameraThread extends AsyncTask<Void, Void, Void> implements SurfaceHolder.Callback {

	    SurfaceHolder surfaceHolder = viewSurface.getHolder();
		int state = 0;
		Bitmap bitmap = null;
		
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			
			state = 1;
			//Log.d("STATE CHECK", "State: 1");
			
			if (camera != null)
				camera.setDisplayOrientation(90);
			else {
				toast("Camera not available!");
				finish();
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (previewRunning){
				camera.stopPreview();
			}
			
			state = 2;
			//Log.d("STATE CHECK", "State: 2");
			
			Camera.Parameters p = camera.getParameters();
			List<Camera.Size> sizesList = p.getSupportedPreviewSizes();
			Camera.Size previewSize = sizesList.get(sizesList.size() - 1);
			
			// for logging the allowable screen resolutions, highest to lowest
			/*for (int i = 0; i < sizesList.size(); i++){
		        previewSize = (Size) sizesList.get(i);
		        Log.i("PictureSize", "Supported Size. Width: " + previewSize.width + " Height: " + previewSize.height); 
		    }*/
			
			p.setPreviewSize(previewSize.width, previewSize.height);
			camera.setParameters(p);

			//Log.d("STATE CHECK", "Right outside try/catch in surfaceChanged");
			
			try {
				camera.setPreviewDisplay(holder);
				camera.setPreviewCallback( mPreviewCallback );
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				camera.startPreview();
				previewRunning = true;
			}
			catch (IOException e) {
				//Log.e(TAG,e.getMessage());
				e.printStackTrace();
			}
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) {
			state = 4;
			//Log.d("STATE CHECK", "State: 4");
			
			/*camera.stopPreview();
			previewRunning = false;
			camera.release();*/
		}
		
		Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
		{
			@Override
			public void onPreviewFrame(byte[] data, Camera camera)
			{
				state = 3;
				if ( camera != null && isCameraCancelled == false)
				{
                    Camera.Parameters parameters = camera.getParameters();
                    int imageFormat = parameters.getPreviewFormat();

                    if ( imageFormat == ImageFormat.NV21 )
                    {
                        int w = parameters.getPreviewSize().width;
                        int h = parameters.getPreviewSize().height;
                        YuvImage yuvImage = new YuvImage( data, imageFormat, w, h, null );
                        Rect rect = new Rect( 0, 0, w, h );
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg( rect, 10, outputStream );
                        
                        //Log.d("IMAGING", "NV21 format");
                        if (frameToSend == null)
                        	frameToSend = outputStream.toByteArray();
                    }
                    else if ( imageFormat == ImageFormat.JPEG || imageFormat == ImageFormat.RGB_565 )
                    {
                        //Log.d("IMAGING", "JPEG/RGB format");
                    	if (frameToSend == null)
                    		frameToSend = data;
                    }
				}
                else if (camera == null)
                {
                    Log.e( getLocalClassName(), "Camera is null" );
                }
			}
		};
		
		@Override
		protected Void doInBackground(Void... arg0)
		{
			//Log.d("STATE CHECK", "doInBackground start");
			surfaceCreated(surfaceHolder);
			surfaceChanged(surfaceHolder, state, state, state);
			camera.setPreviewCallback(mPreviewCallback);
			
			while (previewRunning && !isCancelled()) {
				publishProgress();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			switch (state) {
				case 0:
					toast("State 0, starting...");
					break;
				case 1:
					toast("State 1, camera opened");
					break;
				case 2:
					//Log.d("PublishProgress", "case 2");
					break;
				case 3:
					//Log.d("PublishProgress", "case 3");
					break;
				case 4:
					toast("State 4, camera closed");
					break;
				default:
					toast("Default, something's up");
					break;
			}
		}
	}
    
    private String interpretAccelerometer(String xy){
    	String[] strArr = xy.split(",");
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
    			m =3; //forward right
    		else if(y < -3)
    			m = 1; //forward left
    		else
    			m=2; //forward
    	}
    	else if(x >3)
    	{
    		if(y>3) //reverse right
    			m=8;
    		else if(y < -3) 
    			m=6; //reverse left
    		else
    			m = 7; //reverse
    	}
    	else
    	{
    		if(y>3) 
    			m =5; //right
    		else if(y < -3)
    			m =4; //left
    		else 
    			m=0; //stop
    	}
    	
    	return Integer.toString(m)+","+Integer.toString(ix)+","+Integer.toString(iy);
    	//sendMessage(message);
    }
    
    
    private String getLocalIpAddress()
    {
           try
           {
             for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces()))
             {
                 for (InetAddress addr : Collections.list(intf.getInetAddresses()))
                 {
                     if (!addr.isLoopbackAddress() && (addr instanceof Inet4Address)) {
                         return addr.getHostAddress();
                     }
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
    		if (!ssControls.isClosed())
    			ssControls.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		camera.stopPreview();
		previewRunning = false;
		camera.release();
		Intent intent = getIntent();
		finish();
		startActivity(intent);
    }
    
  	@Override
  	public void onConfigurationChanged(Configuration newConfig){
  		super.onConfigurationChanged(newConfig);
  	}
  	
    
    private SensorEventListener listener=new SensorEventListener() {
		
    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// not used
			
    	}

			@Override
			public void onSensorChanged(SensorEvent event) {
			   final float alpha = (float) 0.8;
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         // smoothed = LowPassFilter.filter(event.values, valuesAccelerometer);

	          valuesAccelerometer[0] = event.values[0];
	          valuesAccelerometer[1] = event.values[1];
	          valuesAccelerometer[2] = event.values[2];
				}
	       else
	      	 return;
			}
	};
	

		private void parseRoute(String routeData){
			route = true;
			//routeToTravel = new routeThread();
			routeData = routeData.substring(6);
			String[] route =routeData.split("\\*");
			Log.d("route[0]", route[0]);
			String[] time = new String[route.length];
			float [] routeTime = new float[route.length];
			int [] routeTimeInt = new int[route.length];
			for(int i = 0; i<route.length; i++)
			{
				int index = route[i].indexOf('~', 8);
				time[i] = route[i].substring(index+1);
				route[i] = route[i].substring(0,index);
				Log.d("parsing route", route[i]);
				Log.d("parsing time", time[i]);
				routeTime[i] = Float.parseFloat(time[i]);
				routeTime[i] *=1000;
				routeTimeInt[i] = (int) routeTime[i];
				//Log.d("ClientActivity", time[i]);
			}
				routeList.add(new routeThread(route, routeTimeInt));
				routeList.get(routeList.size()-1).start();

		}

		class routeThread extends Thread{
			public int i =0;
			public int j = 0;
			public String[] routeString;
			public int[] time;
			public routeThread(String[] r, int[] t)
			{
				routeString = r;
				time = t;
			}
			public int getI(){
				return i;
			}
			public int getJ(){
				return j;
			}
			
			@Override
			public void run()
			{
				for(int k = 0; k < routeString.length; k++)
				{

					routeString[k] = interpretAccelerometer(routeString[k]); //gets the directions in ints
				}
				ai = new AI(routeString, time, current);
				Log.d("in thread", "hi");
				for(i = 0; i<routeString.length; i++)
				{
					if(route)
					{

					//publishProgress(gatherData[0].route[i]);
					
					try {
						sendMessage(routeString[i]);
						ai.setFuture(routeString[i]);
							//Thread.
							
							Log.d("thread sleep time", Integer.toString(time[i]));
							int j = 0;
							//while(j < time[i] )
						//	{
								if(Thread.interrupted())
								{
									Log.d("interrupted", routeString[i]);
									return;
								}
							//	j +=250;
								Thread.sleep(time[i]);
						//	}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
						return;
				}
				sendMessage(interpretAccelerometer("0.00,0.00"));
				route = false;
				return;
			}
		};
			
		
}  
