package routingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	synchronized ArrayList<RoutingTableRow> getRows(){
		return Rows;
	}

	public void PopulateInitialRoutingTable(
			ArrayList<OutputPortInformation> outputPorts) {
		
		RoutingTableRow myself = new RoutingTableRow(0, 0, 0, MyRouterId, 0);
		Rows.add(myself);
		
		for (OutputPortInformation outputPort : outputPorts) {
			// Learned from is 0 as this row was learned from a configuration
			// file not another router
			RoutingTableRow row = new RoutingTableRow(outputPort.PortNumber,
					outputPort.RouterId, outputPort.LinkCost,
					outputPort.RouterId, 0);
			Rows.add(row);
		}
		
		
		
		System.out.print(this);
	}

	public RoutingTable CloneRoutingTable() {
		RoutingTable clone = new RoutingTable(MyRouterId);

		for (RoutingTableRow originalRow : getRows()) {
			clone.Rows.add(originalRow.CloneRoutingTableRow());
		}

		return clone;
	}
	
	public void sortRows(){
		Collections.sort(getRows(), new Comparator<RoutingTableRow>(){
			@Override
			public int compare(RoutingTableRow r1, RoutingTableRow r2){
				return r1.DestRouterId - r2.DestRouterId;
			}});
	}

	public String toString() {
		final Object[][] table = new String[Rows.size()][];
		int tableCount = 0;

		sortRows();
		for (RoutingTableRow row : getRows()){
			table[tableCount] = new String[] {
					Integer.toString(row.DestRouterId),
					Integer.toString(row.LinkCost),
					Integer.toString(row.NextHopRouterId),
					Integer.toString(row.NextHopPortNumber),
					Integer.toString(row.LearnedFrom),
					Integer.toString(row.TimeoutTimer.getTicks())};
			tableCount++;
		}

		String output = "DestRouterId - LinkCost - NextHopRouterId - NextHopPortNumber - LearnedFrom - TimerTicks\n";
		for (final Object[] row : table) {
			output += String.format("%15s%25s%25s%25s%35s%25s\n", row);
		}

		return output;
	}
}
