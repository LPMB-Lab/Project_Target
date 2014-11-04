import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class drawWindow extends JPanel implements MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int m_iCircleDiameter = 100;
	private static final int STATE_POSITION = 105;

	Dimension screenSize;
	RenderingHints rh;
	
	State m_State;
	State m_RecoveryState;
	Trial m_Trial;
	int m_iCurrentTrialStep;
	
	Timer m_Timer;
	Button restartButton;
	Button quitButton;
	Button saveButton;
	
	AudioClip correctSound;
	
	long m_lStartTime;
	int m_iGlobalTimer;
	
	public drawWindow()
	{
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBackground(Color.BLACK);
		setSize((int)screenSize.getWidth(),(int)screenSize.getHeight());
		addMouseListener(this);

		try {
			restartButton = new Button(ImageIO.read(getClass().getResource("images/restartButton.png")), 5, 5);
			quitButton = new Button(ImageIO.read(getClass().getResource("images/quitButton.png")), 100, 5);
			saveButton = new Button(ImageIO.read(getClass().getResource("images/saveButton.png")), 195, 5);
			correctSound = Applet.newAudioClip(getClass().getResource("sounds/correctSound.wav"));
		} catch (IOException e) {e.printStackTrace();}
        
		
		Reset();
	}
	private void Reset()
	{
		if (m_Timer != null)
			m_Timer.cancel();
		
		m_State = State.READY;
		m_Trial = new Trial();
		m_Timer = new Timer();;
		m_iCurrentTrialStep = 0;
		m_iGlobalTimer = 0;
	}
    private void doDrawing(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        
        rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        
        g2d.setColor(Color.blue);
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30)); 
        
        switch(m_State)
        {
	        case READY:
	        	break;
	        case COUNTDOWN:
	        	g2d.drawString("Countdown to begin in " + m_iGlobalTimer + " seconds", 5, STATE_POSITION);
	        case COMPLETED:
	        	g2d.drawString("The test is complete! Thank you for participating!", 5, STATE_POSITION);
	        	break;
		case FINGER_PRESSED:
			break;
		case WAIT_FOR_PRESS:
			break;
		default:
			break;
        }
        
        g2d.drawString("CURRENT TARGET: " + (m_iCurrentTrialStep + 1) + "/36", 5, 75);
        g2d.drawImage(restartButton.getImage(), restartButton.getX(), restartButton.getY(), null);
        g2d.drawImage(quitButton.getImage(), quitButton.getX(), quitButton.getY(), null);
        g2d.drawImage(saveButton.getImage(), saveButton.getX(), saveButton.getY(), null);
        
    }
    class updateTask extends TimerTask
	{
    	State state;
    	
    	updateTask(State state) {this.state = state;}
		public void run()
		{
			if (m_State != State.COUNTDOWN)
			{
				m_State = state;
				System.out.println("STATE IS: " + m_State);
			}
			
			if (m_State == State.COUNTDOWN)
			{
				if (m_iGlobalTimer == 0)
				{
					m_State = state;
				}
				else
				{
					m_Timer.schedule(new updateTask(State.WAIT_FOR_PRESS), 1000);
					m_iGlobalTimer--;
				}
			}
			
			if (m_State == State.WAIT_FOR_PRESS)
			{
				m_lStartTime = new Date().getTime();
				updateTrial();
			}
			
			UpdateGraphics();
		}
	}
    private void updateTrial()
    {	
    	repaint();
    }
    private void countDownToState(int timer, State state)
    {
    	m_iGlobalTimer = timer;
    	m_State = State.COUNTDOWN;
    	m_Timer.schedule(new updateTask(state), 1000);
    }
	@Override
	public void mousePressed(MouseEvent e)
	{	
		int x = e.getX();
		int y = e.getY();
		
		if (quitButton.isPressed(x, y))	{System.exit(0);}
		else if (restartButton.isPressed(x,  y)){Reset();}
		else if(saveButton.isPressed(x,  y))	{ExportFile();}
		else
		{
			switch(m_State)
	        {
		        case WAIT_FOR_PRESS:
		        {
		        	CheckClick(x, y, m_Trial.getElementAt(m_iCurrentTrialStep));
	        		break;
	        	}
			default:
				break;
	        }
		}
		
		repaint();
	}
	private void UpdateGraphics()
	{
		Graphics g;
		g = getGraphics();
		paint(g);
	}
	private void ExportFile()
	{
		if (m_State == State.COMPLETED)
		{
			try
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
				Date date = new Date();
				String fileName = "";
				
				fileName = dateFormat.format(date) + "_NON_NAMED_TRIAL" + ".txt";
				
				PrintWriter writer = new PrintWriter(fileName, "UTF-8");
				String exportString = "";
				
				for (int i = 0; i < m_Trial.getSize(); i++)
				{
					exportString += "TRIAL #" + i + "\r\n";
					exportString += m_Trial.ExportTrial();
					exportString += "Fastest Time: " + m_Trial.getFastestTime() + "\r\n";
					exportString += "\r\n";
				}
				
				writer.println(exportString);
				writer.close();
			}
			catch (FileNotFoundException e) {e.printStackTrace();}
			catch (UnsupportedEncodingException e) {e.printStackTrace();}
		}
	}
	private void CheckClick(int x, int y, int fingerID)
	{
		//int z = (int) Math.sqrt(Math.pow((x1+m_iCircleDiameter/2-x), 2) + Math.pow((y1+m_iCircleDiameter/2-y), 2));
		int z = 1;
		if ( z < m_iCircleDiameter/2)
		{
			long lEndTime = new Date().getTime();
			long diffTime = lEndTime - m_lStartTime;
			
			m_Trial.setTimer(m_iCurrentTrialStep, diffTime);
			
			if (m_iCurrentTrialStep == 35)
			{
				m_State = State.COMPLETED;
			}
			else
			{
				correctSound.play();
				try {Thread.sleep((long)(Math.random()*500 + 500));}
				catch (InterruptedException e) {e.printStackTrace();}
				
				m_iCurrentTrialStep++;
				updateTrial();
			}
			m_lStartTime = new Date().getTime();
		}
	}

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        doDrawing(g);
    }

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}
}
