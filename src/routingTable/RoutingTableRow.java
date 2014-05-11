package routingTable;

import java.io.Serializable;
import java.util.TimerTask;

import timer.CustomTimer;

public class RoutingTableRow implements Serializable, Cloneable {

	private static final long serialVersionUID = -8787444723896714884L;

	public int NextHopPortNumber;
	public int NextHopRouterId;
	public int LinkCost;
	public int DestRouterId;
	public int LearnedFrom;
	public transient CustomTimer TimeoutTimer;
	public transient CustomTimer DeletionTimer;

	/**
	 * Creates a routing table row which will be stored in an array contained in
	 * the RoutingTable class
	 * 
	 * @param nextHopPortNumber
	 *            port number of the neighbouring router that must be sent to in
	 *            order to reach the destination
	 * @param linkCost
	 *            total link cost to reach the destination
	 * @param destRouterId
	 *            router id of the destination router
	 * @param nextHopRouterId
	 *            router id of the neighbouring router that must be sent to in
	 *            order to reach the destination
	 * @param learnedFrom
	 *            router id of the neighbouring router that this route was
	 *            learned from
	 */
	public RoutingTableRow(int nextHopPortNumber, int nextHopRouterId,
			int linkCost, int destRouterId, int learnedFrom) {
		NextHopPortNumber = nextHopPortNumber;
		LinkCost = linkCost;
		DestRouterId = destRouterId;
		NextHopRouterId = nextHopRouterId;
		LearnedFrom = learnedFrom;
		InitializeAndResetRowTimeoutTimer();
	}

	public RoutingTableRow CloneRoutingTableRow() {
		try {
			return (RoutingTableRow) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void InitializeAndResetDeletionTimer() {
		try {
			DeletionTimer.resetTicks();
		} catch (Exception e) {
			DeletionTimer = new CustomTimer(new RowDeletionTask(), 30);
		}
		
	}

	public void InitializeAndResetRowTimeoutTimer() {
		try {
			TimeoutTimer.resetTicks();
		} catch (Exception e) {
			TimeoutTimer = new CustomTimer(new RowTimeoutTask(), 30);
		}

	}

	class RowTimeoutTask extends TimerTask {

		public void run() {
			LinkCost = 16;
			InitializeAndResetDeletionTimer();
		}
	}

	class RowDeletionTask extends TimerTask {

		public void run() {

		}
	}

}
