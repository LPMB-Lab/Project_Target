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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class drawWindow extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	private static final int CIRCLE_DIAMETER = 100;
	private static final int STATE_POSITION = 105;
	private static final int LENGTH_TARGETS = 6;
	private static final int WIDTH_TARGETS = 6;

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

	long m_lStartTime;
	int m_iGlobalTimer;
	double m_iPoints;
	boolean m_bIsCheat;

	public drawWindow() {
		screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		setBackground(Color.BLACK);
		setSize(screenWidth, screenHeight);
		addMouseListener(this);

		try {
			restartButton = new Button(ImageIO.read(getClass().getResource(
					"images/restartButton.png")), 5, 5);
			saveButton = new Button(ImageIO.read(getClass().getResource(
					"images/saveButton.png")), 100, 5);
			quitButton = new Button(ImageIO.read(getClass().getResource(
					"images/quitButton.png")), 195, 5);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Reset();
	}

	private void Reset() {
		if (m_Timer != null)
			m_Timer.cancel();

		m_State = State.READY;
		m_aTargets = new ArrayList<Target>();
		m_Trial = new Trial();
		m_Timer = new Timer();
		m_iCurrentTrialStep = 0;
		m_iGlobalTimer = 0;
		m_iPoints = 0;

		for (int i = 0; i < WIDTH_TARGETS; i++) {
			for (int j = 0; j < LENGTH_TARGETS; j++) {
				Target myTarget = new Target((1 + i * 2) * 100
						/ (WIDTH_TARGETS * 2), (1 + j * 2) * 100
						/ (LENGTH_TARGETS * 2));
				m_aTargets.add(myTarget);
			}
		}
	}

	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHints(rh);

		g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30));

		switch (m_State) {
		case READY:
			g2d.setColor(Color.GREEN);
			break;
		case COUNTDOWN:
		case WAIT_FOR_PRESS:
			g2d.setColor(Color.GRAY);
			break;
		case COMPLETED:
			g2d.drawString(
					"The test is complete! Thank you for participating!", 5,
					STATE_POSITION + 100);
			break;
		default:
			break;
		}

		// Create Start Circle
		g2d.fillOval((int) (screenWidth * 0.1 - CIRCLE_DIAMETER),
				(int) (screenHeight * 0.5 - CIRCLE_DIAMETER), CIRCLE_DIAMETER,
				CIRCLE_DIAMETER);
		
		// Create Labels
		g2d.setColor(Color.BLUE);
		g2d.drawString("CURRENT TARGET: " + (m_iCurrentTrialStep + 1) + "/36",
				5, 75);
		g2d.drawString("POINTS: " + m_iPoints, 5, 105);
		
		// Creating Buttons
		g2d.drawImage(restartButton.getImage(), restartButton.getX(),
				restartButton.getY(), null);
		g2d.drawImage(quitButton.getImage(), quitButton.getX(),
				quitButton.getY(), null);
		g2d.drawImage(saveButton.getImage(), saveButton.getX(),
				saveButton.getY(), null);

		// Create targets but only fill the one that is required to be
		g2d.setColor(Color.YELLOW);
		for (int i = 0; i < m_aTargets.size(); i++) {
			if (m_aTargets.get(i).isFill()) {
				g2d.fillOval((int) (m_aTargets.get(i).getX()
						* (screenWidth - screenWidth * 0.25) / 100
						- CIRCLE_DIAMETER / 2 + screenWidth * 0.25), m_aTargets
						.get(i).getY()
						* screenHeight
						/ 100
						- CIRCLE_DIAMETER
						/ 2, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
			}
		}
	}

	class updateTask extends TimerTask {
		State state;

		updateTask(State state) {
			this.state = state;
		}

		@Override
		public void run() {
			if (m_State != State.COUNTDOWN) {
				m_State = state;
				System.out.println("STATE IS: " + m_State);
			}

			if (m_State == State.COUNTDOWN) {
				if (m_iGlobalTimer <= 0)
					m_State = state;
				else {
					m_Timer.schedule(new updateTask(state), 100);
					m_iGlobalTimer -= 100;
				}
			}

			if (m_State == State.WAIT_FOR_PRESS) {
				m_aTargets.get(m_Trial.getElementAt(m_iCurrentTrialStep))
						.setFill(true);
				UpdateGraphics();
				m_lStartTime = new Date().getTime();
				System.out.println("WAIT FOR PRESS");
			}
		}
	}

	private void countDownToState(int timer, State state) {
		m_iGlobalTimer = timer;
		m_State = State.COUNTDOWN;
		m_Timer.schedule(new updateTask(state), 100);
	}

	private void clearCircles() {
		for (int i = 0; i < m_aTargets.size(); i++)
			m_aTargets.get(i).setFill(false);
	}

	private boolean isReadyPressed(int x, int y) {
		System.out.println("Ready Pressed");
		if ((int) Math
				.sqrt(Math.pow((x + CIRCLE_DIAMETER / 2 - (screenWidth * 0.1)),
						2)
						+ Math.pow(
								(y + CIRCLE_DIAMETER / 2 - (screenHeight * 0.5)),
								2)) < CIRCLE_DIAMETER / 2)
			return true;
		else
			return false;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (quitButton.isPressed(x, y)) {
			System.exit(0);
		} else if (restartButton.isPressed(x, y)) {
			Reset();
		} else if (saveButton.isPressed(x, y)) {
			ExportFile();
		} else if (m_State == State.READY) {
			if (isReadyPressed(e.getX(), e.getY())) {
				m_bIsCheat = false;
				countDownToState(new Random().nextInt(1800) + 200,
						State.WAIT_FOR_PRESS);
			}

		} else if (m_State == State.WAIT_FOR_PRESS) {
			CheckClick(x, y, m_Trial.getElementAt(m_iCurrentTrialStep));
		}

		repaint();
	}

	private void UpdateGraphics() {
		Graphics g;
		g = getGraphics();
		paint(g);
	}

	private void ExportFile() {
		if (m_State == State.COMPLETED) {
			try {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy_MM_dd HH_mm_ss");
				Date date = new Date();
				String fileName = "";

				fileName = dateFormat.format(date) + "_NON_NAMED_TRIAL"
						+ ".txt";

				PrintWriter writer = new PrintWriter(fileName, "UTF-8");
				String exportString = m_Trial.getExportString();

				writer.println(exportString);
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Trial is not yet finished");
		}
	}

	private void CheckClick(int x, int y, int TargetID) {
		int xTarget = (int) (m_aTargets.get(TargetID).getX()
				* (screenWidth - screenWidth * 0.25) / 100 + screenWidth * 0.25);
		int yTarget = m_aTargets.get(TargetID).getY() * screenHeight / 100;
		long lEndTime = new Date().getTime();
		long diffTime = lEndTime - m_lStartTime;
		m_Trial.setTimer(m_iCurrentTrialStep, diffTime);
		int z = (int) Math.sqrt(Math.pow(x - xTarget, 2)
				+ Math.pow(y - yTarget, 2));

		if (diffTime < 2000 && diffTime > 50 && !m_bIsCheat) {
			if (z < CIRCLE_DIAMETER / 2)
				m_iPoints += 1;
			else if (z < CIRCLE_DIAMETER * 1.25 / 2)
				m_iPoints += 0.5;
		}

		if (m_iCurrentTrialStep == LENGTH_TARGETS * WIDTH_TARGETS - 1)
			m_State = State.COMPLETED;
		else {
			m_State = State.READY;
			m_iCurrentTrialStep++;
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (m_State == State.COUNTDOWN) {
			m_bIsCheat = true;
			
			System.out.println("CHEATING");
		}

		clearCircles();
		UpdateGraphics();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}
}

