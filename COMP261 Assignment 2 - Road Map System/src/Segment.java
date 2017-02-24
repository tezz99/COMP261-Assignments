import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PriteshRPatel PC
 */

public class Segment {
    //Fields
    private int roadID;
    private double length; //Dont worry about length
    private int startNode;
    private int endNode;

    public List <Location> locations = new ArrayList<Location>();

    public Segment (int roadID, double length, int startNode, int endNode, List<Location> locations) {
	this.roadID = roadID;
	this.length = length;
	this.startNode = startNode;
	this.endNode = endNode;
	this.locations = locations;
    }

    public List getLocations() {
	return this.locations;
    }

    public int getRoadID() {
	return this.roadID;
    }

    public int getStartNodeID() {
	return this.startNode;
    }

    public int getEndNodeID() {
	return this.endNode;
    }

    public double getLength(){
	return length;
    }

}
