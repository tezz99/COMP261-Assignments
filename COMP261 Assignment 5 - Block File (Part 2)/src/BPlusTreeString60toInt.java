import java.util.ArrayList;
import java.util.Map;

/**
  Implements a B+ tree in which the keys  are Strings (with
  maximum length 60 characters) and the values are integers 
 */

public class BPlusTreeString60toInt {

    private StrToIntNode root; //The root node of the tree.
    private int maxSize;//Size of the B+ tree
    private int maxLeafKeys = 256; //maximum number of keys in a leaf. Set as per the slides.

    public BPlusTreeString60toInt() {
	this.maxSize = 15; //Maximum keys in a node. Currently set as per the slides.
    }


    /**
     * Returns the integer associated with the given key,
     * or null if the key is not in the B+ tree.
     */
    public Integer find(String key){
	if (root != null) {
	    return find(key, root);
	}
	return null;
    }

    public Integer find (String key, StrToIntNode node) {
	//If node is a leaf, then go thru each key in the leaf until you find the right one. And then pass it its value.
	if (node.isLeaf) {
	    return node.KVMap.get(key); //Will return null if key doesnt exist. Will return the value if key DOES exist.
	} else {
	    for (int i = 1; i <= node.numKeys; i++) { //numKeys is basically the size of the keys array (num of keys in array)
		if (key.compareTo(node.keys[i]) < 0) { 
		    return find (key, node.children[i-1]); //Returns the last element of the children array? from slides
		}
	    }
	    return find(key, node.children[node.numKeys]);
	}
    }



    /**
     * Stores the value associated with the key in the B+ tree.
     * If the key is already present, replaces the associated value.
     * If the key is not present, adds the key with the associated value
     * @param value
     * @param key 
     * @return whether pair was successfully added.
     */
    public boolean put(String key, int value){

	if (root == null) {
	    //Creates a new leaf node, adds its key and value to it and then sets the leaf as the root node.
	    StrToIntNode leaf = new StrToIntNode(true, this.maxSize);
	    leaf.KVMap.put(key, value);
	    this.root = leaf;

	    return true; //Returns true as the pair have been successfully added
	} else {
	    StrToIntPairNode node = add(key, value, root);

	    if (node != null) {
		StrToIntNode newNode = new StrToIntNode(false, this.maxSize);
		newNode.numKeys = 1;
		newNode.children[0] = root;
		newNode.keys[1] = node.key;
		newNode.children[1] = node.node;
		this.root = newNode;


	    }
	    return true;
	}
    }

    public StrToIntPairNode add(String key, int value, StrToIntNode node) {
	if (node.isLeaf){
	    if (node.numKeys < this.maxLeafKeys) {
		node.KVMap.put(key, value);
		return null;
	    } else {
		return splitLeaf(key, value, node);
	    }
	}

	//Since its not a leaf, its an internal node and therefore needs to go thru the following loop!
	for(int i = 1; i <= node.numKeys; i++) {
	    if (key.compareTo(node.keys[i]) < 0) {
		StrToIntPairNode node1 = add(key, value, node.children[i-1]);

		if (node1 == null) {
		    return null;
		} else {
		    return dealWithPromote(node1.key, node1.node, node);
		}
	    }
	}

	StrToIntPairNode node2 = add(key, value, node.children[node.numKeys]);
	if (node2 == null) {
	    return null;
	} else {
	    return dealWithPromote(node2.key, node2.node, node);
	}
    }

    public StrToIntPairNode splitLeaf(String key, int value, StrToIntNode node) {
	node.KVMap.put(key, value); 
	StrToIntNode sibling = new StrToIntNode(true, this.maxSize);

	int mid = (node.numKeys + 1)/2;

	ArrayList<String> keyList = new ArrayList<String>(); //Used for removing entries from tree map after copying is done.

	//Copies Keys into sibling node.
	int i = 0;
	for(Map.Entry<String,Integer> entry : node.KVMap.entrySet()) {
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

	return (new StrToIntPairNode(sibling.keys[0], sibling));
    }

    public StrToIntPairNode dealWithPromote(String newKey, StrToIntNode rightChild, StrToIntNode node) {
	if (rightChild == null) {
	    return null;
	}

	if (newKey.compareTo(node.keys[node.numKeys]) > 0) {
	    node.keys[node.numKeys+1] = newKey;
	    node.children[node.numKeys+1] = rightChild;
	} else {
	    for (int i = 1; i <= node.numKeys; i++) {
		if (newKey.compareTo(node.keys[i]) < 0) {
		    //Add key and add child method do the shifting of the arrays too!
		    node.addKey(i, newKey);
		    node.addChild(i, rightChild);
		}
	    }
	}

	// No need to promote further
	if(node.numKeys <= this.maxSize) {
	    return null;
	}

	StrToIntNode sibling = new StrToIntNode(false, this.maxSize); //Create a new internal node.
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

	String promoteKey = node.keys[mid];
	node.keys[mid] = null;

	return (new StrToIntPairNode(promoteKey, sibling));
    }

    public void printAll() {
	StrToIntNode leaf = root; //Set leaf as root for now.

	//Keep going left until you find the leftmost left node.
	while(!leaf.isLeaf){
	    leaf = leaf.children[0];
	}

	while(leaf != null){
	    for(Map.Entry<String,Integer> entry : leaf.KVMap.entrySet()) {
		System.out.println("HOST: " + entry.getKey() + "\t\t\t\t\t IP: " + DNSDB.IPToString(entry.getValue()));
	    }
	    leaf = leaf.next;
	}
    }

}
