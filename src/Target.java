public class Target
{
	private int x;
	private int y;
	private boolean fill;
	
	public Target(int x, int y)
	{
		this.x = x;
		this.y = y;
		
		fill = false;
	}

	public int getX() {return x;}
	public int getY() {return y;}
	public boolean isFill() {return fill;}
	public void setY(int y) {this.y = y;}
	public void setX(int x) {this.x = x;}
	public void setFill(boolean fill) {this.fill = fill;}
}
