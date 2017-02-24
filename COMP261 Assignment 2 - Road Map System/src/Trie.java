import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author PriteshRPatel PC
 */
public class Trie {

    private TrieNode root;
    private List<Integer> predictiveIDs;//Constructor
    
    private boolean singleRoad = false;


    public Trie() {
        root = new TrieNode();
        predictiveIDs = new ArrayList<Integer>();
    }

    public void addString(String string, int roadID) {
        TrieNode pointer = root;
        for (int i = 0; i < string.length(); i++) {
            if (pointer.getChildren().containsKey(string.charAt(i))) {
                pointer = pointer.getChildren().get(string.charAt(i));
            } else {
                TrieNode temp = new TrieNode();
                pointer.getChildren().put(string.charAt(i), temp);
                pointer = temp;
            }
        }
        pointer.addRoadID(roadID);
        pointer.setCompleteWord();
    }

    public List<Integer> findString(String string) {
        this.singleRoad = false;
        this.predictiveIDs = new ArrayList<Integer>();
        TrieNode pointer = root;
        
        for (int i = 0; i < string.length(); i++) {
            if (pointer.getChildren().containsKey(string.charAt(i))) {
                pointer = pointer.getChildren().get(string.charAt(i));
            }
            else {
                return predictiveIDs; //Would be empty at this stage and would mean the road/string is not found
            }
        }
        
        //If it is a complete word where the pointer is at then single road becomes true and passes a list of roadIDs
        if (pointer.isCompleteWord()) {
            this.singleRoad = true;
            return pointer.getRoadIDs();
        }
        
        //If not a single road, passes a list of the possible roads.
        return findRest(pointer);
    }

    
    public List<Integer> findRest(TrieNode pointer) {
        Stack<TrieNode> stack = new Stack();
        List<TrieNode> dealthWith = new ArrayList<TrieNode>();
        
        stack.push(pointer);
        
        while (!stack.isEmpty()) {
            TrieNode node = stack.pop();   
            for(TrieNode tN : node.getChildren().values()) {
                if (!dealthWith.contains(tN)) {
                    stack.push(tN);
                }
            }   
            dealthWith.add(node);
        }
        
        
        //Adds all roadIDs of all roads with prefix into one list of IDs.
        for (int i =0; i < dealthWith.size(); i++) {
            this.predictiveIDs.addAll(dealthWith.get(i).getRoadIDs());
        }
        
        this.singleRoad = false; //Makes single road false as it is giving multiple roads!
        return predictiveIDs; //Returns the list of IDs.
    }
    

    
    public boolean isSingleRoad() {
        return this.singleRoad;
    }
    
}
