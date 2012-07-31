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
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ControlsActivity extends Activity
{
    private WifiManager wifiManager;
    DataInputStream input;
    DataOutputStream output;
    private String command = "";
    private String serverIpAddress = null;// Im changing this
    // private ArrayList <Boolean> connections = new ArrayList<Boolean>();
    private ArrayList <AsyncTask<Void, Void, Void> > connections = new ArrayList<AsyncTask<Void, Void, Void> >();
    ArrayAdapter<String> arrayAdapter; 
    AsyncThread currentBot;
    int bufferSize = 4096;
    private boolean cameraConnected = false;
    private byte[] frameToGet = new byte[bufferSize];
    public static final int STREAMSERVERPORT = 8888;
    int surfaceWidth, surfaceHeight;
    
    private boolean accel = false;
    private boolean button = true;
    private boolean route = false;
	// Buttons and Fields
	private EditText mIPAddress;
    private TextView wifiStatus;
    private TextView accelText;
    
	private ImageButton mForwardButton;
    private ImageButton mReverseButton;
    private ImageButton mTurnLeftButton;
    private ImageButton mTurnRightButton;
    private ImageButton mForwardLeftButton;
    private ImageButton mForwardRightButton;
    private ImageButton mStopButton;
    private Button mWifiButton;
    private SurfaceView videoStream;
	private SurfaceHolder surfaceHolder;
    private Bitmap imageToShow;
    
    //text fields to show speed values of accelerometer
    private TextView sensorX;
    private TextView sensorY;
    
    //Sensor
    private SensorManager mSensorManager;
    Sensor mAccelerometer;
    private float mSensorX;
    private float mSensorY;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controlsa);
		setTitle(R.string.controls_string);
		//arrayAdapter =  new ArrayAdapter<String>(this, R.id.botNames);
		
	}
	
	public void onStart() {
		super.onStart();
        enableButtons();
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
        	toast("Enabling Wifi...");
        	wifiManager.setWifiEnabled(true);
        }
		
		wifiStatus = (TextView) findViewById(R.id.wifiConnected_id);
		accelText = (TextView) findViewById(R.id.bot_accel_id);
        toast("create wifi button");
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
			
			mSensorX = event.values[0];
			mSensorY = event.values[1];
			
			DecimalFormat df = new DecimalFormat("#.##");
			String mSensorXString = df.format(mSensorX);
			String mSensorYString = df.format(mSensorY);
			
			sensorX.setText(mSensorXString);
			sensorY.setText(mSensorYString);
			command = mSensorXString+","+ mSensorYString;
			
        	/*for(int i =0; i < connections.size(); i++) {
        		//((AsyncThread) connections.get(i)).setSend(true);
        	}*/
			if(currentBot !=null)
				currentBot.setSend(true);
        	
		}
	};
	
	class AsyncThread extends AsyncTask<Void, Void, Void> {
    	private boolean send;
    	private boolean activeBot;
    	BluetoothSocket bluetooth;
    	InetAddress serverAddr;
    	int state = 0;
		@Override
		protected Void doInBackground(Void... params) {			
			try {
                serverAddr = InetAddress.getByName(serverIpAddress);
                //Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                state = 0;
                bluetooth = new BluetoothSocket();
                publishProgress();
                bluetooth.execute();
                while (!connections.isEmpty()) {
                	if(isCancelled())
                	{
                		bluetooth.cancel(true);
                		break;
                	}
                    try {
                        state = 1;
                       // Log.d("count", Integer.toString(count));
                        if (send) {
                        	state = 3;
	                        send = false;
	                        output.println(command);
	                       // Log.d("send commands", command);
	                        output.flush();
                        }
                       
                        if (connections.indexOf(this) == -1) {
                            state = 2;
                            output.close();
                            input.close();
                            break;
                        }
                        
                        publishProgress();
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                
                socket.close();
                //Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                //Log.e("ClientActivity", "C: Error", e);
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
		public void setActive(boolean b){
			activeBot = b;
		}
		public String getServerAddr(){
			return serverAddr.toString();
		}
   }
	
	class BluetoothSocket extends AsyncTask<Void, Void, Void>{
		InetAddress serverAddr;
  	String accelReadings = "hi";
  	boolean run =true;
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.d("inBluetooth", accelReadings);
	      serverAddr = InetAddress.getByName(serverIpAddress);
	      Socket socket = new Socket(serverAddr, 8888);
	     // PrintWriter output = new PrintWriter(socket.getOutputStream());
	      BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	     while(run){
	    	 if(isCancelled())
	    		 break;
	    	// Log.d("accel Reading before", accelReadings);
	    	 accelReadings = input.readLine();
	    	 Log.d("accel Reading", accelReadings);
	    	 publishProgress();
	    	 Thread.sleep(100);
	     }
	     socket.close();
	     input.close();
	     
			}
			catch (Exception e) {
	      Log.e("ClientActivity", "S: Error", e);
	  }
					
			return null;
		}
		
		protected void onProgressUpdate(Void... values) {  
		//	Log.d("onUpdate", accelReadings);
      if(accelReadings !=null)
				accelText.setText(accelReadings);
		}
		
		
	}
	
	public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.control_menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.buttons:
	        	if(button)
	        		return true;
	        	else
	        	{
	        		disableAccel();
	        		enableButtons();
	        	}
	            return true;
	        case R.id.accel:
	        	if(accel)
	        		return true;
	        	else
	        	{
	        		disableButtons();
	        		enableAccel();
	        	}
	            
	            return true;
	        case R.id.connect:
	        	showDialog(0);
	        	return true;
	        case R.id.bots:
	        	showDialog(1);
	        	return true;
	        case R.id.makeRoute:
	        	Intent intent = new Intent(this, PlanRouteActivity.class);
	        	startActivity(intent);
	        	return true;
	        case R.id.routes:
	        	Intent intent1 = new Intent(this, RouteActivity.class);
	        	startActivityForResult(intent1, 1);
	        }
	        return false;
	    }
	
	public void createButtons(){
		mForwardButton = (ImageButton) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "-8.00,0.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
        mReverseButton = (ImageButton) findViewById(R.id.reverseButton);
        mReverseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "8.00,0.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
        
        mTurnLeftButton = (ImageButton) findViewById(R.id.turnLeftButton);
        mTurnLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "0.00,-8.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
        
        mTurnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        mTurnRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "0.00,8.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
        
        mForwardLeftButton = (ImageButton) findViewById(R.id.forwardLeftButton);
        mForwardLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "-8.00,-8.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
        
        mForwardRightButton = (ImageButton) findViewById(R.id.forwardRightButton);
        mForwardRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "-8.00,8.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                        	currentBot.setSend(true);
                
            }
        });
        
        mStopButton = (ImageButton) findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(route)
            		route = false;
                command = "0.00,0.00";
                	/*for(int i =0; i < connections.size(); i++)
                		((AsyncThread) connections.get(i)).setSend(true);*/
                if(currentBot != null)
                	currentBot.setSend(true);
            }
        });
           
	}
	
	public void createAccel(){
		sensorX = (TextView) findViewById(R.id.sensorX_id);
		sensorY = (TextView) findViewById(R.id.sensorY_id);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
	
	public void enableButtons(){
		button = true;
		setContentView(R.layout.controlsa);
		wifiStatus = (TextView) findViewById(R.id.wifiConnected_id);
		createButtons();
		mStopButton.setVisibility(View.VISIBLE);
		mForwardRightButton.setVisibility(View.VISIBLE);
		mForwardButton.setVisibility(View.VISIBLE);
		mForwardLeftButton.setVisibility(View.VISIBLE);
		mReverseButton.setVisibility(View.VISIBLE);
		mTurnRightButton.setVisibility(View.VISIBLE);
		mTurnLeftButton.setVisibility(View.VISIBLE);
		
		mStopButton.setEnabled(true);
		mForwardButton.setEnabled(true);
		mForwardLeftButton.setEnabled(true);
		mForwardRightButton.setEnabled(true);
		mTurnRightButton.setEnabled(true);
		mTurnLeftButton.setEnabled(true);
		mReverseButton.setEnabled(true);
		

		toast("enable Buttons");
	       
	}
	
	public void disableButtons(){
		button = false;
		mStopButton.setVisibility(View.GONE);
		mForwardRightButton.setVisibility(View.GONE);
		mForwardButton.setVisibility(View.GONE);
		mForwardLeftButton.setVisibility(View.GONE);
		mReverseButton.setVisibility(View.GONE);
		mTurnRightButton.setVisibility(View.GONE);
		mTurnLeftButton.setVisibility(View.GONE);
		
		mStopButton.setEnabled(false);
		mForwardButton.setEnabled(false);
		mForwardLeftButton.setEnabled(false);
		mForwardRightButton.setEnabled(false);
		mTurnRightButton.setEnabled(false);
		mTurnLeftButton.setEnabled(false);
		mReverseButton.setEnabled(false);

		mStopButton = null;
		mForwardButton = null;
		mForwardLeftButton= null;
		mForwardRightButton = null;
		mTurnRightButton = null;
		mTurnLeftButton = null;
		mReverseButton = null;
		
		toast("disable buttons");
	}
	
	public void enableAccel(){
		accel = true;
		//setContentView(R.layout.controlsa);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.controlsb);
		wifiStatus = (TextView) findViewById(R.id.wifiConnected_id);
		createAccel();
		mSensorManager.registerListener(listener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		toast("enable Accel");

		videoStream = (SurfaceView) findViewById(R.id.surfaceView_id);
        surfaceHolder = videoStream.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceWidth = videoStream.getWidth();
        surfaceHeight = videoStream.getHeight();
	}
	
	public void disableAccel(){
		mSensorManager.unregisterListener(listener);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		accel = false;
		sensorX = null;
		sensorY = null;

	}
	
	public void switchBot(int index){
		currentBot.setActive(false);
		currentBot =  (AsyncThread) connections.get(index);
		currentBot.setActive(true);
		//((AsyncThread) connections.get(index)).setActive(true);
	}
	
	public void onStop(){
		if(mSensorManager != null)
			mSensorManager.unregisterListener(listener);
		super.onStop();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if(accel)
	    	return;
      if(button)
      {
       	enableButtons();
      }
	}
	
	protected AlertDialog onCreateDialog(int id){
		switch(id){
			case 0:
				LayoutInflater factory = LayoutInflater.from(this);
				final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
				final SharedPreferences sharedPreferences = getSharedPreferences("previousBots",MODE_PRIVATE);
				final  Map<String, ?> map = sharedPreferences.getAll();
				Set<String> set =  map.keySet();
				String[] keys = new String[set.size()];
				keys = (String[]) set.toArray(keys);	
				ArrayAdapter<String> botAdapter = new ArrayAdapter<String>(this, R.layout.previous_bot_text_view,keys); 
				final ListView botList = (ListView) textEntryView.findViewById(R.id.previousBots);
				botList.setAdapter(botAdapter);
				
				
				AlertDialog.Builder botBuilder = new AlertDialog.Builder(ControlsActivity.this);
				botBuilder.setIcon(R.drawable.alert_dialog_icon)
				.setTitle("Connect to a robot")
				.setView(textEntryView)
				.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                	 mIPAddress = (EditText) textEntryView .findViewById(R.id.ipMenu);
                	 serverIpAddress = mIPAddress.getText().toString();
                     if (!serverIpAddress.equals("")) {
                     	AsyncThread blah = new AsyncThread();
                     	connections.add(blah);
                     	//arrayAdapter.add((Integer.toString(connections.size()-1)));
                     	((AsyncThread) connections.get(connections.size()-1)).setSend(false);
                     	if(connections.size() == 1)
                     		currentBot = blah;
                     		//((AsyncThread) connections.get(0)).setActive(true);
                     	blah.execute();
                     	SharedPreferences.Editor editor = sharedPreferences.edit();
                     	editor.putString(serverIpAddress,serverIpAddress);
                   	 	editor.commit();
                   	 	Log.d("ip address",sharedPreferences.getString(serverIpAddress, "fail"));
                     	
                     	if (accel && !cameraConnected) {
                     		AsyncGetStreamThread cameraThread = new AsyncGetStreamThread();
                     		cameraThread.execute();
                     		cameraConnected = true;
                     	}
                     }

                 }
             })
             .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {

                     /* User clicked cancel so do nothing */
                 }
             });
				final AlertDialog alertBot =  botBuilder.create();
				botList.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						String tempString = ((TextView) arg1).getText().toString();
						int index = tempString.indexOf(' ');
						serverIpAddress = tempString.substring(index+1).trim();
						Log.d("ip", serverIpAddress);
						AsyncThread blah = new AsyncThread();
			           	connections.add(blah);
			           	blah.setSend(false);
			           	if(connections.size() == 1)
			           		currentBot = blah;
			           	blah.execute();
			           	
			           	if (accel && !cameraConnected) {
			         		AsyncGetStreamThread cameraThread = new AsyncGetStreamThread();
			         		cameraThread.execute();
			         		cameraConnected = true;
			         	}
			           	
						alertBot.dismiss();
					}
					
				});
				return alertBot;
         
		case 1:
			LayoutInflater factory2 = LayoutInflater.from(this);
	         final View view = factory2.inflate(R.layout.alert_dialog_2, null);
	         //((ListView) listView).setAdapter(arrayAdapter);
	       //  TextView current = (TextView) view.findViewById(R.id.currentBot);
	        // for(int i = 0; i < connections.size(); i++)
	        	// if(connections)
	       //  int currentIndex =-1;
	         arrayAdapter =  new ArrayAdapter<String>(this, R.layout.alert_dialog_list_text_view);
	         if(currentBot != null)
	         {
		         for(int i = 0; i <connections.size(); i++)
		         {
		        	 arrayAdapter.add((Integer.toString(i))+" "+((AsyncThread) connections.get(i)).getServerAddr());
		        	 //if(((AsyncThread) connections.get(i)).getServerAddr().equals(currentBot.getServerAddr()))
		        	// {
		        		 TextView text = (TextView) view.findViewById(R.id.currentBotText);
		        		 text.setText(currentBot.getServerAddr());
		        	// }
		        	 //Log.d("currentIndex", Integer.toString(currentIndex));
		         }
	         }

	         final ListView list = (ListView) view.findViewById(R.id.botListView2);
	         list.setAdapter(arrayAdapter);
	        // ((CheckedTextView) list.getChildAt(currentIndex)).setChecked(true);
	         AlertDialog.Builder builder = new AlertDialog.Builder(ControlsActivity.this);
	         
	             builder.setIcon(R.drawable.alert_dialog_icon);
	             builder.setTitle("Connect to a robot");
	             builder.setView(view).setNegativeButton("Back", new DialogInterface.OnClickListener() {
	                 public void onClick(DialogInterface dialog, int whichButton) {
	                     /* User clicked cancel cancels automatically */
	                 }
	             });
	             
	            final  AlertDialog alert = builder.create();
	          //  list.
	            //((CheckedTextView) list.getChildAt(currentIndex)).setChecked(true);
	         list.setOnItemClickListener(new OnItemClickListener() {
	        	 @Override
	        	 public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	        	  CheckedTextView tv = (CheckedTextView) arg1;
	        	  String s = tv.getText().toString().substring(0,1);
	        	  //toast(s);
	        	  int index = Integer.parseInt(s);
	        	  switchBot(index);
	        	  alert.dismiss();
	       	 } });
	        return alert;
		}
		return null;
    }
	
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data){
		if(requestCode == 1 && resultCode == Activity.RESULT_OK)
		{
			route = true;
			command = "AI"+data.getStringExtra("nd.reu.ndbot.RouteActivity");
			currentBot.setSend(true);
		}
		
	}

	class GatherData{//to make sending data to routeThread easier
		String[] route;
		int[] time;
		
		GatherData(String[] r, int[] t){
			route = r.clone();
			time = t.clone();
		}
	}

	class AsyncGetStreamThread extends AsyncTask<Void, Void, Void> {
    	String bacon = "";
    	int state = 0, readLength = -1;
    	Canvas easel = null;
    	byte[] temporary = new byte[bufferSize];
    	
		@Override
		protected Void doInBackground(Void... params) {			
			try {
                state = 0;
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                //Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, STREAMSERVERPORT);
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                state = 1;
                publishProgress();
                
                while (state == 1) {
                    try {
                    	// get number of bytes to read
                    	readLength = dis.readInt();
                        //Log.d("First Read Length", "" + readLength);
                    	
                    	// first handshake
                    	dos.writeInt(5);
                    	dos.flush();
                    	
                    	// get actual byte array
                    	if (readLength > 0 && readLength <= temporary.length)
                    		dis.read(temporary, 0, readLength);
                    	else while (dis.available() > 0)
                    		dis.read();
                    	
                    	frameToGet = temporary;
                    	
                    	// second handshake
                    	dos.writeInt(5);
                    	dos.flush();
                    	
                    	// cleanup
                    	temporary = new byte[bufferSize];
                    	publishProgress();
                    	//frameToGet = new byte[bufferSize];
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                        state = 0;
                    }
                }
                
                dis.close();
                dos.close();
                socket.close();
                
                cameraConnected = false;
                
                //Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                //Log.e("ClientActivity", "C: Error", e);
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
			}
            
            onDraw();
        }
		
		public void onDraw() {
            //Log.d("onDraw", "Beginning of function");
            
			if (frameToGet != null)
			{
	            //Log.d("onDraw", "frameToGet is not null");
	            easel = surfaceHolder.lockCanvas();
	            
	            if (easel != null && readLength > 0) {
	                //Log.d("onDraw", "easel is not null");
	                
	            	try {
		            	imageToShow = BitmapFactory.decodeByteArray(frameToGet, 0, frameToGet.length);
		            } catch (Exception e) {
		            	//easel.drawRGB(111, 70, 150);
		            }
		        	
		            if (imageToShow != null) {
		            	//if (surfaceWidth > 0)
		            	//	imageToShow = Bitmap.createScaledBitmap(imageToShow, surfaceWidth, surfaceHeight, false);
		                
		            	//Log.d("onDraw", "surfaceWidth = " + surfaceWidth);
		            	//Log.d("onDraw", "imageToShow is not null");
		            	easel.drawBitmap(imageToShow, 0, 0, null);
		                //Log.d("onDraw", "Bitmap drawn");
		            }
	            }
	            
	            if (easel != null)
	            	surfaceHolder.unlockCanvasAndPost(easel);
			}
		}
    }
	
}

