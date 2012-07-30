package nd.reu.ndbot;

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
		current = "1,0.00,0.00";
	}
	
	//collision detection
	AI(){
		current = "1,0.00,0.00";
		future = "1,0.00,0.00";
	}
	
	public void setFuture(String f){
		current = future;
		future = f;
	}
	
	public boolean isCollision(float first, float last){
		int[] commands;
		if(route == null)
		{
			commands = parseRoute(current, future);

			//if(command[0])
			switch(commands[0])
			{
				case 0:
					if(commands[3] == 1) //car shouldn't be moving
						return false;
					else if(commands[3] == 3 || commands[3] == 1 || commands[3] == 2)
					{
						if(last > first)
							return false;
						else
							return true;
					}
					else if(commands[3] == 8 || commands[3] == 7 || commands[3] == 6)
					{
						if(first > last)
							return false;
						else
							return true;
					}
					return false;
					default: 
						return false;
					
			}
		}
	
		
		
		return false;
		
	}
	private int[] parseRoute(String c, String f){
		int i = 2; //skip past direction int and ,
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
		routes[4] = Float.parseFloat(f.substring(2,index));
		routes[5] = Float.parseFloat(f.substring(index+1));
		
		for(i=2; i < routes.length; i++){
			route[i] = Math.round(routes[i]);
		}
		return route;
	}
	
	
	
}
