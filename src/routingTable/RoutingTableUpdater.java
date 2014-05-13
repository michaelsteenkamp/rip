package routingTable;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

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

	public void RemoveRowsFlaggedForDeletion(RoutingTable input) {
		Iterator<RoutingTableRow> rowIterator = input.Rows.iterator();

		while (rowIterator.hasNext()) {
			RoutingTableRow row = rowIterator.next();
			if (row.DeleteThisRow) {
				System.out.println("ROW REMOVED: " + row.DestRouterId);
				rowIterator.remove();
			}
		}

	}

	public synchronized void ProcessIncomingRoutingTable2(RoutingTable current,
			RoutingTable received, int myRouterId,
			ArrayList<OutputPortInformation> myOutputPorts) {

		for (RoutingTableRow receivedRow : received.Rows) {
			boolean matched = false;
			try {
				for (RoutingTableRow currentRow : current.Rows) {

					if (receivedRow.DestRouterId == currentRow.DestRouterId) {
						matched = true;

						if (receivedRow.LinkCost < currentRow.LinkCost) {
							// Replace current row with received row
							current.Rows.remove(currentRow);

							current.Rows.add(receivedRow);
							receivedRow.NextHopRouterId = received.MyRouterId;
							receivedRow.LearnedFrom = received.MyRouterId;
							receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
									myOutputPorts, received.MyRouterId);
							receivedRow.InitializeAndResetRowTimeoutTimer();
						} else {
							currentRow.InitializeAndResetRowTimeoutTimer();
						}
					}

					if (received.MyRouterId == currentRow.DestRouterId
							&& currentRow.LinkCost == 16) {
						currentRow.InitializeAndResetRowTimeoutTimer();
						currentRow.LinkCost = GetLinkCostFromNextHopRouterId(
								myOutputPorts, currentRow.NextHopRouterId);
					}
				}
			} catch (ConcurrentModificationException e) {

			}

			if (!matched && receivedRow.DestRouterId != myRouterId
					&& receivedRow.LinkCost < 16) {
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
