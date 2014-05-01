package timer;

import java.util.Timer;
import java.util.TimerTask;

import routingTable.RoutingTableRow;

public class RowTimer extends TimerTask {
	public Timer UtilTimer;
	public RoutingTableRow MyRow;
	private int Ticks;
	public final int Speed = 1000;
	public final int Delay = 0; 
	
	public RowTimer(RoutingTableRow myRow){
		MyRow = myRow;
		Ticks = 0;
		UtilTimer = new Timer();
		UtilTimer.scheduleAtFixedRate(this, Delay, Speed);
	}

	@Override
	public void run() {
		Ticks++;
		if(Ticks > 15){
			MyRow.IsValid = false;
		}
	}
	
	public int getTicks(){
		return Ticks;
	}
	
	public void resetTicks(){
		Ticks = 0;
	}
	
}
