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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

class drawWindow extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	private static final int CIRCLE_DIAMETER = 100;
	// Arbitrary spacing tool for labels
	private static final int STATE_POSITION = 105;
	
	// Length and width of targets (6x6 would mean 36 targets)
	private static final int LENGTH_TARGETS = 6;
	private static final int WIDTH_TARGETS = 6;

	// Variables to hold the screen width/height to allow the program to adjust to resizing
	int screenWidth;
	int screenHeight;
	RenderingHints rh;

	// Store current state, see Enum State
	State m_State;
	
	// Current Trial
	Trial m_Trial;
	
	// Current Trials Step
	int m_iCurrentTrialStep;
	
	// Array to hold Targets
	ArrayList<Target> m_aTargets;

	// Manual timer set to update state times
	Timer m_Timer;
	
	// Buttons
	Button restartButton;
	Button quitButton;
	Button saveButton;

	// Timers for each of the measurements, 
	// Response Timer is how long it takes from finger lift to pressing target
	// Reaction Timer is how long it takes from target show to finger lift
	long m_lResponseStartTime;
	long m_lReactionStartTime;
	
	// Timer for the global timer
	int m_iGlobalTimer;
	
	// Points counter
	double m_iPoints;
	
	// Cheating check
	boolean m_bIsCheat;

	public drawWindow() {
		// Set screen width and height
		screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		setBackground(Color.BLACK);
		setSize(screenWidth, screenHeight);
		addMouseListener(this);

		try {
			restartButton = new Button(ImageIO.read(getClass().getResource("images/restartButton.png")), 5, 5);
			saveButton = new Button(ImageIO.read(getClass().getResource("images/saveButton.png")), 100, 5);
			quitButton = new Button(ImageIO.read(getClass().getResource("images/quitButton.png")), 195, 5);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Reset();
	}

	private void Reset() {
		// Reset Timer
		if (m_Timer != null) {
			m_Timer.cancel();
		}

		// Initial state should be ready
		m_State = State.READY;
		
		// Wipe all current variables
		m_aTargets = new ArrayList<Target>();
		m_Trial = new Trial();
		m_Timer = new Timer();
		m_iCurrentTrialStep = 0;
		m_iGlobalTimer = 0;
		m_iPoints = 0;

		// Generate new targets and add them to target list
		for (int i = 0; i < WIDTH_TARGETS; i++) {
			for (int j = 0; j < LENGTH_TARGETS; j++) {
				Target myTarget = new Target((1 + i * 2) * 100 / (WIDTH_TARGETS * 2),
						(1 + j * 2) * 100 / (LENGTH_TARGETS * 2));
				m_aTargets.add(myTarget);
			}
		}
	}

	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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
				g2d.drawString("The test is complete! Thank you for participating!", 5, STATE_POSITION + 100);
				break;
			default:
				break;
		}

		// Create Start Circle
		g2d.fillOval((int) (screenWidth * 0.1 - CIRCLE_DIAMETER), (int) (screenHeight * 0.5 - CIRCLE_DIAMETER),
				CIRCLE_DIAMETER, CIRCLE_DIAMETER);

		// Create Labels
		g2d.setColor(Color.BLUE);
		g2d.drawString("CURRENT TARGET: " + (m_iCurrentTrialStep + 1) + "/" + LENGTH_TARGETS * WIDTH_TARGETS, 5, 75);
		g2d.drawString("POINTS: " + m_iPoints, 5, 105);

		// Creating Buttons
		g2d.drawImage(restartButton.getImage(), restartButton.getX(), restartButton.getY(), null);
		g2d.drawImage(quitButton.getImage(), quitButton.getX(), quitButton.getY(), null);
		g2d.drawImage(saveButton.getImage(), saveButton.getX(), saveButton.getY(), null);

		// Create targets but only fill the one that is required to be
		g2d.setColor(Color.YELLOW);
		for (int i = 0; i < m_aTargets.size(); i++) {
			if (m_aTargets.get(i).isFill()) {
				g2d.fillOval(
						(int) (m_aTargets.get(i).getX() * (screenWidth - screenWidth * 0.25) / 100 - CIRCLE_DIAMETER / 2
								+ screenWidth * 0.25),
						m_aTargets.get(i).getY() * screenHeight / 100 - CIRCLE_DIAMETER / 2, CIRCLE_DIAMETER,
						CIRCLE_DIAMETER);
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
			
			// Instant switch or something?
			if (m_State != State.COUNTDOWN) {
				m_State = state;
				System.out.println("STATE IS: " + m_State);
			}

			// If the state is still in countdown decrement the global timer
			if (m_State == State.COUNTDOWN) {
				if (m_iGlobalTimer <= 0) {
					// If Global timer ready, switch state
					m_State = state;
				} else {
					// If global timer not ready, decrement by 100 and do it again
					m_Timer.schedule(new updateTask(state), 100);
					m_iGlobalTimer -= 100;
				}
			}

			if (m_State == State.WAIT_FOR_PRESS) {
				m_aTargets.get(m_Trial.getElementAt(m_iCurrentTrialStep)).setFill(true);
				UpdateGraphics();
				
				// Start timer for response/reaction timer as soon as target appears
				m_lResponseStartTime = new Date().getTime();
				m_lReactionStartTime = new Date().getTime();
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
		for (int i = 0; i < m_aTargets.size(); i++) {
			m_aTargets.get(i).setFill(false);
		}
	}

	private boolean isReadyPressed(int x, int y) {
		return ((int) Math.sqrt(Math.pow((x + CIRCLE_DIAMETER / 2 - (screenWidth * 0.1)), 2)
				+ Math.pow((y + CIRCLE_DIAMETER / 2 - (screenHeight * 0.5)), 2)) < CIRCLE_DIAMETER / 2);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (quitButton.isPressed(x, y)) {
			int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Warning",
					JOptionPane.YES_NO_OPTION);

			if (dialogResult == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		} else if (restartButton.isPressed(x, y)) {
			Reset();
		} else if (saveButton.isPressed(x, y)) {
			ExportFile();
		} else if (m_State == State.READY) {
			if (isReadyPressed(e.getX(), e.getY())) {
				// Set the cheat option to false as soon as the ready button is
				// pressed
				m_bIsCheat = false;

				// Make the target appear within 1800 - 2000 ms
				countDownToState(new Random().nextInt(1800) + 200, State.WAIT_FOR_PRESS);
			}
		} else if (m_State == State.WAIT_FOR_PRESS) {
			// If we are waiting for a press, check for contact with the active
			// target
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

		//JTextField fileNameInput = new JTextField();
		String CompletionString = "Save Results?";

		if (m_State != State.COMPLETED) {
			CompletionString += " (Trial is Unfinished)";
		}

		String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());

		final JComponent[] inputs = new JComponent[] { new JLabel(CompletionString), new JLabel(fileName) };
		int dialogResult = JOptionPane.showConfirmDialog(null, inputs, "Save File", JOptionPane.OK_CANCEL_OPTION);

		if (dialogResult == JOptionPane.YES_OPTION) {
			try {
				PrintWriter writer = new PrintWriter(fileName, "UTF-8");
				String exportString = m_Trial.getExportString();

				writer.println(exportString);
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private void CheckClick(int x, int y, int TargetID) {
		// Calculate the Response TImer
		long lEndTime = new Date().getTime();
		long diffTime = lEndTime - m_lResponseStartTime;
		m_Trial.setResponseTimer(m_iCurrentTrialStep, diffTime);

		// Calculate click accuracy
		int xTarget = (int) (m_aTargets.get(TargetID).getX() * (screenWidth - screenWidth * 0.25) / 100
				+ screenWidth * 0.25);
		int yTarget = m_aTargets.get(TargetID).getY() * screenHeight / 100;
		int z = (int) Math.sqrt(Math.pow(x - xTarget, 2) + Math.pow(y - yTarget, 2));
		
		if (m_bIsCheat) {
			// If cheat then push trial back once
			m_State = State.READY;
			m_Trial.pushBackTrial(m_iCurrentTrialStep);
		} else {
			if (z < CIRCLE_DIAMETER / 2) {
				// If direct shot, add one point to score
				m_iPoints += 1;
				m_Trial.setPoints(m_iCurrentTrialStep, (float) 1.0 );
			} else if (z < CIRCLE_DIAMETER * 1.25 / 2) {
				// If 125% of radius, add 0.5 points
				m_iPoints += 0.5;
				m_Trial.setPoints(m_iCurrentTrialStep, (float) 0.5);
			}
			
			// If last target then set to complete, if not then increment trial step
			if (m_iCurrentTrialStep == LENGTH_TARGETS * WIDTH_TARGETS - 1) {
				m_State = State.COMPLETED;
			} else {
				m_State = State.READY;
				m_iCurrentTrialStep++;
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}

	// On entering window
	@Override
	public void mouseEntered(MouseEvent e) {}

	// On exit window
	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (m_State == State.COUNTDOWN) {
			// If the mouse is released during countdown, it means the user
			// released finger before the target appeared therefore it is a
			// cheat
			m_bIsCheat = true;
			System.out.println("CHEATING");
		} else if (m_State == State.WAIT_FOR_PRESS) {
			// Calculate the Response TImer
			long lEndTime = new Date().getTime();
			long diffTime = lEndTime - m_lReactionStartTime;
			m_Trial.setReactionTimer(m_iCurrentTrialStep, diffTime);
		}

		clearCircles();
		UpdateGraphics();
	}

	// On mouse press and release instantly
	@Override
	public void mouseClicked(MouseEvent e) {}
}
