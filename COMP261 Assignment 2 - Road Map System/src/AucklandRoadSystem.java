//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.testing
 */

/**
 *
 * @author PriteshRPatel PC
 */
public class AucklandRoadSystem extends GUI {

    private Map<Integer, Node> nodeMap;
    private Map<Integer, Road> roadMap;
    private List<Segment> segmentList = new ArrayList<Segment>();
    private List<Polygon> polygonList = new ArrayList<Polygon>();

    private Location origin;
    private double scale;

    //Used To calculate scale
    private double lowestX = Double.POSITIVE_INFINITY;
    private double lowestY = Double.POSITIVE_INFINITY;
    private double highestX = Double.NEGATIVE_INFINITY;
    private double highestY = Double.NEGATIVE_INFINITY;

    //For selecting nodes feature
    private int clickedNodeID = 0; //Doubled up as the start node id.
    private int endNodeID = 0; 

    //For dragging map
    private double oldX;
    private double oldY;
    private double dragX;
    private double dragY;

    private boolean drag = false;

    //For basic search feature
    List<Integer> searchedIDList = new ArrayList<Integer>(); //List of Road object IDs part of the same road.

    //For trie search feature
    private Trie trie;
    private boolean singleRoad = false; //IF the list of roadIDs returned from trieSearch method is of 1 road, then set to ture. This is used for the color of roads.


    //For AStar Search
    private Stack<Node> aStarNodes= new Stack<Node>();
    private List<Segment> aStarSegments = new ArrayList<Segment>();

    //For Articulation Points
    private HashSet<Node> articulationPoints = new HashSet<Node>();
    private HashSet<Node> visitedSet = new HashSet<Node>();

    //For Restrictions feature
    private HashMap<Node, List<Restriction>> effectedNodeMap = new HashMap<Node, List<Restriction>>();


    //***SWITCHES***
    private boolean polygon = false; //Draw polygons or not
    private boolean trieSearch = true; //Use try search or normal search from core
    private boolean iterAP = true; //Use iterative or recursive version of Articulation Points
    private boolean restrictionsOn = true; //Use to turn off and on restrictions for route finding.
    private boolean shortestDistance = true; //Set to find the shortest route by distance first.


    public static void main(String[] args) {
	new AucklandRoadSystem();
    }

    public AucklandRoadSystem() {
	this.nodeMap = new HashMap<Integer, Node>(); //Initialise node map
	this.roadMap = new HashMap<Integer, Road>(); //Initialise the road map
	trie = new Trie();
	getTextOutputArea().setText("Polygons: " + this.polygon + "\n" + "Trie Seach: " + this.trieSearch + "\n"  + "Iterative Articulation Points: " + this.iterAP + "\n"  + "Road Restrictions: " + this.restrictionsOn + "\n"  );
    }

