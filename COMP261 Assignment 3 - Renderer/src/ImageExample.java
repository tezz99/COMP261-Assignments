import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A simple example of how to extend the GUI class. The converBitmapToImage
 * method is particularly useful.
 */
public class ImageExample extends GUI {

    private double shift = 1.0;

    @Override
    protected void onLoad(File file) {
	System.out.println("no files loaded in this example.");
    }

    @Override
    protected void onKeyPress(KeyEvent ev) {
	if (ev.getKeyCode() == KeyEvent.VK_LEFT
		|| Character.toUpperCase(ev.getKeyChar()) == 'A')
	    shift += 0.1;
	else if (ev.getKeyCode() == KeyEvent.VK_RIGHT
		|| Character.toUpperCase(ev.getKeyChar()) == 'D')
	    shift -= 0.1;
    }

    @Override
    protected BufferedImage render() {
	Color[][] bitmap = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];

	// make the bitmap of smoothly changing colors;
	// Your program should render a model
	for (int x = 0; x < CANVAS_WIDTH; x++) {
	    for (int y = 0; y < CANVAS_HEIGHT; y++) {
		float hue = (float) Math.sin((x + Math.pow(y, shift))
			/ (CANVAS_WIDTH));
		bitmap[x][y] = Color.getHSBColor(hue, 1.0f, 1.0f);
	    }
	}

	// render the bitmap to the image so it can be displayed (and saved)
	return convertBitmapToImage(bitmap);
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
	return image;
    }

    public static void main(String[] args) {
	new ImageExample();
    }

}

// code for COMP261 assignments