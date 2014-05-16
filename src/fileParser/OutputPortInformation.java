package fileParser;

public class OutputPortInformation {

    private int PortNumber;
    private int LinkCost;
    private int RouterId;

    public OutputPortInformation(int portNumber, int linkCost, int routerId) {
        setPortNumber(portNumber);
        setLinkCost(linkCost);
        setRouterId(routerId);
    }

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return PortNumber;
    }

    /**
     * @param portNumber
     *            the portNumber to set
     */
    public void setPortNumber(int portNumber) {
        PortNumber = portNumber;
    }

    /**
     * @return the linkCost
     */
    public int getLinkCost() {
        return LinkCost;
    }

    /**
     * @param linkCost
     *            the linkCost to set
     */
    public void setLinkCost(int linkCost) {
        LinkCost = linkCost;
    }

    /**
     * @return the routerId
     */
    public int getRouterId() {
        return RouterId;
    }

    /**
     * @param routerId
     *            the routerId to set
     */
    public void setRouterId(int routerId) {
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