    @Override
    public void redraw(Graphics g) {
	g.setColor(Color.gray);

	// Draw All nodes/intersections
	for (Node n : this.nodeMap.values()) {
	    g.fillOval((int) n.getLocation().asPoint(this.origin, scale).getX(), (int) n.getLocation().asPoint(this.origin, scale).getY(), 3 , 3);
	}

	// Draw All nodes/intersections
	g.setColor(Color.red);
	for (Node n : this.articulationPoints) {
	    g.fillOval((int) n.getLocation().asPoint(this.origin, scale).getX(), (int) n.getLocation().asPoint(this.origin, scale).getY(), 4 , 4);
	}
	g.setColor(Color.gray);



	//Draw all Segments
	for (Segment s : this.segmentList) {

	    Location previous = s.locations.get(0);
	    for (int i = 1; i < s.locations.size(); i++) {
		g.drawLine((int) previous.asPoint(this.origin, this.scale).getX(), (int) previous.asPoint(this.origin, this.scale).getY(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getX(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getY());
		previous = s.locations.get(i);
	    }
	}


	//DRAWING ASTAR RESULT
	g.setColor(Color.blue);
	for (Segment s : this.aStarSegments) {
	    Location previous = s.locations.get(0);
	    for (int i = 1; i < s.locations.size(); i++) {
		g.drawLine((int) previous.asPoint(this.origin, this.scale).getX(), (int) previous.asPoint(this.origin, this.scale).getY(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getX(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getY());
		previous = s.locations.get(i);
	    }
	}
	g.setColor(Color.gray);



	//Draws the polygons (DOESNT DRAW PROPERLY)
	if (this.polygon) {
	    g.setColor(Color.black);
	    for (Polygon p : this.polygonList) {
		Location previous = p.getLocations().get(0);
		for (int i = 1; i < p.getLocations().size(); i++) {
		    g.drawLine((int) previous.asPoint(this.origin, this.scale).getX(), (int) previous.asPoint(this.origin, this.scale).getY(), (int) p.getLocations().get(i).asPoint(this.origin, this.scale).getX(), (int) p.getLocations().get(i).asPoint(this.origin, this.scale).getY());
		    previous = p.getLocations().get(i);
		}
	    }
	    g.setColor(Color.gray);
	}


	//Highlights all roads with the name entered in search. 
	if (!this.searchedIDList.isEmpty()) {

	    //Sets color depending on the search result. If showing exact searched road, color will be green otherwise, will show predicted roads as yellow.
	    if (this.singleRoad) {
		g.setColor(Color.green);
	    } else {
		g.setColor(Color.orange);
	    }

	    for (int roadID : this.searchedIDList) {
		for (Segment s : this.roadMap.get(roadID).segments) {
		    Location previous = s.locations.get(0);
		    for (int i = 1; i < s.locations.size(); i++) {

			g.drawLine((int) previous.asPoint(this.origin, this.scale).getX(), (int) previous.asPoint(this.origin, this.scale).getY(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getX(), (int) s.locations.get(i).asPoint(this.origin, this.scale).getY());
			previous = s.locations.get(i);
		    }
		}
	    }
	    g.setColor(Color.gray);
	}

	//Highlights clicked/selected node
	if (this.clickedNodeID != 0) {
	    g.setColor(Color.blue);
	    g.fillOval((int) this.nodeMap.get(clickedNodeID).getLocation().asPoint(this.origin, scale).getX(), (int) this.nodeMap.get(clickedNodeID).getLocation().asPoint(this.origin, scale).getY(), 6, 6);
	    g.setColor(Color.gray);
	}

	//Highlings the end node
	if (this.endNodeID != 0) {
	    g.setColor(Color.green);
	    g.fillOval((int) this.nodeMap.get(endNodeID).getLocation().asPoint(this.origin, scale).getX(), (int) this.nodeMap.get(endNodeID).getLocation().asPoint(this.origin, scale).getY(), 6, 6);
	    g.setColor(Color.gray);
	}

    }

    //Used for dragging map
    public void onMousePress(MouseEvent e) {
	this.oldX = e.getX();
	this.oldY = e.getY();
    }

    public void onMouseDrag(MouseEvent e) {
	this.dragX = e.getX();
	this.dragY = e.getY();
	this.origin = this.origin.moveBy((this.oldX - this.dragX) * (1 / this.scale), (this.dragY - this.oldY) * (1 / this.scale));
	this.oldX = e.getX();
	this.oldY = e.getY();

	this.drag = true; 

    }

    @Override
    public void onClick(MouseEvent e) {
	//If mouse isn't dragged then..
	if (this.drag == false) {
	    // Everything below is for selecting intersections.
	    Location clickedLocation = Location.newFromPoint(e.getPoint(), this.origin, this.scale); //Location of mouse click

	    Node closestNode = null; //The location that is closest to the mouse click
	    double lowestDistance = Double.MAX_VALUE; //Initial lowset distance. Its high so it cant find one that is lower..

	    //Finds the closest node to the mouse click. The 0.15 number can be changed to allow for accuracy. The more accurate the click is expected to be, the closer to zero it should be.
	    if (this.clickedNodeID == 0) {
		for (Node n : this.nodeMap.values()) {
		    if (clickedLocation.distance(n.getLocation()) < 0.15) {
			if (clickedLocation.distance(n.getLocation()) < lowestDistance) {
			    lowestDistance = clickedLocation.distance(n.getLocation());
			    closestNode = n;
			    this.clickedNodeID = closestNode.getNodeID();
			}
		    }
		}
	    } else if (this.endNodeID == 0) {
		for (Node n : this.nodeMap.values()) {
		    if (clickedLocation.distance(n.getLocation()) < 0.15) {
			if (clickedLocation.distance(n.getLocation()) < lowestDistance) {
			    lowestDistance = clickedLocation.distance(n.getLocation());
			    closestNode = n;
			    if (this.clickedNodeID != closestNode.getNodeID()) {
				this.endNodeID = closestNode.getNodeID();
			    }
			}
		    }
		}
	    }

	    //If there is a node close to the click, Outputs information about the node. Need to check if roads in and out are being printed correctly.
	    if (this.endNodeID == 0) {
		if (closestNode != null) {
		    String connectedRoads = "";

		    Set<String> connectedRoadsSet = new HashSet<String>(); //Set id road names connected to the node.

		    //Following two loops add all roads connected to the node to the connectedRoadsSet set

		    for (Segment s : closestNode.edgesIn) {
			connectedRoadsSet.add(this.roadMap.get(s.getRoadID()).getRoadName());
		    }


		    for (Segment s : closestNode.edgesOut) {
			connectedRoadsSet.add(this.roadMap.get(s.getRoadID()).getRoadName());
		    }

		    //Turns set of connected roads into a string
		    for (String s : connectedRoadsSet) {
			connectedRoads = connectedRoads.concat(s + ", ");
		    }

		    //Prints set of connected nodes.
		    getTextOutputArea().setText("Node ID:" + (Integer.toString(closestNode.getNodeID())) + "\nRoads Connected to Intersection: " + connectedRoads);
		    this.searchedIDList = new ArrayList<Integer>();

		    return;
		}
	    } else {
		this.aStarSearch(); //Conducts AStar Search because there is an end node selected
	    }

	    if (closestNode == null) {
		this.clickedNodeID = 0; //If click is not on a node, deselects the node.
		this.endNodeID = 0;//If click is not on a node, deselects the node.
		this.articulationPoints = new HashSet();
		this.aStarNodes = new Stack<Node>();
		this.aStarSegments = new Stack<Segment>();
		this.searchedIDList = new ArrayList<Integer>();
		getTextOutputArea().setText(""); //Clears text area.
	    }
	}

	this.drag = false;
    }

    //Used for zooming
    public void onWheelMove(MouseWheelEvent e) {
	if (this.scale - e.getWheelRotation() > 0.5) {
	    this.scale -= (e.getWheelRotation() - 0.5);
	}
    }



    @Override
    public void onMove(Move m) {
	switch (m) {
	case ZOOM_IN:
	    this.scale += 2;
	    break;

	case ZOOM_OUT:
	    if (scale - 0.5 > 1) {
		this.scale -= 0.5;
		break;
	    }
	    break;

	case NORTH:
	    this.origin = this.origin.moveBy(0, -7);
	    break;

	case EAST:
	    this.origin = this.origin.moveBy(-7, 0);
	    break;

	case SOUTH:
	    this.origin = this.origin.moveBy(0, 7);
	    break;

	case WEST:
	    this.origin = this.origin.moveBy(7, 0);
	    break;
	}

    }


    @Override
    public void onSearch() {
	if (getSearchBox().getText().equals("find ap")) {
	    if (this.iterAP) {
		this.resetForArticulationPoints(); //Resets all nodes to run the articulation points algorithm
		this.findArticulationPointsIter();
		return;
	    }
	    else {
		this.resetForArticulationPoints(); //Resets all nodes to run the articulation points algorithm
		this.findArticulationPointsRec();
		return;
	    }
	}

	if (this.trieSearch) {
	    trieSearch();
	} else {
	    normalSearch();
	}
    }

    public void normalSearch() {
	this.clickedNodeID = 0; //Deselects the selected node.
	this.searchedIDList = new ArrayList<Integer>(); //Clears IDs of searched road

	boolean found = false; //boolean used to remember if a road has been found.

	for (Road r : this.roadMap.values()) {
	    if (getSearchBox().getText().equals(r.getRoadName())) {
		this.searchedIDList.add(r.getRoadID());
		this.singleRoad = true;
		found = true;
	    }
	}

	//If found, display road information!
	if (found) {
	    //Because there are multiple road objects of the same road, need to make it so it does not display duplicate information.
	    Map<String, Integer> searchedIDMap = new HashMap<String, Integer>();
	    for (int ID : this.searchedIDList) {
		if (!searchedIDMap.containsKey(this.roadMap.get(ID).getCity())) { //If the city is not a key already, add it as key and add the RoadID as its value.
		    searchedIDMap.put(this.roadMap.get(ID).getCity(), ID);
		}
	    }

	    // Displays road information.
	    int i = 1;
	    getTextOutputArea().setText("");//Clears text output area.
	    for (Map.Entry<String, Integer> entry : searchedIDMap.entrySet()) {
		getTextOutputArea().append("Found " + i + ": " + this.capsFirst(this.roadMap.get(entry.getValue()).getRoadName()) + ", " + this.capsFirst(entry.getKey()) + "\n");
		i++;
	    }

	}

	if (!found) {
	    this.searchedIDList = new ArrayList<Integer>();
	    getTextOutputArea().setText("Searched Road Not Found");
	}
    }

    public void trieSearch() {
	this.singleRoad = false;
	this.clickedNodeID = 0; //Deselects the selected node.
	this.searchedIDList = new ArrayList<Integer>(); //Clears IDs of searched road

	this.searchedIDList = trie.findString(getSearchBox().getText());

	//Because there are multiple road objects of the same road, need to make it so it does not display duplicate information.
	Map<String, Integer> searchedIDMap = new HashMap<String, Integer>();
	for (int ID : this.searchedIDList) {
	    if (!searchedIDMap.containsKey(this.roadMap.get(ID).getCity())) { //If the city is not a key already, add it as key and add the RoadID as its value.
		searchedIDMap.put(this.roadMap.get(ID).getCity(), ID);
	    }
	}

	// Displays road information.
	int i = 1;
	getTextOutputArea().setText("");//Clears text output area.
	for (Map.Entry<String, Integer> entry : searchedIDMap.entrySet()) {
	    getTextOutputArea().append("Found " + i + ": " + this.capsFirst(this.roadMap.get(entry.getValue()).getRoadName()) + ", " + this.capsFirst(entry.getKey()) + "\n");
	    i++;
	}

	if (trie.isSingleRoad()) {
	    this.singleRoad = true;
	} else {
	    this.singleRoad = false;
	}

	if (this.searchedIDList.isEmpty()) {
	    getTextOutputArea().setText("No road with that prefix");
	    return;
	}
    }

    public void findArticulationPointsRec() {
	//While all nodes have not been visited, keep finding articulation points for unvisted nodes. Needed because graph has multiple components
	while (this.canContinue() != null) {
	    Node start = this.canContinue();

	    start.setDepth(0);
	    int numSubtrees = 0;

	    for (int id : start.getAdjacentNodeIDs()) {
		Node neighbour = this.nodeMap.get(id);
		if (neighbour.getDepth() == Integer.MAX_VALUE){
		    this.recArtPts(neighbour, 1, start);
		}
	    }
	    if (numSubtrees > 1) {
		this.articulationPoints.add(start);
	    }
	    getTextOutputArea().setText("Articulation Points: " + this.articulationPoints.size()); //Displays number of articulation points
	}
    }

    //Needed for articulation points button button
    public void findArticulationPoints() {
	if (this.iterAP) {
	    this.resetForArticulationPoints(); //Resets all nodes to run the articulation points algorithm
	    this.findArticulationPointsIter();
	    return;
	}
	else {
	    this.resetForArticulationPoints(); //Resets all nodes to run the articulation points algorithm
	    this.findArticulationPointsRec();
	    return;
	}
    }

    public int recArtPts(Node node, int depth, Node fromNode){
	node.setDepth(depth);
	int reachback = depth;

	for (int id : node.getAdjacentNodeIDs()) {
	    Node neighbour = this.nodeMap.get(id);
	    this.visitedSet.add(neighbour);

	    if (neighbour != fromNode) {
		if (neighbour.getDepth() < Integer.MAX_VALUE) {
		    reachback = Math.min(neighbour.getDepth(), reachback);
		} else {
		    int childReach = recArtPts(neighbour, depth+1, node);
		    if (childReach >= depth) {
			this.articulationPoints.add(node);
		    }
		    reachback = Math.min(childReach, reachback);
		}
	    }
	}
	return reachback;
    }

    public void findArticulationPointsIter() {

	while (this.canContinue() != null){
	    Node start = this.canContinue();
	    start.setDepth(0);
	    int numOfSubTrees = 0;

	    for (int id : start.getAdjacentNodeIDs()) {
		Node neighbour = this.nodeMap.get(id);

		if (neighbour.getDepth() == Integer.MAX_VALUE){
		    this.iterArtPts(neighbour, start);
		    numOfSubTrees++;
		}
	    }
	    if (numOfSubTrees > 1) {
		this.articulationPoints.add(start);
	    }

	    this.visitedSet.add(start);
	}
	getTextOutputArea().setText("Articulation Points: " + this.articulationPoints.size());
    }

    public void iterArtPts(Node firstNode, Node root) {
	Stack<Node> fringe = new Stack<Node>();

	firstNode.setDepth(1);
	firstNode.setFromNodeAP(root);
	firstNode.setReachback(0);
	fringe.push(firstNode);

	while (!fringe.isEmpty()) {

	    Node node = fringe.peek();

	    if (node.getChildren() == null) {
		node.setChildren(new LinkedList<Node>());
		for (int n : node.getAdjacentNodeIDs()) {
		    Node neighbour = this.nodeMap.get(n);
		    if (neighbour != node.getFromNodeAP()){
			node.getChildren().offer(neighbour);
		    }
		}
	    } 
	    else if (!node.getChildren().isEmpty()) {
		Node child = node.getChildren().poll();
		if (child.getDepth() < Integer.MAX_VALUE) {
		    node.setReachback(Math.min(node.getReachback(), child.getDepth()));
		}
		else {
		    child.setDepth(node.getDepth()+1);
		    child.setFromNodeAP(node);
		    child.setReachback(node.getDepth()); ///Not in notes
		    fringe.push(child);
		}
	    }
	    else {
		if (node != firstNode) {
		    if (node.getReachback() >= node.getFromNodeAP().getDepth()) {
			this.articulationPoints.add(node.getFromNodeAP());
		    }
		    node.getFromNodeAP().setReachback(Math.min(node.getFromNodeAP().getReachback(), node.getReachback()));
		}
		this.visitedSet.add(fringe.pop());
	    }
	}
    }

    /*Checks if there are still nodes that have not been visited when finding articulation points. Required becasue there are multiple components in the graph*/
    public Node canContinue() {
	for (Node n : this.nodeMap.values()){
	    if (!this.visitedSet.contains(n)) {
		return n;
	    }
	}
	return null;
    }

    /* Resets nodes and required fields for finding articulation points*/
    public void resetForArticulationPoints() {
	this.visitedSet.clear();
	//articulationPoints = new ArrayList<Node>();
	articulationPoints = new HashSet<Node>();

	for (Node n : this.nodeMap.values()) {
	    n.resetForAP();
	}
    }

    //Calls aStarSearch method depending on the set mode (distance or time)
    public void aStarSearch(){
	if (this.shortestDistance) {
	    this.aStarSearchDistance();
	} else {
	    this.aStarSearchTime();
	}
    }

    public void changeRouteMode() {
	if (this.shortestDistance) {
	    this.shortestDistance = false;
	    getTextOutputArea().setText("Set to: Find by Shortest Distance");
	} else {
	    getTextOutputArea().setText("Set to: Find by Fastest Time");
	    this.shortestDistance = true;
	}
    }


    public void aStarSearchDistance() {
	this.resetForAStarSearch(); //Resets the nodes and fields for AStar Search

	PriorityQueue<Node> fringe = new PriorityQueue<Node>();
	Node goal = this.nodeMap.get(this.endNodeID);

	Node start = this.nodeMap.get(this.clickedNodeID); 
	start.setFromNode(null);
	start.setTotalCostToHere(0);
	start.setEstimateToGoal(start.getLocation().distance(this.nodeMap.get(this.endNodeID).getLocation())); //Uses the distance method in location class
	fringe.offer(start);

	while (!fringe.isEmpty()) {
	    Node thisNode = fringe.poll();
	    Node fromNode = thisNode.getFromNode();
	    double costToHere = thisNode.getTotalCostToHere();
	    double totalCostToGoal = thisNode.getEstimateToGoal();


	    //System.out.println(thisNode.getEstimateToGoal());

	    if (!thisNode.isVisited()) {
		thisNode.setVisited(true);
		thisNode.setFromNode(fromNode);
		thisNode.setTotalCostToHere(costToHere);

		if (thisNode == goal) {
		    //Saves all nodes part of the result to aStarNodes stack.
		    Node next = goal;
		    while (next != null) {
			this.aStarNodes.push(next);
			next = next.getFromNode();
		    }
		    getTextOutputArea().setText("***SHORTEST DISTANCE PATH FOUND*** \n");
		    getTextOutputArea().append("Displaying route from NODE ID: " + start.getNodeID() + " to NODE ID: " + goal.getNodeID() + "\n\n");
		    this.printResultsAndProcess(); //Displays result of aStar Search and 
		    return;
		}


		for (Segment s : thisNode.edgesOut){
		    Node neighbour = this.nodeMap.get(s.getEndNodeID());

		    boolean hasAccess; //Used to turn restrictions on and off

		    if (this.restrictionsOn) {
			hasAccess = this.hasAccess(thisNode, neighbour);
		    } else {
			hasAccess = true;
		    }

		    if (!neighbour.isVisited() && hasAccess) {
			neighbour.setFromNode(thisNode);
			neighbour.setTotalCostToHere(costToHere + s.getLength());
			neighbour.setEstimateToGoal(costToHere + s.getLength() + neighbour.getLocation().distance(goal.getLocation()));
			fringe.offer(neighbour);
		    }
		}
	    }
	}

	getTextOutputArea().setText("Path NOT Found");
    }

    public void aStarSearchTime() {
	this.resetForAStarSearch(); //Resets the nodes and fields for AStar Search

	PriorityQueue<Node> fringe = new PriorityQueue<Node>();
	Node goal = this.nodeMap.get(this.endNodeID);

	Node start = this.nodeMap.get(this.clickedNodeID); 
	start.setFromNode(null);
	start.setTotalCostToHere(0);

	//Does not take road class into account
	start.setEstimateToGoal(((start.getLocation().distance(this.nodeMap.get(this.endNodeID).getLocation())) / 5)); //Divides distance by the lowest possible speed and then divides by class. Never overestimates so admissable.

	fringe.offer(start);

	while (!fringe.isEmpty()) {
	    Node thisNode = fringe.poll();
	    Node fromNode = thisNode.getFromNode();
	    double costToHere = thisNode.getTotalCostToHere();
	    double totalCostToGoal = thisNode.getEstimateToGoal();


	    //System.out.println(thisNode.getEstimateToGoal());

	    if (!thisNode.isVisited()) {
		thisNode.setVisited(true);
		thisNode.setFromNode(fromNode);
		thisNode.setTotalCostToHere(costToHere);

		if (thisNode == goal) {
		    //Saves all nodes part of the result to aStarNodes stack.
		    Node next = goal;
		    while (next != null) {
			this.aStarNodes.push(next);
			next = next.getFromNode();
		    }
		    getTextOutputArea().setText("***SHORTEST DISTANCE PATH FOUND*** \n");
		    getTextOutputArea().append("Displaying route from NODE ID: " + start.getNodeID() + " to NODE ID: " + goal.getNodeID() + "\n\n");
		    this.printResultsAndProcess(); //Displays result of aStar Search and 
		    return;
		}


		for (Segment s : thisNode.edgesOut){
		    Node neighbour = this.nodeMap.get(s.getEndNodeID());



		    boolean hasAccess; //Used to turn restrictions on and off
		    if (this.restrictionsOn) {
			hasAccess = this.hasAccess(thisNode, neighbour);
		    } else {
			hasAccess = true;
		    }



		    if (!neighbour.isVisited() && hasAccess) {
			neighbour.setFromNode(thisNode);


			//int roadClass = this.roadMap.get(s.getRoadID()).getRoadClass();

			int speed;

			if (this.roadMap.get(s.getRoadID()).getSpeed() == 0) {
			    speed = 5;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 1) {
			    speed = 20;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 2) {
			    speed = 40;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 3) {
			    speed = 60;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 4) {
			    speed = 80;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 5) {
			    speed = 100;
			} else if (this.roadMap.get(s.getRoadID()).getSpeed() == 6) {
			    speed = 110;
			} else {
			    speed = 120; //Being realisitc. No such thing as no limit!
			}

			//Does not take road class into account
			neighbour.setTotalCostToHere((costToHere) + ((s.getLength()/speed)));
			neighbour.setEstimateToGoal((costToHere) + ((s.getLength()/speed)) + (((neighbour.getLocation().distance(goal.getLocation()))/5)) );

			//neighbour.setTotalCostToHere((costToHere) + ((s.getLength()/speed)/roadClass + 1));
			//neighbour.setEstimateToGoal((costToHere) + ((s.getLength()/speed)/roadClass + 1) + (((neighbour.getLocation().distance(goal.getLocation()))/5)/1) );
			fringe.offer(neighbour);
		    }
		}
	    }
	}

	getTextOutputArea().setText("Path NOT Found");
    }


    public boolean hasAccess(Node currentNode, Node neighbour){
	if (this.effectedNodeMap.containsKey(currentNode)) {
	    for (Restriction r : this.effectedNodeMap.get(currentNode)) {
		if ((this.nodeMap.get(r.getFromNodeID()) == currentNode.getFromNode()) && (this.nodeMap.get(r.getToNodeID()) == neighbour)){
		    for (Segment s : currentNode.getFromNode().getEdgesOut()) {
			if (s.getRoadID() == r.getFromRoadID() && currentNode == this.nodeMap.get(s.getEndNodeID())) {
			    for (Segment seg : currentNode.getEdgesOut()) {
				if (seg.getRoadID() == r.getToRoadID() && neighbour == this.nodeMap.get(seg.getEndNodeID())) {
				    return false;
				}
			    }
			}
		    }
		}
	    }
	}
	return true;
    }



    public void printResultsAndProcess() {
	Node first = this.aStarNodes.pop();
	while(!this.aStarNodes.isEmpty()){
	    Node second = this.aStarNodes.pop();

	    for (Segment s : first.getEdgesOut()) {
		if (first.getNodeID() == s.getStartNodeID() && second.getNodeID() == s.getEndNodeID()){
		    this.aStarSegments.add(s);
		}
	    }
	    first = second;
	}

	LinkedHashMap<String, Double> roads = new LinkedHashMap<String, Double>();
	for (int i = 0; i < this.aStarSegments.size(); i++) {
	    String key = this.capsFirst(this.roadMap.get(this.aStarSegments.get(i).getRoadID()).getRoadName() + ", " + this.roadMap.get(this.aStarSegments.get(i).getRoadID()).getCity());
	    if (roads.containsKey(key)) {
		roads.put(key, roads.get(key) + this.aStarSegments.get(i).getLength());
	    } else {
		roads.put(key, this.aStarSegments.get(i).getLength());
	    }

	}

	//Print out route. Also adds total distance
	double totalDistance = 0;
	DecimalFormat df = new DecimalFormat("#.###");

	for (String key : roads.keySet()) {
	    getTextOutputArea().append(key + ":\t" + df.format(roads.get(key)) +"km"+ "\n");
	    totalDistance += roads.get(key);
	}
	getTextOutputArea().append("Total Distance = " + df.format(totalDistance) + "km\n");

	if (this.shortestDistance) {
	    getTextOutputArea().append("Route Finding mode used: Shortest Distance");
	} else {
	    getTextOutputArea().append("Route Finding mode used: Fastest Time");
	}
    }

    public void resetForAStarSearch() {
	for (Node n : this.nodeMap.values()){
	    n.resetForSearch();
	}
	this.aStarNodes = new Stack<Node>();
	this.aStarSegments = new Stack<Segment>();
    }


    //Method Makes road names look better!
    public String capsFirst(String str) {
	String[] words = str.split(" ");
	StringBuilder ret = new StringBuilder();
	for(int i = 0; i < words.length; i++) {
	    ret.append(Character.toUpperCase(words[i].charAt(0)));
	    ret.append(words[i].substring(1));
	    if(i < words.length - 1) {
		ret.append(' ');
	    }
	}
	return ret.toString();
    }



    @Override
    public void onLoad(File nodes, File roads, File segments, File polygons, File restrictions
	    ) {
	loadNodes(nodes);
	loadRoads(roads);
	loadSegments(segments);
	loadPolygons(polygons); //Will need to uncomment the code in the redraw method to draw the polygons. Not working properly.
	loadRestrictions(restrictions);

    }

    public void loadRestrictions(File restrictions) {
	//Doesnt load restrictions file if it doesnt exist
	if (restrictions == null) {
	    System.out.println("No Restriction File");
	    return;
	}

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(restrictions));
	    reader.readLine(); //Skip first line of headings
	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] values = line.split("\t"); //Split values of line into array

		Restriction restriction = new Restriction(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), Integer.parseInt(values[4]));

		if (this.effectedNodeMap.containsKey(this.nodeMap.get(Integer.parseInt(values[2])))){
		    this.effectedNodeMap.get(this.nodeMap.get(Integer.parseInt(values[2]))).add(restriction);
		} else {
		    this.effectedNodeMap.put(this.nodeMap.get(Integer.parseInt(values[2])), new ArrayList<Restriction>());
		    this.effectedNodeMap.get(this.nodeMap.get(Integer.parseInt(values[2]))).add(restriction);
		}
	    }

	    reader.close();
	} catch (FileNotFoundException exception) {
	    getTextOutputArea().setText("Restriction File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}



    }

    public void loadNodes(File nodes) {
	try {

	    BufferedReader reader = new BufferedReader(new FileReader(nodes));
	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] values = line.split("\t"); //Split values of line into array
		Node node = new Node(Integer.parseInt(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2])); //Create Node
		nodeMap.put(Integer.parseInt(values[0]), node); // Add node to hashmap

		//For calculating scale.
		if (node.getLocation().x < this.lowestX) {
		    this.lowestX = node.getLocation().x;
		}

		if (node.getLocation().y < this.lowestY) {
		    this.lowestY = node.getLocation().y;
		}

		if (node.getLocation().x > this.highestX) {
		    this.highestX = node.getLocation().x;
		}

		if (node.getLocation().y > this.highestY) {
		    this.highestY = node.getLocation().y;
		}

		this.origin = new Location(this.lowestX, this.highestY);
		this.scale = Math.min(getDrawingAreaDimension().getWidth() / (this.highestX - this.lowestX), getDrawingAreaDimension().getHeight() / (this.highestY - this.lowestY));
	    }
	    reader.close();
	} catch (FileNotFoundException exception) {
	    getTextOutputArea().setText("Node File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}
    }

    public void loadRoads(File roads) {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(roads));

	    reader.readLine(); //Skip first line of headings

	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] values = line.split("\t"); //Split values of line into array 
		Road road = new Road(Integer.parseInt(values[0]), Integer.parseInt(values[1]), values[2], values[3], Integer.parseInt(values[4]), Integer.parseInt(values[5]), Integer.parseInt(values[6]), Integer.parseInt(values[7]), Integer.parseInt(values[8]), Integer.parseInt(values[9]));
		roadMap.put(Integer.parseInt(values[0]), road);

