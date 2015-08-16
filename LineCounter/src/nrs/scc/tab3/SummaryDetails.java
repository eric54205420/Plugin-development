/*
 *  SourceCodeCounter
 *  Copyright (C) 2009 Nick Sydenham <nsydenham@yahoo.co.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nrs.scc.tab3;

import java.util.HashMap;
import java.util.Map;

/**
 * Summary details of applying the counters
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SummaryDetails
{
	private Map<String, Map<String, Integer>> summary = new HashMap<String, Map<String, Integer>>();
	private int totalLines;
	private int totalBlankLines;
	private int totalCountedLines;
	private int totalSourceLines;

	/**
	 * Add a count for the specified file type
	 * @param fileType name of tile type, e.g. java
	 * @param counter e.g. Comment
	 * @param increment the amount to increment by
	 */
	public void addCount(String fileType, String counter, int increment)
	{
		if (increment == 0) {
			return;
		}
		
		Map<String, Integer> counterMap = summary.get(fileType);
		if (counterMap == null)
		{
			counterMap = new HashMap<String, Integer>();
			summary.put(fileType, counterMap);
		}
		
		Integer total = counterMap.get(counter);
		if (total == null) {
			total = 0;
		}
		total += increment;
		counterMap.put(counter, total);
	}

	/**
	 * Add the summary details into this
	 * @param details
	 */
	public void mergeSummaryDetails(SummaryDetails details)
	{
		assert details != null : "SummaryDetails cannot be null";
		
		totalLines += details.getTotalLines();
		totalBlankLines += details.getTotalBlankLines();
		totalCountedLines += details.getTotalCountedLines();
		totalSourceLines += details.getTotalSourceLines();

		for (String fileType : details.getSummaryTotals().keySet())
		{
			summary.put(fileType, details.getSummaryTotals().get(fileType));
		}
	}

	public void incrementTotalLines(int increment)
	{
		totalLines += increment;
	}
	
	public void incrementBlankLines(int increment)
	{
		totalBlankLines += increment;
	}
	
	public void incrementCountedLines(int increment)
	{
		totalCountedLines += increment;
	}
	
	public void incrementSourceLines(int increment)
	{
		totalSourceLines += increment;
	}
	
	public Map<String, Map<String, Integer>> getSummaryTotals()
	{
		return summary;
	}

	public int getTotalLines()
	{
		return totalLines;
	}

	public int getTotalBlankLines()
	{
		return totalBlankLines;
	}

	public int getTotalCountedLines()
	{
		return totalCountedLines;
	}

	public int getTotalSourceLines()
	{
		return totalSourceLines;
	}
}
