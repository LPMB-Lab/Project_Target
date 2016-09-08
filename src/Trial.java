
import java.util.ArrayList;
import java.util.Collections;

public class Trial {

	private int NUMBER_OF_TRIALS = 36;

	private int[] m_aEntries = new int[NUMBER_OF_TRIALS];
	private long[] m_aResponseTimers = new long[NUMBER_OF_TRIALS];
	private long[] m_aReactionTimers = new long[NUMBER_OF_TRIALS];
	private float[] m_aPoints = new float[NUMBER_OF_TRIALS];
	private long m_iFastestResponseTime;
	private long m_iFastestReactionTime;

	Trial(int numberOfTrials) {
	    NUMBER_OF_TRIALS = numberOfTrials;
		ResetTrials();
		GenerateTrials();
	}

	private void ResetTrials() {
		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			m_aResponseTimers[i] = 0;
		}

		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			m_aReactionTimers[i] = 0;
		}

		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			m_aPoints[i] = 0;
		}
	}

	private void GenerateTrials() {
		ArrayList<Integer> EntryNumbers = new ArrayList<Integer>();

		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			EntryNumbers.add(i);
		}

		Collections.shuffle(EntryNumbers);

		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			m_aEntries[i] = EntryNumbers.get(i);
		}
	}

	public void setResponseTimer(int step, long responseTime) {
		m_aResponseTimers[step] = responseTime;

		if (step == 0) {
			// On the first step set the fastest time
			m_iFastestResponseTime = responseTime;
		} else {
			// Else check with current fastest and set it
			if (responseTime < m_iFastestResponseTime) {
				m_iFastestResponseTime = responseTime;
			}
		}
	}

	public void setReactionTimer(int step, long reactionTime) {
		m_aReactionTimers[step] = reactionTime;

		if (step == 0) {
			// On the first step set the fastest time
			m_iFastestReactionTime = reactionTime;
		} else {
			if (reactionTime < m_iFastestReactionTime) {
				m_iFastestReactionTime = reactionTime;
			}
		}
	}
	
	public void pushBackTrial(int step) {
		// What we are going to do here is swap the step trial with the last trial
		int tempStore = m_aEntries[step];
		m_aEntries[step] = m_aEntries[NUMBER_OF_TRIALS-1];
		m_aEntries[NUMBER_OF_TRIALS-1] = tempStore;
	}

	public void setPoints(int step, float points) {
		m_aPoints[step] = points;
	}

	public int getElementAt(int index) {
		return m_aEntries[index];
	}

	public int getSize() {
		return NUMBER_OF_TRIALS;
	}

	public String getExportString() {
		String exportString = "Response Timings (ms): ";
		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			exportString += m_aResponseTimers[i] + ", ";
		}

		exportString += "\r\nReaction Timings (ms): ";
		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			exportString += m_aReactionTimers[i] + ", ";
		}

		exportString += "\r\nPoints: ";
		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			exportString += m_aPoints[i] + ", ";
		}

		exportString += "\r\nEntries: ";
		for (int i = 0; i < NUMBER_OF_TRIALS; i++) {
			exportString += m_aEntries[i] + ", ";
		}

		exportString += "\r\nFastest Response Time (ms): " + m_iFastestResponseTime + "ms";
		exportString += "\r\nFastest Reaction Time (ms): " + m_iFastestReactionTime + "ms \r\n";

		return exportString;
	}
}
