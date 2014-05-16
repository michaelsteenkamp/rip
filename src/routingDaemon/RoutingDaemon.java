package routingDaemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import fileParser.OutputPortInformation;
import routingTable.RoutingTable;
import routingTable.RoutingTableRow;
import timer.CustomTimer;

public class RoutingDaemon extends TimerTask {

	public Integer RouterId;
	public ArrayList<OutputPortInformation> OutputPorts = new ArrayList<OutputPortInformation>();
	public ArrayList<InputSocket> InputSockets = new ArrayList<InputSocket>();
	public DatagramSocket OutputSocket;
	public RoutingTable Table;
	public Timer UpdateTimer;
	public long updateIntervalMs = 10000;

	public RoutingDaemon(Integer routerId, ArrayList<Integer> inputPorts,
			ArrayList<OutputPortInformation> outputPorts) {
		RouterId = routerId;
		OutputPorts = outputPorts;
		Table = new RoutingTable(RouterId);
		Table.PopulateInitialRoutingTable(outputPorts);
		createInputSockets(inputPorts);
		createOutputSocket();
		createTimerAndSetInterval();
	}

	/**
	 * Creates a list of input sockets which the router will listen on
	 * 
	 * @param inputPorts
	 *            port numbers which the sockets will listen on
	 */
	private void createInputSockets(ArrayList<Integer> inputPorts) {
		for (int port : inputPorts) {
			InputSocket socket = new InputSocket(port);
			InputSockets.add(socket);
		}
	}

	/**
	 * Creates an output socket which will be used for sending DatagramPackets
	 */
	private void createOutputSocket() {
		try {
			OutputSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Error: Could not create output socket");
			System.exit(0);
		}
	}

	/**
	 * Creates the timer object for this class. Because this class extends
	 * TimerTask the run method will be called at the specified interval set by
	 * this method
	 */
	private void createTimerAndSetInterval() {
		UpdateTimer = new Timer();
		UpdateTimer.schedule(this, 0, updateIntervalMs);
	}

	@Override
	public void run() {
		sendRoutingTableToNeighbors();
	}

	/**
	 * Sends the current routing table to each neighbour (every output port) so
	 * that they can update their tables
	 */
	private void sendRoutingTableToNeighbors() {
		for (OutputPortInformation output : OutputPorts) {
			try {
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						arrayOutputStream);
				// Remove invalid rows which have been flagged for deletion
				removeRowsFlaggedForDeletion(Table);
				// Set metrics to infinity and add link cost
				RoutingTable tableToSend = Table.CloneRoutingTable();
				setMetricsToInfinity(tableToSend, output.getRouterId());
				addLinkCost(tableToSend, output.getLinkCost());
				tableToSend.setMyRouterId(RouterId);
				// Write new table into object output stream
				objectOutputStream.writeObject(tableToSend);
				objectOutputStream.flush();
				byte[] buffer = arrayOutputStream.toByteArray();
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length, InetAddress.getLocalHost(),
						output.getPortNumber());
				OutputSocket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Keep the row entry to ourself valid and with a cost of 0.
		for (RoutingTableRow current : Table.getRows()) {
			if (current.DestRouterId == Table.getMyRouterId()) {
				current.LinkCost = 0;
				current.InitializeAndResetRowTimeoutTimer();
			}
		}
		System.out.print(Table);
	}

