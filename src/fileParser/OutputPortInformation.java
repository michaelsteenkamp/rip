package fileParser;

public class OutputPortInformation {

	public int PortNumber;
	public int LinkCost;
	public int RouterId;

	public OutputPortInformation(int portNumber, int linkCost, int routerId) {
		PortNumber = portNumber;
		LinkCost = linkCost;
		RouterId = routerId;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(PortNumber + "-");
		sb.append(LinkCost + "-");
		sb.append(RouterId);
		return sb.toString();
	}
}
