package routingTable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fileParser.OutputPortInformation;

public class RoutingTable implements Serializable {
	private static final long serialVersionUID = 4064137834678840042L;
	public ArrayList<RoutingTableRow> Rows;
	public int MyRouterId;

	public RoutingTable(int id) {
		MyRouterId = id;
		Rows = new ArrayList<RoutingTableRow>();
	}
	
	public synchronized ArrayList<RoutingTableRow> getRows(){
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
					new DecimalFormat("00").format(row.LinkCost),
					Integer.toString(row.NextHopRouterId),
					new DecimalFormat("0000").format(row.NextHopPortNumber),
					Integer.toString(row.LearnedFrom),
					Integer.toString(row.TimeoutTimer.getTicks())};
			tableCount++;
		}

		String output = "DestId - LinkCost - NextHopId - NextHopPort - LearnedFrom - Ticks\n";
		for (final Object[] row : table) {
			output += String.format("%5s%15s%20s%20s%20s%25s\n", row);
		}

		return output;
	}
}
