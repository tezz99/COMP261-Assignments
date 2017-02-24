import java.util.ArrayList;

/**
 *
 * @author PriteshRPatel PC
 */

public class Polygon {
    
    private ArrayList<Location> locations = new ArrayList<Location>();
    
    public Polygon (ArrayList<Location> locations) {
        this.locations = locations;
    }

    public ArrayList<Location> getLocations() {
        return this.locations;
    }
    
}
