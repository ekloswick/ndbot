package nd.reu.ndbot;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PlanRouteActivity extends Activity {
	private ImageButton mForwardButton;
    private ImageButton mReverseButton;
    private ImageButton mTurnLeftButton;
    private ImageButton mTurnRightButton;
    private ImageButton mForwardLeftButton;
    private ImageButton mForwardRightButton;
    private ImageButton mStopButton;
    private Button mDoneButton;
    private Button remove;
	
    private EditText name;
    private EditText lengthText;
	private TextView listText;
	
	String command;
	String commandString;
	String nameOfRoute;
	String Route;
	@Override
	public	void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.plan_route);
		createView();
		Intent startIntent = getIntent();
		nameOfRoute = startIntent.getStringExtra("name");
		if(nameOfRoute != null)
		{
			SharedPreferences shared = getSharedPreferences("myPrefs",MODE_PRIVATE);
			SharedPreferences sharedString = getSharedPreferences("myPrefs2",MODE_PRIVATE);
			Route = sharedString.getString("route"+nameOfRoute, "oops");
			Log.d("Route", Route);
			commandString = shared.getString(nameOfRoute, "oops");
			Log.d("CommandString", commandString);
			name.setText(nameOfRoute);
			listText.setText(Route);
		}
	}
	public void createView(){
		mForwardButton = (ImageButton) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{	
            		if(commandString == null)
            		{
            			Route = "F"+s+" ";
            			listText.setText(Route);
            		}
            		else
            		{
            			Route = Route + "F"+s+" ";
            			listText.setText(Route);
            		}
            		commandString += "-8.00,0.00~"+s+'\n';
            	Log.d("ClientActivity", commandString);
            		lengthText.setText("");
            	
            			
            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        mReverseButton = (ImageButton) findViewById(R.id.reverseButton);
        mReverseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{	
            		if(commandString == null)
            		{
            			Route = "B"+s+" ";
            			listText.setText("B"+s+" ");
            		}
            		else
            		{
            			Route = Route+ "B"+s+" ";
            			listText.setText(Route);
            		}
            		commandString += "8.00,0.00~"+s+'\n';
            		lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        
        mTurnLeftButton = (ImageButton) findViewById(R.id.turnLeftButton);
        mTurnLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{
                	if(commandString == null)
            		{
                		Route = "L"+s+" ";
                		listText.setText("L"+s+" ");
            		}
            		else
            		{
            			Route = Route+"L"+s+" ";
            			listText.setText(Route);
            		}
            		commandString += "0.00,-8.00~"+s+'\n';
                	lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        
        mTurnRightButton = (ImageButton) findViewById(R.id.turnRightButton);
        mTurnRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{
                	if(listText == null)
            		{
                		Route = "R"+s+" ";
                		listText.setText("R"+s+" ");
            		}
            		else
            		{
            			Route = Route + "R"+s+" ";
            			listText.setText(Route);
            		}
            		commandString += "0.00,8.00~"+s+'\n';
                	lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        
        mForwardLeftButton = (ImageButton) findViewById(R.id.forwardLeftButton);
        mForwardLeftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{	
            		if(commandString == null)
            		{
            			Route = "FL"+s+" ";
            			listText.setText("FL"+s+" ");
            		}
            		else
            		{
            			Route += "FL"+s+" ";
            			listText.setText(Route);
            		}
            			
            		commandString += "-8.00,-8.00~"+s+'\n';
            		lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        
        mForwardRightButton = (ImageButton) findViewById(R.id.forwardRightButton);
        mForwardRightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{	
            		if(commandString == null)
            		{
            			Route = "FR"+s+" ";
            			listText.setText("FR"+s+" ");
            		}
            		else
            		{
            			Route += "FR"+s+" ";
            			listText.append("FR"+s+" ");
            		}
            		commandString += "-8.00,8.00~"+s+'\n';
            		lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");
            }
        });
        
        mStopButton = (ImageButton) findViewById(R.id.stopButton);
        mStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{
                	if(commandString == null)
                	{
                		Route = "S"+s+" ";
                		listText.setText("S"+s+" ");
                	}
            		else
            		{
            			Route += "S"+s+" ";
            			listText.append("S"+s+" ");
            		}
            		commandString += "0.00,0.00~"+s+'\n';
                	lengthText.setText("");

            	}
            	else
            		toast("Error: enter a length of time or amount of degrees");	
            }
        });
        
        lengthText = (EditText) findViewById(R.id.length);
        lengthText.setOnKeyListener(new EditText.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  // Perform action on key press
                	InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                	in.hideSoftInputFromWindow(lengthText
                            .getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                	lengthText.setText(lengthText.getText().toString().trim());
                }
                return false;
            }
        });
        name = (EditText) findViewById(R.id.nameText);
        listText = (TextView) findViewById(R.id.list);
        mDoneButton = (Button) findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		//String commandString = listText.getText().toString();
        		/*Intent intent=new Intent();
        		intent.putExtra("nd.reu.ndbot.PlanRouteActivity",commandString);
        		setResult(RESULT_OK, intent);
        	    finish();
        	    */
        		if(name ==null)
        			toast("Error: enter a name for the route");
        		else
        		{
        			SavePreferences(name.getText().toString().trim(),commandString,"route" + name.getText().toString(),Route);
        			//SavePreferences("route" + name.getText().toString(),Route);
        			//LoadPreferences();
        			finish();
        		}	
        	}
        });
        remove = (Button) findViewById(R.id.delete);
        remove.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//actual command
				String newString = commandString.substring(0, commandString.length()-1);
				int index = newString.lastIndexOf('\n');
				commandString = newString.substring(0, index+1);
				//readable version
				String newString2 = Route.substring(0, Route.length()-1);
				index = newString2.lastIndexOf(" ");
				Route = newString2.substring(0, index+1);
				listText.setText(Route);
			}
        	
        });
        
	}
	
	 private void SavePreferences(String key, String value, String key2, String value2){
         SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
         SharedPreferences sharedPreferences2 = getSharedPreferences("myPrefs2",MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         SharedPreferences.Editor editor2 = sharedPreferences2.edit();
         editor.putString(key, value);
         editor.commit();
         editor2.putString(key2, value2);
         editor2.commit();
        }
	 private void LoadPreferences(){
		 //   SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		    SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
		   // String strSavedMem1 = sharedPreferences.getString("MEM1", "");
		  //  String strSavedMem2 = sharedPreferences.getString("MEM2", "");
		   // textSavedMem1.setText(strSavedMem1);
		   // textSavedMem2.setText(strSavedMem2);
		    Map<String, ?> map = sharedPreferences.getAll();
		    Log.d("map_size_planRoute",Integer.toString(map.size()));
		    Log.d("load", sharedPreferences.getString("h", "o"));
		   }
	 
	
	public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
}
