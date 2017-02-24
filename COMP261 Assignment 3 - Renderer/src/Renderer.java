import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Renderer extends GUI{

    //FIELDS
    private Vector3D lightSource;
    private ArrayList<Triangle> triangles;

    public static void main(String[] args) {
	new Renderer();
    }



    protected void onLoad(File file) {
	try {
	    this.triangles = new ArrayList<Triangle>();

	    BufferedReader reader = new BufferedReader(new FileReader(file));

	    //Sets lightsource vector from first line
	    String[] lightDirectionValues = reader.readLine().split(" ");
	    this.lightSource = new Vector3D(Float.parseFloat(lightDirectionValues[0]), Float.parseFloat(lightDirectionValues[1]), Float.parseFloat(lightDirectionValues[2]));
	    //System.out.println("Light Source: " + Float.parseFloat(lightDirectionValues[0])+ " " + Float.parseFloat(lightDirectionValues[1])+ " " + Float.parseFloat(lightDirectionValues[2]));

	    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
		String[] values = line.split(" "); //Split values of line into array 

		Vector3D v1 = new Vector3D(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
		//System.out.println(Float.parseFloat(values[0]) + " " + Float.parseFloat(values[1]) + " " +  Float.parseFloat(values[2])); //Prints v1 values

		Vector3D v2 = new Vector3D(Float.parseFloat(values[3]), Float.parseFloat(values[4]), Float.parseFloat(values[5]));
		//System.out.println(Float.parseFloat(values[3]) + " " + Float.parseFloat(values[4]) + " " +  Float.parseFloat(values[5])); //Prints v2 values

		Vector3D v3 = new Vector3D(Float.parseFloat(values[6]), Float.parseFloat(values[7]), Float.parseFloat(values[8]));
		//System.out.println(Float.parseFloat(values[6]) + " " + Float.parseFloat(values[7]) + " " +  Float.parseFloat(values[8])); //Prints v3 values

		this.triangles.add(new Triangle(v1, v2, v3, Integer.parseInt(values[9]), Integer.parseInt(values[10]), Integer.parseInt(values[11])));
		//System.out.println(Integer.parseInt(values[9])+ " " + Integer.parseInt(values[10])+ " " + Integer.parseInt(values[11])); //Prints rgb values
	    }
	    reader.close();
	} catch (FileNotFoundException exception) {
	    System.out.println("File not found");
	} catch (IOException exception) {
	    System.out.println(exception);
	}

	//this.scaleTriangles(); //Scales triangles but isnt needed?
    }



    protected void onKeyPress(KeyEvent ev) {
	if (ev.getKeyCode() == KeyEvent.VK_LEFT|| Character.toUpperCase(ev.getKeyChar()) == 'A'){

	    Transform t = Transform.newYRotation(50f);
	    for (int i = 0; i < this.triangles.size(); i++) {
		Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
		Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
		Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

		this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	    }
	}
	else if (ev.getKeyCode() == KeyEvent.VK_RIGHT|| Character.toUpperCase(ev.getKeyChar()) == 'D'){

	    Transform t = Transform.newYRotation(-50f);
	    for (int i = 0; i < this.triangles.size(); i++) {
		Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
		Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
		Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

		this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	    }
	}
	else if (ev.getKeyCode() == KeyEvent.VK_DOWN|| Character.toUpperCase(ev.getKeyChar()) == 'W'){
	    Transform t = Transform.newXRotation(50f);

	    for (int i = 0; i < this.triangles.size(); i++) {
		Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
		Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
		Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

		this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	    }
	}
	else if (ev.getKeyCode() == KeyEvent.VK_UP|| Character.toUpperCase(ev.getKeyChar()) == 'S'){
	    Transform t = Transform.newXRotation(-50f);

	    for (int i = 0; i < this.triangles.size(); i++) {
		Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
		Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
		Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

		this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	    }
	}
    }

    protected BufferedImage render() {
	Color[][] zbufferC = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
	Float[][] zbufferD = new Float[CANVAS_WIDTH][CANVAS_WIDTH];

	zbufferC = this.initialiseZbufferC(zbufferC); //Initialises the zbuffer to a default color
	zbufferD = this.initialiseZbufferD(zbufferD);

	if (this.triangles == null) {
	    return convertBitmapToImage(zbufferC);
	}


	this.scaleTriangles();
	this.translateTriangles();

	for (Triangle t : this.triangles){ 
	    //Skipping hidden polygons
	    if (t.getNormal().z > 0){
		continue;
	    }

	    float[][] edgelist = t.createEdgeList();
	    Color shading = this.getShading(t);

	    for (int y = 0; y < edgelist.length; y++){
		int x = Math.round(edgelist[y][0]);
		float z = edgelist[y][1];

		float mz = (edgelist[y][3]-edgelist[y][1])/(edgelist[y][2]-edgelist[y][0]);

		while (x <= Math.round(edgelist[y][2])){
		    //if (z < zbufferD[x][y]) {
		    if(x >= 0 && x < GUI.CANVAS_WIDTH && z < zbufferD[x][y]) {
			zbufferD[x][y] = z;
			zbufferC[x][y] = shading;
		    }
		    x++;
		    z = z + mz;
		}
	    }
	}
	// render the bitmap to the image so it can be displayed (and saved)
	return convertBitmapToImage(zbufferC);
    }

    public void translateTriangles(){
	float[] boundingBox = this.modelBounds();

	float modelMinX = boundingBox[0];
	float modelMinY = boundingBox[2];

	float chX = 0f - modelMinX;
	float chY = 0f - modelMinY;

	Transform t = Transform.newTranslation(chX, chY, 0);

	for (int i = 0; i < this.triangles.size(); i++) {
	    Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
	    Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
	    Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

	    this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	}


    }

    public void scaleTriangles() {
	float[] boundingBox = this.modelBounds();

	float canvasWidth = CANVAS_WIDTH;
	float canvasHeight = CANVAS_HEIGHT;
	float modelWidth = boundingBox[1] - boundingBox[0];
	float modelHeight = boundingBox[3] - boundingBox[2];

	float scale = Math.min(canvasWidth/modelWidth, canvasHeight/modelHeight);


	Transform t = Transform.newScale(scale, scale, scale);

	for (int i = 0; i < this.triangles.size(); i++) {
	    Vector3D v1 = t.multiply(this.triangles.get(i).getV1());
	    Vector3D v2 = t.multiply(this.triangles.get(i).getV2());
	    Vector3D v3 = t.multiply(this.triangles.get(i).getV3());

	    this.triangles.set(i, (new Triangle(v1, v2, v3, this.triangles.get(i).getR(), this.triangles.get(i).getG(), this.triangles.get(i).getB())));
	}
    }



    public float[] modelBounds(){
	float minX = Float.POSITIVE_INFINITY;
	float maxX = Float.NEGATIVE_INFINITY;
	float minY = Float.POSITIVE_INFINITY;
	float maxY = Float.NEGATIVE_INFINITY;

	for(Triangle t : this.triangles) {
	    if (t.minX() < minX) {
		minX = t.minX();
	    }

	    if (t.maxX() > maxX) {
		maxX = t.maxX();
	    }

	    if (t.minY() < minY) {
		minY = t.minY();
	    }

	    if (t.maxY() > maxY) {
		maxY = t.maxY();
	    }
	}

	float[] toReturn = new float[4];
	toReturn[0] = minX;
	toReturn[1] = maxX;
	toReturn[2] = minY;
	toReturn[3] = maxY;

	return toReturn;
    }


    public Color getShading(Triangle t){
	Vector3D lightDirection = this.lightSource;
	Vector3D faceNormal = t.getNormal();
	float costh = faceNormal.cosTheta(lightDirection);


	float ambientR = getAmbientLight()[0]/255f;
	float ambientG = getAmbientLight()[1]/255f;
	float ambientB = getAmbientLight()[2]/255f;

	float lightIntensity = getLightIntensity()/100f;

	float r = checkColorBounds((ambientR + lightIntensity * costh) * (t.getR()/255f));
	float g = checkColorBounds((ambientG + lightIntensity * costh) * (t.getG()/255f));
	float b = checkColorBounds((ambientB + lightIntensity * costh) * (t.getB()/255f));

	return new Color(r, g, b);
    }

    public float checkColorBounds(float f) {
	if (f < 0) {
	    return 0;    
	} 
	else if (f > 1){
	    return 1;
	}
	return f;
    }


    //This method Initialises the zbuffer to a default color
    public Color[][] initialiseZbufferC(Color[][] zbufferC){
	for (int row = 0; row < zbufferC.length; row++){
	    for (int col = 0; col < zbufferC[0].length; col++){
		zbufferC[row][col] = Color.GRAY;
	    }
	}
	return zbufferC;
    }

    public Float[][] initialiseZbufferD (Float[][] zbufferD) {
	for (int row = 0; row < zbufferD.length; row++){
	    for (int col = 0; col < zbufferD[0].length; col++){
		zbufferD[row][col] = Float.POSITIVE_INFINITY;
	    }
	}
	return zbufferD; //returns the zbuffer 2d array
    }


    /**
     * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
     * indexed by column then row and has imageHeight rows and imageWidth
     * columns. Note that image.setRGB requires x (col) and y (row) are given in
     * that order.
     */
    private BufferedImage convertBitmapToImage(Color[][] bitmap) {
	BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT,
		BufferedImage.TYPE_INT_RGB);
	for (int x = 0; x < CANVAS_WIDTH; x++) {
	    for (int y = 0; y < CANVAS_HEIGHT; y++) {
		image.setRGB(x, y, bitmap[x][y].getRGB());
	    }
	}
	return image; //returns in image format
    }
}
