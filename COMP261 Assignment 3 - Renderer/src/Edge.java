
public class Edge {
    public final Vector3D a;
    public final Vector3D b;
    public final Triangle triangle;

    public Edge(Triangle triangle, Vector3D a, Vector3D b) {
	this.triangle = triangle;
	this.a = a;
	this.b = b;
    }

    public Vector3D getMinYVertex() {
	if (a.y < b.y) {
	    return a;
	} 
	return b;
    }

    public Vector3D getOtherVertex() {
	if (a.y < b.y) {
	    return b;
	}
	return a;
    }
}