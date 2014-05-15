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
import java.util.Timer;
import java.util.TimerTask;

import fileParser.OutputPortInformation;
import routingTable.RoutingTable;
import routingTable.RoutingTableUpdater;
import timer.CustomTimer;

public class RoutingDaemon extends TimerTask {

	public Integer RouterId;
	public ArrayList<OutputPortInformation> OutputPorts = new ArrayList<OutputPortInformation>();
	public ArrayList<InputSocket> InputSockets = new ArrayList<InputSocket>();
	public DatagramSocket OutputSocket;
	public RoutingTable Table;
	public RoutingTableUpdater TableUpdater = new RoutingTableUpdater();
	public Timer UpdateTimer;
	private long updateIntervalMs = 10000;

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
				TableUpdater.RemoveRowsFlaggedForDeletion(Table);
				// Set metrics to infinity and add link cost
				RoutingTable tableToSend = Table.CloneRoutingTable();
				TableUpdater.SetMetricsToInfinity(tableToSend, output.RouterId);
				TableUpdater.AddLinkCost(tableToSend, output.LinkCost);
				tableToSend.MyRouterId = RouterId;
				// Write new table into object output stream
				objectOutputStream.writeObject(tableToSend);
				objectOutputStream.flush();
				byte[] buffer = arrayOutputStream.toByteArray();
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length, InetAddress.getLocalHost(),
						output.PortNumber);
				OutputSocket.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param received
	 *            The received routing table
	 * @param portNumber
	 *            The port on which the routing table has been received
	 */
	private void updateRoutingTable(RoutingTable received) {
		TableUpdater.ProcessIncomingRoutingTable(Table, received, OutputPorts);
		System.out.print(Table);
	}

	/**
	 * 
	 * @param routerId
	 *            The router id of our neighbour which has timed out
	 */
	private void markRowsAsInvalid(int routerId) {
		TableUpdater.MarkRowsAsInvalid(Table, routerId);
		sendRoutingTableToNeighbors();
		System.out.print(Table);
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
				AssociatedRouterId = table.MyRouterId;
				RoutingDaemon.this.updateRoutingTable(table);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		class InvalidTask extends TimerTask {

			@Override
			public void run() {
				System.out.println("Invalid timer marking rows as invalid: "
						+ AssociatedRouterId);
				RoutingDaemon.this.markRowsAsInvalid(AssociatedRouterId);
			}

		}

	}
}
