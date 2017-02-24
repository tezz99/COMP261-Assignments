
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;/**
 *
 * @author PriteshRPatel PC
 */
public class TrieNode {

    private HashMap<Character, TrieNode> children;
    private boolean completeWord = false;
    private List<Integer> roadIDs;
    
    
    //Constructor used to create root trie node.
    public TrieNode() {
        roadIDs = new ArrayList<Integer>();
        this.children = new HashMap<Character, TrieNode>();
    }

    public List getRoadIDs() {
        return this.roadIDs;
    }
    
    public HashMap<Character, TrieNode> getChildren(){
        return this.children;
    }
    
    public boolean isCompleteWord() {
        return this.completeWord;
    }
    
    public void setCompleteWord() {
        this.completeWord = true;
    } 
    
    public void addRoadID(int roadID) {
        this.roadIDs.add(roadID);
    }
    
    
}
