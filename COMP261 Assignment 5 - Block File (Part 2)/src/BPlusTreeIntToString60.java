import java.util.ArrayList;
import java.util.Map;


/**
  Implements a B+ tree in which the keys are integers and the
  values are Strings (with maximum length 60 characters)
 */

public class BPlusTreeIntToString60 {

    private IntToStringNode root; //The root node of the tree.
    private int maxSize;//Size of the B+ tree
    private int maxLeafKeys = 256; //maximum number of keys in a leaf


    public BPlusTreeIntToString60() {
	this.maxSize = 15; //Maximum keys in a node?
    }


    /**
     * Returns the String associated with the given key,
     * or null if the key is not in the B+ tree.
     */
    public String find(int key){
	if (root  != null) {
	    return find(key, root); 
	}
	return null;
    }

    public String find(int key, IntToStringNode node) {
	//If node is a leaf, then go thru each key in the leaf until you find the right one. And then pass it its value.
	if (node.isLeaf()) {
	    return node.KVMap.get(key); //Will return null if key doesnt exist. Will return the value if key DOES exist.
	} else {
	    for (int i = 1; i <= node.numKeys; i++) { //numKeys is basically the size of the keys array (num of keys in array)
		if(key < node.keys[i]) {
		    return find(key, node.children[i-1]); //Returns the last element of the children array? from slides
		}
	    }
	    return find(key, node.children[node.numKeys]);
	}
    }

    /**
     * Stores the value associated with the key in the B+ tree.
     * If the key is already present, replaces the associated value.
     * If the key is not present, adds the key with the associated value
     * @param key 
     * @param value
     * @return whether pair was successfully added.
     */
    public boolean put(int key, String value){
	if (root == null) {
	    //Creates a new leaf node, adds its key and value to it and then sets the leaf as the root node.
	    IntToStringNode leaf = new IntToStringNode(true, this.maxSize);
	    leaf.KVMap.put(key, value);
	    this.root = leaf;

	    return true; //Returns true as the pair have been successfully added

	} else {
	    IntToStrPairNode node = add(key, value, root); //If root was full, returns a new key and new leaf node.

	    //Makes a new root node.
	    if (node != null) {
		IntToStringNode newNode = new IntToStringNode(false, this.maxSize);
		newNode.numKeys = 1;
		newNode.children[0] = root;
		newNode.keys[1] = node.key;
		newNode.children[1] = node.node;
		this.root = newNode;
	    }
	    return true;
	}
    }

    public IntToStrPairNode add(int key, String value, IntToStringNode node) {
	if (node.isLeaf()) {
	    if (node.numKeys < this.maxLeafKeys){
		node.KVMap.put(key, value);
		return null;
	    } else {    
		return splitLeaf(key, value, node);
	    }
	}

	//Since its not a leaf, its an internal node and therefore needs to go thru the following loop!
	for (int i = 1; i <= node.numKeys; i++) {
	    if(key < node.keys[i]) {
		IntToStrPairNode node1 = add(key, value, node.children[i-1]); 
		if (node1 == null) {
		    return null;
		} else {
		    return dealWithPromote(node1.key, node1.node, node);
		}
	    }
	}

	IntToStrPairNode node2 = add(key, value, node.children[node.numKeys]);
	if (node2 == null) {
	    return null;
	} else {
	    return dealWithPromote(node2.key, node2.node, node);
	}
    }


    public IntToStrPairNode splitLeaf(int key, String value, IntToStringNode node) {
	node.KVMap.put(key, value); 
	IntToStringNode sibling = new IntToStringNode(true, this.maxSize);

	int mid = (node.numKeys + 1)/2;

	ArrayList<Integer> keyList = new ArrayList<Integer>(); //Used for removing entries from tree map after copying is done.

	//Copies Keys into sibling node.
	int i = 0;
	for(Map.Entry<Integer,String> entry : node.KVMap.entrySet()) {
	    i++;
	    if (i >= mid) {
		sibling.KVMap.put(entry.getKey(), entry.getValue());
		keyList.add(entry.getKey());
	    }
	}

	//Removes the keys from the node.
	for (int j = 0; j < keyList.size(); j++) {
	    node.KVMap.remove(keyList.get(j));
	}

	if (node.next != null) {
	    sibling.next = node.next;
	}

	node.next = sibling;

	return (new IntToStrPairNode(sibling.keys[0], sibling));
    }

    public IntToStrPairNode dealWithPromote(int newKey, IntToStringNode rightChild, IntToStringNode node) {

	if (rightChild == null) {
	    return null;
	}

	if (newKey > node.keys[node.numKeys]) {
	    node.keys[node.numKeys + 1] = newKey;
	    node.children[node.numKeys + 1] = rightChild;
	} else {
	    for (int i = 1; i <= node.numKeys; i++) {
		if (newKey < node.keys[i]) {
		    node.addKey(i, newKey);
		    node.addChild(i, rightChild);
		}
	    }
	}

	// No need to promote further
	if(node.numKeys <= this.maxSize) {
	    return null;
	}

	IntToStringNode sibling = new IntToStringNode(false, this.maxSize); //Create a new node.
	int mid = (this.maxSize/2)/ + 1; //Node is overfull, have to split and promote.


	int j = 0; //counter for filling sibling.
	//Not sure if les than or equal to or not. check
	for (int i = mid + 1; i <= this.maxSize; i++) {
	    j++;
	    sibling.keys[j] = node.keys[i];
	    node.keys[i] = null; //remove element
	}


	j = 0;//Reset back to zero.
	for (int i = mid; i <= this.maxSize; i++){
	    sibling.children[j] = node.children[i];
	    node.children[i] = null;
	}

	//Upadating ket and children counters in node.
	node.numChildren = mid + 1;
	node.numKeys = mid;

	int promoteKey = node.keys[mid];

	node.keys[mid] = null;

	return (new IntToStrPairNode(promoteKey, sibling));
    }

    public void printAll() {
	IntToStringNode leaf = root; //Set leaf as root for now.

	//Keep going left until you find the leftmost left node.
	while(!leaf.isLeaf){
	    leaf = leaf.children[0];
	}

	while(leaf != null){
	    for(Map.Entry<Integer,String> entry : leaf.KVMap.entrySet()) {
		System.out.println("IP: " + DNSDB.IPToString(entry.getKey()) + "\t\t\t\t\t HOST: " + entry.getValue());
	    }
	    leaf = leaf.next;
	}
    }
}