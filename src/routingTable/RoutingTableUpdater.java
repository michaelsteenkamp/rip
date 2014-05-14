package routingTable;

import java.util.ArrayList;
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
		for (RoutingTableRow row : input.getRows()) {
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
		for (RoutingTableRow row : input.getRows()) {
			row.LinkCost += linkCost;
		}
	}

	/**
	 * Remove any rows flagged for deletion.
	 * 
	 * @param input
	 *            The input routing table from which rows will be removed
	 */
	public void RemoveRowsFlaggedForDeletion(RoutingTable input) {
		Iterator<RoutingTableRow> rowIterator = input.getRows().iterator();

		while (rowIterator.hasNext()) {
			RoutingTableRow row = rowIterator.next();
			if (row.DeleteThisRow) {
				System.out.println("ROW REMOVED: " + row.DestRouterId);
				rowIterator.remove();
			}
		}

	}

	/**
	 * Processes and updates the current routing table based off the received
	 * routing table
	 * 
	 * @param current
	 *            The current routing table
	 * @param received
	 *            The received routing table
	 * @param myOutputPorts
	 */
	public synchronized void ProcessIncomingRoutingTable(RoutingTable current,
			RoutingTable received,
			ArrayList<OutputPortInformation> myOutputPorts) {

		ArrayList<RoutingTableRow> rowsToAddToCurrentTable = new ArrayList<RoutingTableRow>();

		for (RoutingTableRow receivedRow : received.getRows()) {
			boolean matched = false;
			// Must use an iterator in order to remove rows while iterating over
			// the current rows
			Iterator<RoutingTableRow> currentRowIterator = current.getRows()
					.iterator();

			while (currentRowIterator.hasNext()) {
				RoutingTableRow currentRow = currentRowIterator.next();

				if (receivedRow.DestRouterId == currentRow.DestRouterId) {
					matched = true;

					if (receivedRow.LinkCost < currentRow.LinkCost
							&& currentRow.LinkCost != 16) {
						// Replace current row with received row
						// current.Rows.remove(currentRow);
						currentRowIterator.remove();

						rowsToAddToCurrentTable.add(receivedRow);
						receivedRow.NextHopRouterId = received.MyRouterId;
						receivedRow.LearnedFrom = received.MyRouterId;
						receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
								myOutputPorts, received.MyRouterId);
						receivedRow.InitializeAndResetRowTimeoutTimer();
					} else if (receivedRow.LinkCost < 16) {
						// We have received a valid entry for the current row
						// It is not cheaper so don't replace the current row
						// Just reinitialise the timeout timer.
						currentRow.InitializeAndResetRowTimeoutTimer();
						// We have learnt this row from our neighbouring router,
						// therefore we must
						// update its cost even if it is worse than our current
						// link cost
					} else if (currentRow.NextHopRouterId == received.MyRouterId) {
						currentRow.LinkCost = receivedRow.LinkCost;
					}
				}
				// A neighbouring router has come back online, reset the link
				// cost
				// of its row if it is still in the table
				if (received.MyRouterId == currentRow.DestRouterId
						&& currentRow.LinkCost == 16) {
					currentRow.InitializeAndResetRowTimeoutTimer();
					currentRow.LinkCost = GetLinkCostFromNextHopRouterId(
							myOutputPorts, currentRow.NextHopRouterId);
				}

			}
			// We have received a new valid row, add it to our table
			if (!matched && receivedRow.LinkCost < 16) {
				rowsToAddToCurrentTable.add(receivedRow);
				receivedRow.NextHopRouterId = received.MyRouterId;
				receivedRow.LearnedFrom = received.MyRouterId;
				receivedRow.NextHopPortNumber = GetOutputPortFromRouterId(
						myOutputPorts, received.MyRouterId);
				receivedRow.InitializeAndResetRowTimeoutTimer();

			}
		}
		// New rows are added here as we can not add them while iterating over
		// the current routing table
		for (RoutingTableRow newRow : rowsToAddToCurrentTable) {
			current.Rows.add(newRow);
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
		for (RoutingTableRow row : current.getRows()) {
			if (row.NextHopRouterId == routerId) {
				row.LinkCost = 16;
				row.InitializeAndResetDeletionTimer();
			}
		}
	}
}
