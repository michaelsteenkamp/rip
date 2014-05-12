package routingTable;

import java.util.ArrayList;

import fileParser.OutputPortInformation;

public class RoutingTableUpdater {
	/**
	 * Sets routing table metrics to infinity when these metrics were learned
	 * from the neighbour that this routing table is being sent to. This
	 * implements split horizon with poisoned reverse
	 * 
	 * @param input
	 *            The current routing table for this router
	 * @param neighbor
	 *            RouterId of the neighbouring router which the output table
	 *            will be sent to
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
	 * @param input
	 *            Input routing table
	 * @param linkCost
	 *            Cost to be added to each row
	 */
	public void AddLinkCost(RoutingTable input, int linkCost) {
		for (RoutingTableRow row : input.Rows) {
			row.LinkCost += linkCost;
		}
	}

	/**
	 * Processes and updates the current routing table based on the received
	 * routing table
	 */
	public synchronized void ProcessIncomingRoutingTable(RoutingTable current,
			RoutingTable received, int myRouterId,
			ArrayList<OutputPortInformation> myOutputPorts) {
		for (RoutingTableRow receivedRow : received.Rows) {
			boolean matched = false;

			for (RoutingTableRow currentRow : current.Rows) {
				if (receivedRow.DestRouterId == currentRow.DestRouterId) {
					// There is already an entry for this destination in the
					// current routing table
					matched = true;
					if (receivedRow.LinkCost <= currentRow.LinkCost) {
						// Replace current row with received
						currentRow = receivedRow;
						currentRow.NextHopRouterId = received.MyRouterId;
						currentRow.LearnedFrom = received.MyRouterId;
						currentRow.NextHopPortNumber = GetOutputPortFromRouterId(
								myOutputPorts, received.MyRouterId);
					}
				}
			}
			// This received row does not match any rows in the current routing
			// table
			if (!matched && receivedRow.DestRouterId != myRouterId) {
				receivedRow.NextHopRouterId = received.MyRouterId;
				receivedRow.LearnedFrom = received.MyRouterId;
				receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
						myOutputPorts, received.MyRouterId);
				receivedRow.InitializeAndResetRowTimeoutTimer();
				current.Rows.add(receivedRow);
			}
		}
	}

	public synchronized void ProcessIncomingRoutingTable2(RoutingTable current,
			RoutingTable received, int myRouterId,
			ArrayList<OutputPortInformation> myOutputPorts) {

		for (RoutingTableRow receivedRow : received.Rows) {
			boolean matched = false;

			for (RoutingTableRow currentRow : current.Rows) {
				if (receivedRow.DestRouterId == currentRow.DestRouterId) {
					matched = true;

					if (receivedRow.LinkCost < currentRow.LinkCost) {
						current.Rows.remove(currentRow);
						current.Rows.add(receivedRow);
						receivedRow.NextHopRouterId = received.MyRouterId;
						receivedRow.LearnedFrom = received.MyRouterId;
						receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
								myOutputPorts, received.MyRouterId);
						receivedRow.InitializeAndResetRowTimeoutTimer();
					}

					currentRow.InitializeAndResetRowTimeoutTimer();
				}

				if (received.MyRouterId == currentRow.NextHopRouterId
						&& currentRow.LinkCost == 16) {
					currentRow.InitializeAndResetRowTimeoutTimer();
					currentRow.LinkCost = GetLinkCostFromNextHopRouterId(
							myOutputPorts, currentRow.NextHopRouterId);
				}

				if(received.MyRouterId == currentRow.NextHopRouterId){
					currentRow.InitializeAndResetRowTimeoutTimer();
				}

			}

			if (!matched && receivedRow.DestRouterId != myRouterId) {
				current.Rows.add(receivedRow);
				receivedRow.NextHopRouterId = received.MyRouterId;
				receivedRow.LearnedFrom = received.MyRouterId;
				receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
						myOutputPorts, received.MyRouterId);
				receivedRow.InitializeAndResetRowTimeoutTimer();

			}

		}
	}

	private int GetOutputPortFromRouterId(
			ArrayList<OutputPortInformation> outputPorts, int routerId) {
		for (OutputPortInformation output : outputPorts) {
			if (output.RouterId == routerId) {
				return output.PortNumber;
			}
		}
		return 0;
	}

	private int GetLinkCostFromNextHopRouterId(
			ArrayList<OutputPortInformation> outputPorts, int nextHopRouterId) {
		for (OutputPortInformation output : outputPorts) {
			if (output.RouterId == nextHopRouterId) {
				return output.LinkCost;
			}
		}
		return 0;
	}

	public void MarkRowsAsInvalid(RoutingTable current, int routerId) {
		for (RoutingTableRow row : current.Rows) {
			if (row.NextHopRouterId == routerId) {
				row.LinkCost = 16;
				row.InitializeAndResetDeletionTimer();
			}
		}
	}
}
