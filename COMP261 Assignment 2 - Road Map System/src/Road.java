
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PriteshRPatel PC
 */

public class Road {
    private int roadID;
    private String roadName;

    private int type; //Dont need this?
    private String city; //dont need this?

    private int directions; //If 0, both directions allowed. If 1, only oneway.
    private int speed; //Speeds depend on value. Refer to readme.txt
    private int roadClass; //Class of the road. Roads of higher class are considered to be faster or atleast better. Refer to readme.txt

    //If 0, then okay for this category of traffic. If 1, unusable for this category of traffic.
    private int car; 
    private int pedestrians;
    private int bicycle;

    public List<Segment> segments = new ArrayList<Segment>(); //Segments that make up a road



    public Road (int roadID, int type, String roadName, String city, int directions, int speed, int roadClass, int car, int pedestrians, int bicycle){
	this.roadID = roadID;
	this.type = type;
	this.roadName = roadName;
	this.city = city;
	this.directions = directions;
	this.setSpeed(speed);
	this.setRoadClass(roadClass);
	this.car = car;
	this.pedestrians = pedestrians;
	this.bicycle = bicycle;

	//System.out.println(this.roadID + " " + this.type+ " " + this.roadName+ " " + this.city+ " " + this.directions+ " " + this.speed+ " " + this.roadClass+ " " + this.car+ " " + this.pedestrians+ " " + this.bicycle );
    }

    public void addSegment (Segment segment) {
	this.segments.add(segment);
    }

    public int getDirections() {
	return this.directions;
    }

    public List getSegments() {
	return this.segments;
    }

    public int getRoadID(){
	return this.roadID;
    }

    public String getRoadName() {
	return this.roadName;
    }

    public String getCity(){
	return this.city;
    }

    public int getRoadClass() {
	return roadClass;
    }

    public void setRoadClass(int roadClass) {
	this.roadClass = roadClass;
    }

    public int getSpeed() {
	return speed;
    }

    public void setSpeed(int speed) {
	this.speed = speed;
    }

}
