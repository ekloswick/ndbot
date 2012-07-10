package nd.reu.ndbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	
    private EditText lengthText;
	private TextView listText;
	
	String command;
	String commandString;
	@Override
	public	void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.plan_route);
		createView();
		
	}
	public void createView(){
		mForwardButton = (ImageButton) findViewById(R.id.forwardButton);
        mForwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	String s = lengthText.getText().toString();
            	if(s != null)
            	{	commandString += "-8.00,0.00~"+s+'\n';
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
            	{	commandString += "8.00,0.00~"+s+'\n';
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
            	{	commandString += "-8.00,-8.00~"+s+'\n';
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
            	{	commandString += "-8.00,8.00~"+s+'\n';
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
                }
                return false;
            }
        });

        listText = (TextView) findViewById(R.id.list);
        mDoneButton = (Button) findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		//String commandString = listText.getText().toString();
        		Intent intent=new Intent();
        		intent.putExtra("nd.reu.ndbot.PlanRouteActivity",commandString);
        		setResult(RESULT_OK, intent);
        	    finish();
        	}
        });
	}
	
	public void toast(String text) {
    	Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
}
