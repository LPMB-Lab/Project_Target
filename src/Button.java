import java.awt.image.BufferedImage;

public class Button
{
	private int x;
	private int y;
	private int width;
	private int height;
	private BufferedImage image;
	
	Button(BufferedImage image, int x, int y)
	{
		this.x = x;
		this.y = y;
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

	public int getX() {return x;}
	public int getY() {return y;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public BufferedImage getImage() {return image;}
	public boolean isPressed(int pressX, int pressY)
	{
		if (pressX < x + width && pressX > x)
			if (pressY < y + height && pressY > y)
				return true;
		
		return false;
	}
}
