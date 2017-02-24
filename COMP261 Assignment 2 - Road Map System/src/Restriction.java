
public class Restriction {
    private int fromNodeID;
    private int fromRoadID;
    private int effectedNodeID;
    private int toRoadID;
    private int toNodeID;

    public  Restriction (int fromNodeID, int fromRoadID, int effectedNodeID, int toRoadID, int toNodeID) {
	this.setFromNodeID(fromNodeID);
	this.setFromRoadID(fromRoadID);
	this.setEffectedNodeID(effectedNodeID);
	this.setToRoadID(toRoadID);
	this.setToNodeID(toNodeID);
    }

    public int getFromNodeID() {
	return fromNodeID;
    }

    public void setFromNodeID(int fromNodeID) {
	this.fromNodeID = fromNodeID;
    }

    public int getFromRoadID() {
	return fromRoadID;
    }

    public void setFromRoadID(int fromRoadID) {
	this.fromRoadID = fromRoadID;
    }

    public int getEffectedNodeID() {
	return effectedNodeID;
    }

    public void setEffectedNodeID(int effectedNodeID) {
	this.effectedNodeID = effectedNodeID;
    }

    public int getToNodeID() {
	return toNodeID;
    }

    public void setToNodeID(int toNodeID) {
	this.toNodeID = toNodeID;
    }

    public int getToRoadID() {
	return toRoadID;
    }

    public void setToRoadID(int toRoadID) {
	this.toRoadID = toRoadID;
    }


}