	private void setMetricsToInfinity(RoutingTable input, int neighbor) {
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
	private void addLinkCost(RoutingTable input, int linkCost) {
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
	private void removeRowsFlaggedForDeletion(RoutingTable input) {
		Iterator<RoutingTableRow> rowIterator = input.getRows().iterator();

		while (rowIterator.hasNext()) {
			RoutingTableRow row = rowIterator.next();
			if (row.DeleteThisRow && row.DestRouterId != Table.getMyRouterId()) {
				rowIterator.remove();
				System.out.println(Table);
			}
		}
	}

	/**
	 * Processes and updates the current routing table based off the received
	 * routing table
	 */
	private synchronized void processIncomingRoutingTable(RoutingTable received) {

		ArrayList<RoutingTableRow> rowsToAddToCurrentTable = new ArrayList<RoutingTableRow>();

		for (RoutingTableRow receivedRow : received.getRows()) {
			boolean matched = false;
			// Must use an iterator in order to remove rows while iterating over
			// the current rows
			Iterator<RoutingTableRow> currentRowIterator = Table.getRows()
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
						receivedRow.NextHopRouterId = received.getMyRouterId();
						receivedRow.LearnedFrom = received.getMyRouterId();
						receivedRow.NextHopPortNumber = getOutputPortFromRouterId(received
								.getMyRouterId());
						receivedRow.InitializeAndResetRowTimeoutTimer();
						// We have received a valid entry for the current row
						// It is not cheaper so don't replace the current row
						// Just reinitialise the timeout timer.
					} else if (receivedRow.LinkCost < 16) {
						currentRow.InitializeAndResetRowTimeoutTimer();
						// We have learnt this row from our neighbouring router,
						// therefore we must
						// update its cost even if it is worse than our current
						// link cost
					} else if (currentRow.NextHopRouterId == received
							.getMyRouterId()) {
						currentRow.LinkCost = receivedRow.LinkCost;
					}
				}
				// A neighbouring router has come back online, reset the link
				// cost
				// of its row if it is still in the table
				if (received.getMyRouterId() == currentRow.DestRouterId
						&& currentRow.LinkCost == 16) {
					currentRow.InitializeAndResetRowTimeoutTimer();
					currentRow.LinkCost = getLinkCostFromNextHopRouterId(currentRow.NextHopRouterId);
				}
			}
			// We have received a new valid row, add it to our table
			if (!matched && receivedRow.LinkCost < 16) {
				rowsToAddToCurrentTable.add(receivedRow);
				receivedRow.NextHopRouterId = received.getMyRouterId();
				receivedRow.LearnedFrom = received.getMyRouterId();
				receivedRow.NextHopPortNumber = getOutputPortFromRouterId(received
						.getMyRouterId());
				receivedRow.InitializeAndResetRowTimeoutTimer();

			}
		}
		// New rows are added here as we can not add them while iterating over
		// the current routing table
		for (RoutingTableRow newRow : rowsToAddToCurrentTable) {
			Table.Rows.add(newRow);
		}
	}

	private int getOutputPortFromRouterId(int routerId) {
		for (OutputPortInformation output : OutputPorts) {
			if (output.getRouterId() == routerId) {
				return output.getPortNumber();
			}
		}
		return 0;
	}

	private int getLinkCostFromNextHopRouterId(int nextHopRouterId) {
		for (OutputPortInformation output : OutputPorts) {
			if (output.getRouterId() == nextHopRouterId) {
				return output.getLinkCost();
			}
		}
		return 0;
	}

	private void markRowsAsInvalid(int routerId) {
		for (RoutingTableRow row : Table.getRows()) {
			if (row.NextHopRouterId == routerId) {
				row.LinkCost = 16;
				row.InitializeAndResetDeletionTimer();
			}
		}
		//Send out a triggered update to our neighbours.
		sendRoutingTableToNeighbors();
	}

	/**
	 * The input socket class is used to listen for updated from neighbouring
	 * routers, each input socket runs on its own thread so that we can listen
	 * on multiple ports at once.
	 */
	class InputSocket implements Runnable {

		public DatagramSocket Socket;
		public int AssociatedRouterId = 0;
		public CustomTimer InvalidTimer;
		private int timeoutTicks = 20;

		public InputSocket(int port) {
			try {
				Socket = new DatagramSocket(port);
				createTimerAndSetInterval();
			} catch (SocketException e) {
				System.out.println("Error: Could not add port " + port
						+ " to InputSockets");
				e.printStackTrace();
			}
			Thread t = new Thread(this, Integer.toString(port));
			t.start();
		}

		@Override
		public void run() {
			while (true) {
				listenForUpdates();
			}
		}

		/**
		 * Creates the timer object for this class. Because this class extends
		 * TimerTask the run method will be called at the specified interval set
		 * by this method
		 */
		private void createTimerAndSetInterval() {
			InvalidTimer = new CustomTimer(new InvalidTask(), timeoutTicks);
		}

		/**
		 * Listens for updates from neighbouring routers. This method is
		 * blocking
		 */
		private void listenForUpdates() {
			byte[] buffer = new byte[1048576];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				Socket.receive(packet);
				InvalidTimer.resetTicks();
				ByteArrayInputStream byteArray = new ByteArrayInputStream(
						buffer);
				ObjectInputStream inputStream = new ObjectInputStream(byteArray);
				RoutingTable table = (RoutingTable) inputStream.readObject();
				AssociatedRouterId = table.getMyRouterId();
				processIncomingRoutingTable(table);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		class InvalidTask extends TimerTask {

			@Override
			public void run() {
				System.out.println("Invalid timer marking rows as invalid: "
						+ AssociatedRouterId);
				markRowsAsInvalid(AssociatedRouterId);
			}

		}

	}
}
