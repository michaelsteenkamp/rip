package routingTable;

import java.util.ArrayList;
import java.io.Serializable;

import fileParser.OutputPortInformation;

public class RoutingTable implements Serializable {
	private static final long serialVersionUID = 4064137834678840042L;
	public ArrayList<RoutingTableRow> Rows;
	public int MyRouterId;
	
	
	public RoutingTable(int id) {
		MyRouterId = id;
		Rows = new ArrayList<RoutingTableRow>();
	}
	
	public void PopulateInitialRoutingTable(ArrayList<OutputPortInformation> outputPorts){
		for(OutputPortInformation outputPort : outputPorts){
			// Learned from is 0 as this row was learned from a configuration file not another router
			RoutingTableRow row = new RoutingTableRow(
					outputPort.PortNumber,
					outputPort.RouterId,
					outputPort.LinkCost,
					outputPort.RouterId,
					0);
			Rows.add(row);
		}
		System.out.print(this);
	}
	
	public RoutingTable CloneRoutingTable(){
		RoutingTable clone = new RoutingTable(MyRouterId);
	
		for(RoutingTableRow originalRow: Rows){
			clone.Rows.add(originalRow.CloneRoutingTableRow());
		}
		
		return clone;
	}
	
	public String toString(){
		final Object[][] table = new String[Rows.size()][];
		
		int tableCount = 0;
		for(RoutingTableRow row : Rows){
			table[tableCount] = new String[] { 
					Integer.toString(row.DestRouterId), 
					Integer.toString(row.LinkCost), 
					Integer.toString(row.NextHopRouterId), 
					Integer.toString(row.NextHopPortNumber),
					Integer.toString(row.LearnedFrom),
					Boolean.toString(row.IsValid),
					Integer.toString(row.RowTimer.getTicks())
			};
			tableCount++;
		}
		
		String output = "DestRouterId  LinkCost  NextHopRouterId  NextHopPortNumber  LearnedFrom  IsValid  RowTimerTicks\n";
		for (final Object[] row : table) {
		    output += String.format("%5s%15s%15s%15s%15s%15s%15s\n", row);
		}
		
		return output;
	}
}
