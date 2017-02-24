/* Class is used for adding to BPLUSTree Int To String */
public class IntToStrPairNode {
    public int key;
    public IntToStringNode node;

    public IntToStrPairNode(int key, IntToStringNode node) {
	//Sets key and right child! Used in add.
	this.key = key;
	this.node = node;
    }
}
