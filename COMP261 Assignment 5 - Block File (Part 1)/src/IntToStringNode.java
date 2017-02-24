import java.util.Map;
import java.util.TreeMap;

public class IntToStringNode {

    public Map<Integer, String> KVMap = new TreeMap<Integer, String>();
    public boolean isLeaf;
    //public final int maxSize;

    public Integer[] keys;
    public IntToStringNode[] children; //Basically the pointers to the children.
    public IntToStringNode next;

    //To keep count of the the number of elements in an array.
    public int numKeys = 0; //Number of keys in the array
    public int numChildren = 0; //Number of children in the array;

    public IntToStringNode(boolean isLeaf, int size){
	this.isLeaf = isLeaf;
	//this.maxSize = size;

	keys = new Integer[size + 1]; //Size + 1 since first element in array contains nothing
	children = new IntToStringNode[size + 1]; //Basically keys size
    }

    public boolean isLeaf() {
	return isLeaf;
    }


    //Adds key at the position given and does the required shifting!
    public void addKey(int position, int key){
	// move all the keys to make space for the new one
	for(int i = keys.length - 1; i > position; i--)
	    keys[i] =  keys[i - 1];

	keys[position] = key;
	numKeys++;
    }

    //Adds key at the position given and does the required shifting!
    public void addChild(int position, IntToStringNode child){
	// move all the keys to make space for the new one
	for(int i = children.length - 1; i > position; i--)
	    children[i] = children[i - 1];

	children[position] =  child;
	numChildren++; //Increments the count of number of children in the children array.
    }
}
