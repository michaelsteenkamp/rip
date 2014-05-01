package routingTable;

import java.io.Serializable;
import timer.RowTimer;


public class RoutingTableRow implements Serializable, Cloneable{
	
	private static final long serialVersionUID = -8787444723896714884L;
	
	public int NextHopPortNumber;
	public int NextHopRouterId;
	public int LinkCost;
	public int DestRouterId;
	public int LearnedFrom;
	public boolean IsValid;
	public transient RowTimer RowTimer;

	/**
	 * Creates a routing table row which will be stored in an array contained in the RoutingTable class
	 * @param nextHopPortNumber port number of the neighboring router that must be sent to in order to reach the destination
	 * @param linkCost total link cost to reach the destination
	 * @param destRouterId router id of the destination router
	 * @param nextHopRouterId router id of the neighboring router that must be sent to in order to reach the destination
	 * @param learnedFrom router id of the neighboring router that this route was learned from
	 */
	public RoutingTableRow(int nextHopPortNumber, int nextHopRouterId, int linkCost, int destRouterId, int learnedFrom){
		NextHopPortNumber = nextHopPortNumber;
		LinkCost = linkCost;
		DestRouterId = destRouterId;
		NextHopRouterId = nextHopRouterId;
		LearnedFrom = learnedFrom;
		IsValid = true;
		RowTimer = new RowTimer(this);
	}
	
	public boolean HasSameDestination(RoutingTableRow other){
		if(DestRouterId == other.DestRouterId){
			return true;
		}
		return false;
	}
	
	public void InitializeRowTimer(){
		RowTimer = new RowTimer(this);
	}
	
	public RoutingTableRow CloneRoutingTableRow(){
		try {
			return (RoutingTableRow) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