		//For trie searching
		trie.addString(values[2], Integer.parseInt(values[0]));
	    }
	    reader.close();

	} catch (FileNotFoundException exception) {
	    getTextOutputArea().setText("Road File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}
    }

    public void loadSegments(File segments) {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(segments));
	    reader.readLine(); //Skip first line of headings
	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] values = line.split("\t");

		//Items required to create segment object
		int roadID = Integer.parseInt(values[0]);
		double length = Double.parseDouble(values[1]);
		int startNode = Integer.parseInt(values[2]);
		int endNode = Integer.parseInt(values[3]);
		List<Location> locations = new ArrayList<Location>();

		for (int i = 2; i + 2 < values.length; i += 2) {
		    Location location = Location.newFromLatLon(Double.parseDouble(values[i + 2]), Double.parseDouble(values[i + 3]));
		    locations.add(location);
		}

		Segment segment = new Segment(roadID, length, startNode, endNode, locations);

		this.segmentList.add(segment); //Add segment to collection of AcuklandRoadSystem
		this.roadMap.get(roadID).addSegment(segment);
		this.nodeMap.get(startNode).addEdgeOut(segment);

		this.nodeMap.get(endNode).addEdgeIn(segment);

		//Adds adjacent nodes
		this.nodeMap.get(startNode).addNighbourNodeID(endNode);
		this.nodeMap.get(endNode).addNighbourNodeID(startNode);

		if (this.roadMap.get(roadID).getDirections() == 0){
		    Segment segmentOtherWay = new Segment(roadID, length, endNode, startNode, locations);
		    this.nodeMap.get(endNode).addEdgeOut(segmentOtherWay);
		    this.nodeMap.get(startNode).addEdgeIn(segmentOtherWay);
		}
	    }
	    reader.close();

	} catch (FileNotFoundException exception) {
	    getTextOutputArea().setText("Segments File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}
    }

    public void loadPolygons(File polygons) {
	//Doesnt load polygon file if it doesnt exist
	if (polygons == null) {
	    System.out.println("No Polygon File");
	    return;
	}

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(polygons));

	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] firstSplit = line.split("=");
		if (firstSplit[0].equals("Data0")) {
		    String justCoordinates = firstSplit[1];
		    //System.out.println(justCoordinates);

		    String[] secondSplit = justCoordinates.split(",");

		    for (int i = 0; i < secondSplit.length; i += 2) {
			secondSplit[i] = secondSplit[i].substring(1);
		    }

		    for (int i = 1; i < secondSplit.length; i += 2) {
			secondSplit[i] = secondSplit[i].substring(0, secondSplit[i].length() - 1);
		    }

		    ArrayList<Location> locations = new ArrayList<Location>();

		    for (int i = 0; i + 1 < secondSplit.length; i += 2) {
			Location location = Location.newFromLatLon(Double.parseDouble(secondSplit[i]), Double.parseDouble(secondSplit[i + 1]));
			locations.add(location);
		    }

		    this.polygonList.add(new Polygon(locations));
		}
	    }
	    reader.close();
	} catch (FileNotFoundException exception) {
	    getTextOutputArea().setText("Polygon File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}
    }

}
