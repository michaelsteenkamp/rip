package routingTable;

public class RoutingTableUpdater {
	/**
	 * Sets routing table metrics to infinity when these metrics were learned
	 * from the neighbor that this routing table is being sent to. This
	 * implements split horizon with poisoned reverse
	 * 
	 * @param input The current routing table for this router
	 * @param neighbor RouterId of the neighboring router which the output table will be sent to
	 */
	public void SetMetricsToInfinity(RoutingTable input, int neighbor) {
		for (RoutingTableRow row : input.Rows) {
			RoutingTableRow currentRow = row;
			if (currentRow.LearnedFrom == neighbor) {
				currentRow.LinkCost = 16;
			}
		}
	}

	/**
	 * 
	 * @param input Input routing table
	 * @param linkCost Cost to be added to each row
	 */
	public void AddLinkCost(RoutingTable input, int linkCost) {
		for (RoutingTableRow row : input.Rows) {
			row.LinkCost += linkCost;
		}
	}

	/**
	 * Processes and updates the current routing table based on the received
	 * routing table
	 * @param current Current routing table
	 * @param received Received routing table
	 * @return the new routing table
	 */
	public void ProcessIncomingRoutingTable(RoutingTable current, RoutingTable received, int myRouterId) {
		for (RoutingTableRow receivedRow : received.Rows) {
			boolean matched = false;
			for (RoutingTableRow currentRow : current.Rows) {
				if (receivedRow.HasSameDestination(currentRow)) {
					// There is already an entry for this row in the current routing table
					matched = true;
					if (receivedRow.LinkCost < currentRow.LinkCost) {
						// Replace current row with received as it has a lower
						// link cost
						currentRow = receivedRow;
						currentRow.LearnedFrom = received.MyRouterId;
					}
				}
			}
			// This received row does not match any rows in the current routing
			// table
			if (!matched && receivedRow.DestRouterId != myRouterId) {
				receivedRow.LearnedFrom = received.MyRouterId;
				current.Rows.add(receivedRow);
			}
		}
	}
	
	public void MarkRowsAsInvalid(RoutingTable current, int routerId){
		
	}
}
