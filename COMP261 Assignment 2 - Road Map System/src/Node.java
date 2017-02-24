import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author PriteshRPatel PC
 */

public class Node implements Comparable<Node>{

    //Fields
    private int nodeID;
    private Location location;
    private double lat;
    private double lon;

    //For AStar Search
    private boolean visited = false;
    private Node fromNode;
    private double totalCostToHere;
    private double estimateToGoal;

    private double totalTimeCostToHere;

    //For ArticulationPoints
    private boolean visitedAP = false;
    private Node fromNodeAP;
    private int depth = Integer.MAX_VALUE;
    private int reachback;
    private Queue<Node> children;


    public List<Segment> edgesIn = new ArrayList<Segment>();
    public List<Segment> edgesOut = new ArrayList<Segment>();

    public HashSet<Integer> neighbourNodeIDs = new HashSet<Integer>();


    public Node(int nodeID, double lat, double lon) {
	this.nodeID = nodeID;
	this.lat = lat;
	this.lon = lon;
	this.location = this.location.newFromLatLon(this.lat, this.lon); 

    }


    ////GENERAL NODE METHODS
    public int getNodeID() {
	return this.nodeID;
    }

    public Location getLocation() {
	return this.location;
    }

    public double getLat(){
	return this.lat;
    }

    public double getLon(){
	return this.lon;
    }

    public void addEdgeOut(Segment segment){
	this.edgesOut.add(segment);
    }

    public List<Segment> getEdgesOut() {
	return this.edgesOut;
    }

    public void addEdgeIn(Segment s) {
	this.edgesIn.add(s);
    }

    public List<Segment> getEdgesIn() {
	return this.edgesIn;
    }

    public int compareTo(Node nodeTwo) {
	if (this.estimateToGoal < nodeTwo.getEstimateToGoal()) {
	    return -1;
	}else if (this.estimateToGoal == nodeTwo.getEstimateToGoal()) {
	    return 0;
	} else {
	    return 1;
	}
    }



    ////BELOW Methods for A* Search
    public void resetForSearch(){
	this.visited = false;
	this.fromNode = null;
	this.totalCostToHere = 0;
	this.estimateToGoal = 0;
    }

    public double getEstimateToGoal(){
	return this.estimateToGoal;
    }

    public void setTotalCostToHere(double totalCostToHere) {
	this.totalCostToHere = totalCostToHere;
    }

    public void setFromNode(Node fromNode){
	this.fromNode = fromNode;
    }

    public Node getFromNode(){
	return this.fromNode;
    }

    public double getTotalCostToHere() {
	return this.totalCostToHere;
    }

    public void setEstimateToGoal (double estimate) {
	this.estimateToGoal = estimate;
    }

    public boolean isVisited() {
	return visited;
    }

    public void setVisited(boolean visited) {
	this.visited = visited;
    }



    ////Below Methods for Articulation Points
    public void resetForAP() {
	this.depth = Integer.MAX_VALUE;
	this.visitedAP = false;
	this.fromNodeAP = null;
	this.reachback = Integer.MAX_VALUE; ///check if can be 0 ********************
	this.children = null; //Check if this is correct
    }

    /*Returns adjacent node IDs*/
    public HashSet<Integer> getAdjacentNodeIDs() {
	return this.neighbourNodeIDs;
    }

    public void addNighbourNodeID(int ID){
	this.neighbourNodeIDs.add(ID);
    }


    public boolean getVisitedAP() {
	return this.visitedAP;
    }

    public Node getFromNodeAP() {
	return this.fromNodeAP;
    }

    public void setVisitedAP(boolean b) {
	this.visitedAP = b;
    }

    public void setFromNodeAP(Node from) {
	this.fromNodeAP = from;
    }

    public void setDepth(int d) {
	this.depth = d;
    }

    public int getDepth() {
	return this.depth;
    }

    public int getReachback() {
	return this.reachback;
    }

    public void setReachback(int i){
	this.reachback = i;
    }

    public Queue<Node> getChildren() {
	return this.children;
    }

    public void setChildren(Queue<Node> q){
	this.children = q;
    }


    public double getTotalTimeCostToHere() {
	return totalTimeCostToHere;
    }


    public void setTotalTimeCostToHere(double totalTimeCostToHere) {
	this.totalTimeCostToHere = totalTimeCostToHere;
    }





}

