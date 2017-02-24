package Main;

import java.awt.Point;
import java.io.File;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class World {
	
    public static final int SIZE = 12;

    private Set<Point> availableFuel = Collections.newSetFromMap(new ConcurrentHashMap<Point, Boolean>());
    private Random rand = new Random();
    private Robot[] robots;

    public World(){
	robots = new Robot[]{null,
			     new Robot(this, 0, 0, "red"),
			     new Robot(this, SIZE-1, SIZE-1, "blue")};
    }

    /**
     * This world can no longer be used after this call.
     */
    public void reset(){
	availableFuel.clear();
	for (int i=1; i<=2; i++){
	    robots[i].updatePending();
	    robots[i].cancel();
	}
    }

    public Set<Point> getAvailableFuel() {
	return availableFuel;
    }

    public void updateWorld(){
	addFuel(false);
	for (int i=1; i<=2; i++){
	    robots[i].updatePending();
	}
    }

    public void loadRobotProgram(int id, File code){
	RobotProgramNode prog = Parser.parseFile(code);
	if (prog != null){
	    System.out.println("Robot "+id+" now has program: ");
	    System.out.println(prog);
	    robots[id].setProgram(prog);
	}
    }

    public void start(){
	//add some initial fuel
	if(availableFuel.isEmpty()){
	    addFuel(true);
	    addFuel(true);
	}
	new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			robots[1].run();
		    }catch(RobotInterruptedException e){}
		    robots[1].setFinished(true);
		}
	    }).start();
	new Thread(new Runnable() {
		@Override
		public void run() {
		    try {robots[2].run();
		    }catch(RobotInterruptedException e){}
		    robots[2].setFinished(true);
		}
	    }).start();
    }

    public Robot getRobot(int id){
	if (id<=0 || id>robots.length){ return null; }
	return robots[id];
    }


    /** Returns the opponent robot of the argument */
    public Robot getOtherRobot(Robot robot) {
	if (robot==robots[2]) return robots[1];
	if (robot==robots[1]) return robots[2];
	return null;
    }

    private void addFuel(boolean definitely){
	if(definitely || rand.nextDouble() < 0.2){
	    int x = rand.nextInt(12);
	    int y = rand.nextInt(12);
	    Point fuel = new Point(x, y);
	    availableFuel.add(fuel);
	}
    }
}
