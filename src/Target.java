public class Target {
    private int m_iX;
    private int m_iY;
    private boolean m_bFill;

    public Target(int x, int y) {
        m_iX = x;
        m_iY = y;
        m_bFill = false;
    }

    public int getX() {
        return m_iX;
    }

    public int getY() {
        return m_iY;
    }

    public boolean isFill() {
        return m_bFill;
    }

    public void setY(int y) {
        m_iY = y;
    }

    public void setX(int x) {
        m_iX = x;
    }

    public void setFill(boolean fill) {
        m_bFill = fill;
    }
}
