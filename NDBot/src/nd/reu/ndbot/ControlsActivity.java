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
import java.util.Timer;
import java.util.TimerTask;

import android.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private boolean connected = false;
    private ArrayList <Boolean> sendData = new ArrayList<Boolean>();
   // private ArrayList <Boolean> connections = new ArrayList<Boolean>();
    private ArrayList <AsyncTask<Void, Void, Void> > connections = new ArrayList<AsyncTask<Void, Void, Void> >();
    private String frameToGet = "";
    ArrayAdapter<String> arrayAdapter; 
    AsyncThread currentBot;
    
    private boolean accel = false;
    private boolean button = true;
	// Buttons and Fields
	private EditText mIPAddress;
    private TextView wifiStatus;
	private ImageButton mForwardButton;
    private ImageButton mReverseButton;
    private ImageButton mTurnLeftButton;
    private ImageButton mTurnRightButton;
    private ImageButton mForwardLeftButton;
    private ImageButton mForwardRightButton;
    private ImageButton mStopButton;
    private Button mWifiButton;
    
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
		wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
        	toast("Enabling Wifi...");
        	wifiManager.setWifiEnabled(true);
        }
		
		wifiStatus = (TextView) findViewById(R.id.wifiConnected_id);
		
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
    	InetAddress serverAddr;
    	//String bacon = "Happy happy joy joy";
    	int state = 0;
		@Override
		protected Void doInBackground(Void... params) {			
			try {
                serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, 8080);
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
	                        frameToGet = input.readLine();
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
		public void setActive(boolean b){
			activeBot = b;
		}
		public String getServerAddr(){
			return serverAddr.toString();
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
	        	startActivityForResult(intent, 1);
	        	return true;
	        case R.id.routes:
	        	
	        }
	        return false;
	    }
	
	public void createButtons(){
		mForwardButton = (ImageButton) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                command = "-8.00,0.00";
                toast(command);
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
		/*if(getResources().getConfiguration().orientation == 1)
			setContentView(R.layout.controlsa);
		else
			setContentView(R.layout.controlsl);
		*/
	

	}
	protected AlertDialog onCreateDialog(int id){
		switch(id){
			case 0:
				LayoutInflater factory = LayoutInflater.from(this);
				final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
				return new AlertDialog.Builder(ControlsActivity.this)
				.setIcon(R.drawable.alert_dialog_icon)
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
                     	//sendData.add(new Boolean(false));
                     	if(connections.size() == 1)
                     		currentBot = blah;
                     		//((AsyncThread) connections.get(0)).setActive(true);
                     	blah.execute();
                     }

                 }
             })
             .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {

                     /* User clicked cancel so do nothing */
                 }
             })
             .create();
         
		case 1:
			LayoutInflater factory2 = LayoutInflater.from(this);
	         final View view = factory2.inflate(R.layout.alert_dialog_list, null);
	         //((ListView) listView).setAdapter(arrayAdapter);
	       //  TextView current = (TextView) view.findViewById(R.id.currentBot);
	        // for(int i = 0; i < connections.size(); i++)
	        	// if(connections)
	         if(currentBot != null)
	         {
	        	// current.setText(currentBot.getServerAddr());
	         }
	         arrayAdapter =  new ArrayAdapter<String>(this, R.layout.alert_dialog_list_text_view);
	         for(int i = 0; i <connections.size(); i++)
	         {
	        	 arrayAdapter.add((Integer.toString(i))+" "+((AsyncThread) connections.get(i)).getServerAddr());
	         }
	         ListView list = (ListView) view.findViewById(R.id.botListView);
	         
	         list.setAdapter(arrayAdapter);
	         AlertDialog.Builder builder = new AlertDialog.Builder(ControlsActivity.this);

	             builder.setIcon(R.drawable.alert_dialog_icon);
	             builder.setTitle("Connect to a robot");
	             builder.setView(view).setNegativeButton("Back", new DialogInterface.OnClickListener() {
	                 public void onClick(DialogInterface dialog, int whichButton) {

	                     /* User clicked cancel cancels automatically */
	                 }
	             });
	 	      final  AlertDialog alert = builder.create();
	         list.setOnItemClickListener(new OnItemClickListener() {
	        	 @Override
	        	 public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	        	  TextView tv = (TextView) arg1;
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
			parseRoute(data.getStringExtra("nd.reu.ndbot.PlanRouteActivity"));
		}
		
	}
	
	private void parseRoute(String routeData){
		routeData = routeData.substring(4);
		String[] route =routeData.split("\n");
		String[] time = new String[route.length];
		float [] routeTime = new float[route.length];
		int [] routeTimeInt = new int[route.length];
		for(int i = 0; i<route.length; i++)
		{
			int index = route[i].indexOf('~', 8);
			time[i] = route[i].substring(index+1);
			route[i] = route[i].substring(0,index);
			//Log.d("ClientActivity", route[i]);
			//Log.d("ClientActivity", time[i]);
			routeTime[i] = Float.parseFloat(time[i]);
			routeTime[i] *=1000;
			routeTimeInt[i] = (int) routeTime[i];
			//Log.d("ClientActivity", time[i]);
		}
		//sendRoute(route, routeTimeInt);
		//routeThread routeToTravel = (routeThread) new routeThread().execute(new GatherData(route, routeTimeInt));
		new routeThread().execute(new GatherData(route, routeTimeInt));
	}
	
	/*public void sendRoute(String[] route, int[] time){
		
	}
	*/
	class routeThread extends AsyncTask<GatherData, String,Void> {

		@Override
		protected Void doInBackground(GatherData... gatherData) {
			
			
			for(int i = 0; i<gatherData[0].route.length; i++)
			{
				publishProgress(gatherData[0].route[i]);
				try {
					Log.d("thread sleep time", Integer.toString(gatherData[0].time[i]));
					Thread.sleep(gatherData[0].time[i]);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			publishProgress("0.00,0.00");
			
			return null;
		}
		@Override
		protected void onProgressUpdate(String... route){
			Log.d("OnProgress", route[0]);
			command = route[0];
            if(currentBot != null)
            	currentBot.setSend(true);
		}
	
	
	
	
	
	}
	
	class GatherData{
		String[] route;
		int[] time;
		
		GatherData(String[] r, int[] t){
			route = r.clone();
			time = t.clone();
		}
	}
	
	/*class RunnableThread implements Runnable {

		Thread runner;
		String[] route;
		int[] time;
		public RunnableThread(String[] r, int[] t, String threadName) {
			route = r.clone();
			time = t.clone();
			runner = new Thread(this, threadName); // (1) Create a new thread.
			System.out.println(runner.getName());
			runner.start(); // (2) Start the thread.
		}
		public void run() {
			for(int i =0; i < route.length; i++)
			{
				
			}
		}
	}
	*/

	
}

