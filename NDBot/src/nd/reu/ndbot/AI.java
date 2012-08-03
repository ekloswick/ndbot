package nd.reu.ndbot;

import android.util.Log;

public class AI {
	private String[] route;
	private int[] time;
	private String current;
	private String future;
	
	//preplanned
	AI(String[] r, int[] t, String f){
		route = r;
		time = t;
		future = f;
		current = "0,0.00,0.00";
	}
	
	//collision detection
	AI(){
		current = "0,0.00,0.00";
		future = "0,0.00,0.00";
	}
	
	public void setFuture(String f){
		current = future;
		future = f;
		Log.d("new command", future);
	}
	
	public boolean isCollision(float first, float last){
		int command;
		//if(route == null)
		//{
		Log.d("in AI collision", "hi");
			command = parseRoute(current, future);

			//if(command[0])
			Log.d("command[3]", Integer.toString(command));
					if(command == 0) //car shouldn't be moving
					{
						Log.d("AI", "stopped");
						return false;
						
					}
					else if(command == 3 || command == 1 || command == 2) //forward
					{
						Log.d("in AI forward", "forward");
						if(last > first)
						{
							Log.d("f","false");
							return false;
						}
						else
						{
							Log.d("t","true");
							return true;
						}

					}
					else if(command == 8 || command == 7 || command == 6) //backwards
					{
						Log.d("in AI collision", "reverse");
						if(first > last)
							return false;
						else
							return true;
					}
					return false;
					
			//}
	
		
		
	//	return false;
		
	}
	private int parseRoute(String c, String f){
	/*	int i = 2; //skip past direction int and ,
		int index = 0;
		char[] ch = c.toCharArray();
		float[] routes= new float[6];
		int[] route = new int[6];
		
		for(; i < ch.length; i++)
		{
			if(ch[i] == ',')
			{
				index = i;
				break;
			}
		}
		Log.d("current",c);
		Log.d("future",f);
		routes[0]= ch[0];
		routes[1] = Float.parseFloat(c.substring(2,index));
		routes[2] = Float.parseFloat(c.substring(index+1));
		
		char[] fch = f.toCharArray();
		for(i = 2; i < fch.length; i++)
		{
			if(fch[i] == ',')
			{
				index = i;
				break;
			}
		}
		routes[3] = fch[0];
		//Log.d("parsing routes[3]",Float.toString(routes[3]));
		routes[4] = Float.parseFloat(f.substring(2,index));
		routes[5] = Float.parseFloat(f.substring(index+1));
		
		for(i=2; i < routes.length; i++){
			route[i] = Math.round(routes[i]);
		}
		Log.d("route direction",Integer.toString(route[3]));*/
		
		String dir = f.substring(0,1);
		
		
		
		
		return Integer.parseInt(dir);
	}
	public String[] reRoute(int index){//
		String[] newRoute = new String[route.length-index+4];
		if(future.substring(0,1).equals("7"))// was in reverse
		{
			//forward, turn right, reverse, turn left
			newRoute[0] = "-8.00,0.00";
			newRoute[1] = "0.00,8.00";
			newRoute[2] = "8.00,0.00";
			newRoute[3] = "0.00,-8.00";
		}
		else //moving forward/forward right or left
		{
			//reverse, turn right, forward, turn left
			newRoute[0] = "8.00,0.00";
			newRoute[1] = "0.00,8.00";
			newRoute[2] = "-8.00,0.00";
			newRoute[3] = "0.00,-8.00";
		}
		for(int i = index; i < route.length; i++)
		{
			newRoute[i-index+4] = route[index];
		}
		//correct for original turn at end?
		return newRoute;
	}
	
	public int[] reTime(int index, int timeInCurrent)
	{
		int[] newTime = new int[time.length-index+4];
		int timeRemaining = time[index]-timeInCurrent;
		newTime[0] = 1000;
		newTime[1] = 500;
		newTime[2] = 1400;
		newTime[3] = newTime[1];
		newTime[4] = timeRemaining;
		for(int i = index+1; i < time.length; i++)
		{
			newTime[i-index+4] = time[index];
		}
		
		
		return newTime;
	}
	
	
}
