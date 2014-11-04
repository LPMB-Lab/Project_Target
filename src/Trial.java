/*
 *   FINGERS:
 *   ==========
 *   LEFT HAND:
 *   -4 : Pinky
 *   -3 : Ring
 *   -2 : Middle
 *   -1 : Index
 *   
 *   RIGHT HAND:
 *   +1 : Index
 *   +2 : Middle
 *   +3 : Ring
 *   +4 : Pinky
 */
import java.util.ArrayList;
import java.util.Collections;

public class Trial
{
	private int[] m_aEntries = new int[36];
	private long[] m_aTimers = new long[36];
	private long m_iFastestTime;
	
	Trial()
	{	
		for (int i = 0; i < m_aEntries.length; i++)
			m_aTimers[i] = 0;
		
		GenerateTrials();
	}
	
	private void GenerateTrials()
	{
		ArrayList<Integer> EntryNumbers = new ArrayList<Integer>();
		
		for (int i = 0; i < 36; i++)
			EntryNumbers.add(i);
			
		Collections.shuffle(EntryNumbers);
		
		for (int i = 0; i < 36; i++)
			m_aEntries[i] = EntryNumbers.get(i);
	}
	public void setTimer(int step, long time)
	{
		m_aTimers[step] = time;
		
		if (step == 19)
		{
			m_iFastestTime = m_aTimers[0];
			for (int i = 1; i < m_aTimers.length; i++)
			{
				if (m_aTimers[i] < m_iFastestTime)
					m_iFastestTime = m_aTimers[i];
			}
		}
	}
	public int getElementAt(int index)
	{
		return m_aEntries[index];
	}
	public int getSize() {return m_aEntries.length;}
	public String getExportString()
	{
		String exportString = "Timings: ";
		
		for (int i = 0; i < m_aTimers.length; i++)
		{
			if (i == 19)
				exportString += m_aTimers[i] + "\r\n";
			else
				exportString += m_aTimers[i] + ", ";
		}
		
		exportString += "Entries: ";
		
		for (int i = 0; i < m_aEntries.length; i++)
		{
			if (i == 19)
				exportString += m_aEntries[i] + "\r\n";
			else
				exportString += m_aEntries[i] + ", ";
		}
		
		exportString += "Fastest Time: " + m_iFastestTime + "\r\n\r\n";

		return exportString;
	}
}