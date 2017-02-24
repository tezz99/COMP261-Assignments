import java.util.Map;
import java.util.TreeMap;


public class StrToIntNode {
    public Map<String, Integer> KVMap = new TreeMap<String, Integer>();
    public boolean isLeaf;

    public String[] keys;
    public StrToIntNode[] children;
    public StrToIntNode next;

    //To keep count of the the number of elements in an array.
    public int numKeys = 0; //Number of keys in the array (BASICALLY NODE.SIZE FROM PSEUDOCODE)
    public int numChildren = 0; //Number of children in the array; USED TO KEEP TRACK OF THE NUMBER OF CHILDREN  NODE HAS.

    public StrToIntNode(boolean isLeaf, int size) {
	this.isLeaf = isLeaf;

	keys = new String[size + 1]; //Size + 1 since first element in array contains nothing
	children = new StrToIntNode[size + 1]; //Basically keys size
    }

    //Adds key at the position given and does the required shifting!
    public void addKey(int position, String key){
	// move all the keys to make space for the new one
	for(int i = keys.length - 1; i > position; i--)
	    keys[i] =  keys[i - 1];

	keys[position] = key;
	numKeys++;
    }

    //Adds key at the position given and does the required shifting!
    public void addChild(int position, StrToIntNode child){
	// move all the keys to make space for the new one
	for(int i = children.length - 1; i > position; i--)
	    children[i] = children[i - 1];

	children[position] =  child;
	numChildren++; //Increments the count of number of children in the children array.
    }
}
