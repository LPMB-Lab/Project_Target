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
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextField;

class drawWindow extends JPanel implements MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int m_iCircleDiameter = 100;
	private static final int STATE_POSITION = 105;
	private static final int TOTAL_TRIALS = 36;

	Dimension screenSize;
	RenderingHints rh;
	
	State m_State;
	State m_RecoveryState;
	Vector<Trial> m_vGeneratedTrials;
	Trial m_CurrentTrial;
	int m_iCurrentTrial;
	int m_iCurrentTrialStep;
	
	Timer m_Timer;
	Button startButton;
	Button restartButton;
	Button quitButton;
	Button saveButton;
	
	AudioClip correctSound;
	
	long m_lStartTime;
	JTextField m_TextBox;
	int m_iGlobalTimer;
	
	public drawWindow()
	{
		m_TextBox = new JTextField(10);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBackground(Color.BLACK);
		setSize((int)screenSize.getWidth(),(int)screenSize.getHeight());
		addMouseListener(this);
		add(m_TextBox);
		
		m_vGeneratedTrials = new Vector<Trial>();

		try {
			startButton = new Button(ImageIO.read(getClass().getResource("images/startButton.png")), 5, 5);
			restartButton = new Button(ImageIO.read(getClass().getResource("images/restartButton.png")), 100, 5);
			quitButton = new Button(ImageIO.read(getClass().getResource("images/quitButton.png")), 195, 5);
			saveButton = new Button(ImageIO.read(getClass().getResource("images/saveButton.png")), 290, 5);
			correctSound = Applet.newAudioClip(getClass().getResource("sounds/correctSound.wav"));
		} catch (IOException e) {e.printStackTrace();}
        
		
		Reset();
	}
	private void Reset()
	{
		if (m_Timer != null)
			m_Timer.cancel();
		
		m_State = State.IDLE;
		m_Timer = new Timer();
		m_vGeneratedTrials.clear();
		m_iCurrentTrial = 0;
		m_iCurrentTrialStep = 0;
		m_iGlobalTimer = 0;

		for (int i = 0; i < TOTAL_TRIALS; i++)
		{
			Trial myTrial = new Trial();
			m_vGeneratedTrials.add(myTrial);
		}
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
	        case IDLE:
	        	g2d.drawImage(startButton.getImage(), startButton.getX(),  startButton.getY(),  null);
	        	break;
	        case FINGER_TRACKING:
	        	g2d.drawString("FINGER TRACKING", 5, STATE_POSITION);
	        	break;
	        case COUNTDOWN:
	        	g2d.drawString("Countdown to begin in " + m_iGlobalTimer + " seconds", 5, STATE_POSITION);
	        case IN_TRIAL:
	        	break;
	        case COMPLETED:
	        	g2d.drawString("The test is complete! Thank you for participating!", 5, STATE_POSITION);
	        	break;
        }
        
        g2d.drawString("CURRENT TRIAL: " + (m_iCurrentTrial + 1) + "/40", 5, 75);
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
					m_Timer.schedule(new updateTask(State.IN_TRIAL), 1000);
					m_iGlobalTimer--;
				}
			}
			
			if (m_State == State.IN_TRIAL)
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
		
		if (startButton.isPressed(x, y))		{StartSimulation();}
		else if (quitButton.isPressed(x, y))	{System.exit(0);}
		else if (restartButton.isPressed(x,  y)){Reset();}
		else if(saveButton.isPressed(x,  y))	{ExportFile();}
		else
		{
			switch(m_State)
	        {
		        case FINGER_TRACKING:
		        {
	        	}
		        case IN_TRIAL:
		        {
		        	m_CurrentTrial = m_vGeneratedTrials.get(m_iCurrentTrial);
		        	int fingerIndex = m_CurrentTrial.getCurrentFinger(m_iCurrentTrialStep);
		        	
		        	switch (fingerIndex)
		        	{
		    	    	case -4:CheckClick(x, y, 0);	break;
		    	    	case -3: CheckClick(x, y, 1);	break;
		    	    	case -2: CheckClick(x, y, 2);	break;
		    	    	case -1: CheckClick(x, y, 3);	break;
		    	    	case 1: CheckClick(x, y, 4);	break;
		    	    	case 2: CheckClick(x, y, 5);	break;
		    	    	case 3: CheckClick(x, y, 6);	break;
		    	    	case 4: CheckClick(x, y, 7);	break;
		        	}
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
	private void StartSimulation()
	{
		m_State = State.FINGER_TRACKING;
	}
	private void ExportFile()
	{
		try
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
			Date date = new Date();
			String fileName = "";
			
			if (m_TextBox.getText().equals(""))
				fileName = dateFormat.format(date) + "_NON_NAMED_TRIAL" + ".txt";
			else
				fileName = dateFormat.format(date) + "_" + m_TextBox.getText() + ".txt";
			
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			String exportString = "";
			
			for (int i = 0; i < m_vGeneratedTrials.size(); i++)
			{
				exportString += "TRIAL #" + i + "\r\n";
				exportString += m_vGeneratedTrials.get(i).ExportTrial();
				exportString += "Average InterSwitch Time: " + m_vGeneratedTrials.get(i).getAvgInterSwitchTime() + "\r\n";
				exportString += "Average IntraSwitch Time: " + m_vGeneratedTrials.get(i).getAvgIntraSwitchTime() + "\r\n";
				exportString += "Fastest Time: " + m_vGeneratedTrials.get(i).getFastestTime() + "\r\n";
				exportString += "\r\n";
			}
			
			writer.println(exportString);
			writer.close();
		}
		catch (FileNotFoundException e) {e.printStackTrace();}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
	}
	private void CheckClick(int x, int y, int fingerID)
	{
		//int z = (int) Math.sqrt(Math.pow((x1+m_iCircleDiameter/2-x), 2) + Math.pow((y1+m_iCircleDiameter/2-y), 2));
		int z = 1;
		if ( z < m_iCircleDiameter/2)
		{
			long lEndTime = new Date().getTime();
			long diffTime = lEndTime - m_lStartTime;
			
			m_CurrentTrial = m_vGeneratedTrials.get(m_iCurrentTrial);
			m_CurrentTrial.setTimer(m_iCurrentTrialStep, diffTime);
			
			if (m_iCurrentTrialStep == 19)
			{
				if (m_iCurrentTrial == 39)
				{
					m_State = State.COMPLETED;
					ExportFile();
				}
				else
				{
					m_iCurrentTrial++;
					m_iCurrentTrialStep = 0;
					
					if (m_iCurrentTrial == 19)
						countDownToState(60, State.IN_TRIAL);
					else
						countDownToState(5, State.IN_TRIAL);
				}
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
