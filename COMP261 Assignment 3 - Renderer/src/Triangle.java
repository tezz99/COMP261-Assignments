

public class Triangle {

    //Fields
    private Vector3D v1;
    private Vector3D v2;
    private Vector3D v3;
    private int r, g, b;

    //private ArrayList<Edge> edges = new ArrayList<Edge>(); //All the edges
    private Edge[] edges;

    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3, int r, int g, int b){
	this.setV1(v1);
	this.setV2(v2);
	this.setV3(v3);
	this.setR(r);
	this.setG(g);
	this.setB(b);


	this.edges = new Edge[3];
	this.edges[0] = new Edge(this, v1, v2);
	this.edges[1] = new Edge(this, v2, v3);
	this.edges[2] = new Edge(this, v3, v1);

    }

    public Vector3D getNormal() {	
	return (this.v2.minus(this.v1)).crossProduct((this.v3.minus(this.v2)));
    }

    public float[][] createEdgeList(){
	float[][] edgelist = new float[GUI.CANVAS_HEIGHT][4];
	this.initialiseEdgeList(edgelist);

	for (Edge e : this.edges){
	    Vector3D vertexA = e.getMinYVertex();
	    Vector3D vertexB = e.getOtherVertex();

	    //Slopes
	    float mx = (vertexB.x - vertexA.x)/(vertexB.y - vertexA.y); 
	    float mz = (vertexB.z - vertexA.z)/(vertexB.y - vertexA.y);

	    float x = vertexA.x;
	    float z = vertexA.z;

	    int i = Math.round(vertexA.y);
	    int maxi = Math.round(vertexB.y);

	    do {
		if (i < edgelist.length && x < edgelist[i][0]) {
		    edgelist[i][0] = x;
		    edgelist[i][1] = z;
		}

		if (i < edgelist.length && x > edgelist[i][2]){
		    edgelist[i][2] = x;
		    edgelist[i][3] = z;
		}

		i++;
		x = x + mx;
		z = z + mz;

	    } while (i < maxi);

	}
	return edgelist;
    }

    //Initialises edgelist to infinity, infinity, negative infinity, infinity for all rows.
    public void initialiseEdgeList(float[][] edgelist){
	for (int row = 0; row < edgelist.length; row++){
	    for (int col = 0; col < edgelist[0].length; col++){
		if (col == 0 || col == 1 || col == 3){
		    edgelist[row][col] = Float.POSITIVE_INFINITY;
		}
		else{
		    edgelist[row][col] = Float.NEGATIVE_INFINITY;
		}
	    }
	}
    }



    public float minX() {
	float min1 = Math.min(v1.x, v2.x);
	float minX = Math.round(Math.min(min1, v3.x));
	return minX;

    }

    public float maxX() {
	float max1 = Math.max(v1.x, v2.x);
	float maxX = Math.round(Math.max(max1, v3.x));
	return maxX;
    }

    public float minY(){
	float min1 = Math.min(v1.y, v2.y);
	float minY = Math.round(Math.min(min1, v3.y));
	return minY;
    }

    public float maxY(){
	float max1 = Math.max(v1.y, v2.y);
	float maxY = Math.round(Math.max(max1, v3.y));
	return maxY;
    }

    //Getter and Setter Methods!
    public Vector3D getV1() {
	return this.v1;
    }

    public void setV1(Vector3D v1) {
	this.v1 = v1;
    }

    public Vector3D getV2() {
	return this.v2;
    }

    public void setV2(Vector3D v2) {
	this.v2 = v2;
    }

    public Vector3D getV3() {
	return this.v3;
    }

    public void setV3(Vector3D v3) {
	this.v3 = v3;
    }


    public int getG() {
	return g;
    }


    public void setG(int g) {
	this.g = g;
    }


    public int getR() {
	return r;
    }


    public void setR(int r) {
	this.r = r;
    }


    public int getB() {
	return b;
    }


    public void setB(int b) {
	this.b = b;
    }
}
