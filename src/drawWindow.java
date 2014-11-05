import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class drawWindow extends JPanel implements MouseListener
{

	private static final long serialVersionUID = 1L;
	private static final int CIRCLE_DIAMETER = 100;
	private static final int STATE_POSITION = 105;

	int screenWidth;
	int screenHeight;
	RenderingHints rh;
	
	State m_State;
	Trial m_Trial;
	int m_iCurrentTrialStep;
	ArrayList<Target> m_aTargets;
	
	Timer m_Timer;
	Button restartButton;
	Button quitButton;
	Button saveButton;
	
	AudioClip correctSound;
	
	long m_lStartTime;
	int m_iGlobalTimer;
	
	public drawWindow()
	{
		screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		setBackground(Color.BLACK);
		setSize((int)screenWidth,(int)screenHeight);
		addMouseListener(this);

		try {
			restartButton = new Button(ImageIO.read(getClass().getResource("images/restartButton.png")), 5, 5);
			saveButton = new Button(ImageIO.read(getClass().getResource("images/saveButton.png")), 100, 5);
			quitButton = new Button(ImageIO.read(getClass().getResource("images/quitButton.png")), 195, 5);
			correctSound = Applet.newAudioClip(getClass().getResource("sounds/correctSound.wav"));
		} catch (IOException e) {e.printStackTrace();}
        
		
		Reset();
	}
	private void Reset()
	{
		if (m_Timer != null)
			m_Timer.cancel();
		
		m_State = State.READY;
		m_aTargets = new ArrayList<Target>();
		m_Trial = new Trial();
		m_Timer = new Timer();;
		m_iCurrentTrialStep = 0;
		m_iGlobalTimer = 0;
		
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				Target myTarget = new Target((1+i*2)*100/12, (1+j*2)*100/12);
				m_aTargets.add(myTarget);
			}
		}
		
		m_aTargets.get(0).setFill(true);
	}
    private void doDrawing(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        
        rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        
        switch(m_State)
        {
	        case READY:
	        	g2d.fillOval((int)(screenWidth * 0.1), (int)(screenHeight * 0.5), CIRCLE_DIAMETER, CIRCLE_DIAMETER);
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
        
        g2d.setColor(Color.YELLOW);
        for (int i = 0; i < m_aTargets.size(); i++)
        {
        	if (m_aTargets.get(i).isFill())
        	{
        		System.out.println("FOUND CIRCLE TO DRAW at: (" + m_aTargets.get(i).getX() + ", " + m_aTargets.get(i).getY() + ")");
        		g2d.fillOval(m_aTargets.get(i).getX() * screenWidth/100, m_aTargets.get(i).getY() * screenHeight/100, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        	}
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
		else if (m_State == State.WAIT_FOR_PRESS)
		{
			CheckClick(x, y, m_Trial.getElementAt(m_iCurrentTrialStep));
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
				String exportString = m_Trial.getExportString();
				
				writer.println(exportString);
				writer.close();
			}
			catch (FileNotFoundException e) {e.printStackTrace();}
			catch (UnsupportedEncodingException e) {e.printStackTrace();}
		}
	}
	private void CheckClick(int x, int y, int TargetID)
	{
		//int z = (int) Math.sqrt(Math.pow((x1+m_iCircleDiameter/2-x), 2) + Math.pow((y1+m_iCircleDiameter/2-y), 2));
		int currentTarget = m_iCurrentTrialStep;
		
		int z = 1;
		if ( z < CIRCLE_DIAMETER/2)
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
