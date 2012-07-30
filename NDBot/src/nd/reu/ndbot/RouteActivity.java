package nd.reu.ndbot;

import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class RouteActivity extends Activity{
	Button Ok, Edit, Delete, Cancel;
	String routeChosen;
	String command;
	SharedPreferences sharedPreferences;
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.routes);
		createView();
	}
	public void createView(){
		 sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
		 final Map<String, ?> map = sharedPreferences.getAll();		
		 Set<String> set =  map.keySet();
		 String[] keys = new String[set.size()];
		 keys = (String[]) set.toArray(keys);	
		 ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_single_choice,keys); 
		 ListView routeList = (ListView) findViewById(R.id.routeListView);
		 routeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		 routeList.setAdapter(routeAdapter);
		 routeList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				CheckedTextView tv = (CheckedTextView) arg1;
				routeChosen = ((CheckedTextView) arg1).getText().toString();
				Log.d("itemSelected", routeChosen);
				command = (String) map.get(routeChosen);
				tv.setChecked(true);
			}
			 
		 });
		 Ok = (Button) findViewById(R.id.ok);
		 Ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(routeChosen ==null)
					return;
				else
				{
	        		Intent intent=new Intent();
	        		intent.putExtra("nd.reu.ndbot.RouteActivity",command);
	        		setResult(RESULT_OK, intent);
	        	    finish();
				}
				
			}
			 
		 });
		 Edit = (Button) findViewById(R.id.edit);
		 Edit.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 if(routeChosen ==null)
					 return;
				 else
				 {
					 Intent intent = new Intent(arg0.getContext(), PlanRouteActivity.class);
					 intent.putExtra("name", routeChosen);
					 startActivity(intent);
					 finish();
				 }
			 }
		 });
		 Delete = (Button) findViewById(R.id.delete);
		 Delete.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 if(routeChosen ==null)
					 return;
				 else
				 {
					 SharedPreferences.Editor editor = sharedPreferences.edit();
					 editor.remove(routeChosen);
					 editor.remove("route"+routeChosen);
					 editor.commit();
					 finish();
				 }
			 }
		 });
		 Cancel = (Button) findViewById(R.id.cancel);
		 Cancel.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View arg0){
				 finish();
			 }
		 });
		 
	}
	
	
	
	
	
	

}
