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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nrs.scc.tab2.CountItem;

/**
 * Class that actually does the work of counting the number of lines
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileCounter implements Runnable
{
	private FileCounterManager fcManager;
	private SummaryDetails summary = new SummaryDetails();
	private Charset charset;
	private CharsetDecoder decoder;
	private boolean countBlankLines = true;
	private static final int LIST_TOTAL = 20;
	private List<FileDetails> detailsList = new ArrayList<FileDetails>(LIST_TOTAL);
	private boolean finished = false;
	private NavigableSet<Group> matches = new TreeSet<Group>();
	private final static Logger log = Logger.getLogger(FileCounter.class.getName());

	public static final String NEW_LINE_PATTERN = "\n";
	public static final String BLANK_LINE_PATTERN = "^\\s*$";
	
	private final Matcher NEW_LINE_MATCHER = Pattern.compile(NEW_LINE_PATTERN, Pattern.MULTILINE).matcher("");
	private final Matcher BLANK_LINE_MATCHER = Pattern.compile(BLANK_LINE_PATTERN, Pattern.MULTILINE).matcher("");

	private class Group implements Comparable<Group>
	{
		Integer start;
		Integer end;

		private Group(Integer start, Integer end)
		{
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(Group g)
		{
			return start.compareTo(g.start);
		}
	}

	/**
	 * Create a new FileCounter
	 */
	public FileCounter(FileCounterManager fcManager, String charsetName, boolean countBlankLines)
	{
		this.fcManager = fcManager;
		charset = Charset.forName(charsetName);
		decoder = charset.newDecoder();
		this.countBlankLines = countBlankLines;
	}

	@Override public void run()
	{
		log.entering("FileCounter", "run");
		
		FileDetails fd;
		// count the lines in each file
		while (!Thread.interrupted() && ((fd = fcManager.nextFileDetails()) != null))
		{
			matches.clear();
			countFile(fd);
		}

		// add the remainder
		if (!detailsList.isEmpty()) {
			fcManager.addFileDetailsResults(detailsList);
		}

		finished = true;

		log.exiting("FileCounter", "run");
	}

	/**
	 * Return a summary for the files counted
	 * @return SummaryDetails
	 */
	SummaryDetails getSummaryDetails()
	{
		return summary;
	}
	
	/**
	 * Count the number of lines in a piece of text
	 * @param text the text to process
	 * @return number of lines
	 */
	private int countLines(CharSequence text)
	{
		//log.entering("FileCounter", "countLines");

		NEW_LINE_MATCHER.reset(text);
		int newLines = 0;
		while (NEW_LINE_MATCHER.find())	{
			newLines++;
		}
		newLines++; // last line won't be matched above or if text is blank
		
		//log.exiting("FileCounter", "countLines", newLines);
		return newLines;
	}

	/**
	 * Sorts out whether any of the matches overlap and returns a true count of matching lines
	 * @return
	 */
	private int getCountedLines(CharSequence text)
	{
		CharSequence substring;
		Group prevGroup = null;
		Group group = null;
		int countedLines = 0;

		log.entering("FileCounter", "getCountedLines");
		
		// see if any match groups need to be merged
		Iterator<Group> groupIt = matches.iterator();
		while (groupIt.hasNext())
		{
			group = groupIt.next();
			if (prevGroup == null)
			{
				prevGroup = group;
			}
			else
			{
				if (group.start <= (prevGroup.end+1)) // overlap or next to previous group
				{
					if (group.end >= prevGroup.end)
					{ // previous group gets larger
						prevGroup.end = group.end;
					}
					groupIt.remove(); // merged - no longer needed
				}
				else
				{
					prevGroup = group; // no change
				}
			}
		}

		// count merged groups
		for (Group supergroup : matches)
		{
			substring = text.subSequence(supergroup.start, supergroup.end);
			countedLines += countLines(substring);
		}

		log.exiting("FileCounter", "getCountedLines", countedLines);
		
		return countedLines;
	}

	private void countFile(FileDetails details)
	{
		try
		{
			FileChannel channel = new FileInputStream(details.getFile()).getChannel();
			MappedByteBuffer bb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

			// Decode the file into a char buffer
			CharBuffer cb = decoder.decode(bb);

			int totalLines = 0;
			int blankLines = 0;

			// count total lines
			totalLines += countLines(cb);

			// count blank lines
			if (countBlankLines)
			{
				BLANK_LINE_MATCHER.reset(cb);
				while (BLANK_LINE_MATCHER.find())
				{
					blankLines++;
					matches.add(new Group(BLANK_LINE_MATCHER.start(), BLANK_LINE_MATCHER.end()));
				}
			}

			details.resetTotals();
			details.totalLines = totalLines;
			details.blankLines = blankLines;
			summary.incrementTotalLines(totalLines);
			summary.incrementBlankLines(blankLines);
			summary.addCount(details.getFileExtension(), FileCounterManager.TOTAL_LINES, totalLines);
			summary.addCount(details.getFileExtension(), FileCounterManager.BLANK_LINES, blankLines);
			
			// count other items
			List<CountItem> counters = fcManager.getSelectedPatterns(details);
			if (counters.size() > 0)
			{
				Matcher matcher;
				for (CountItem counter : counters)
				{
					matcher = counter.regExp.matcher(cb);
					while (matcher.find())
					{
						matches.add(new Group(matcher.start(), matcher.end()));
					}
				}
			}

			// calculate counted lines
			details.countedLines = getCountedLines(cb);
			summary.addCount(details.getFileExtension(), FileCounterManager.COUNT_LINES, details.countedLines);
			summary.incrementCountedLines(details.countedLines);

			if (countBlankLines) {
				details.sourceLines = details.totalLines - details.countedLines;
			} else {
				details.sourceLines = details.totalLines - (details.blankLines + details.countedLines);
			}
			summary.incrementSourceLines(details.sourceLines);
			summary.addCount(details.getFileExtension(), FileCounterManager.REM_LINES, details.sourceLines);
			detailsList.add(details);

			if (detailsList.size() == LIST_TOTAL)
			{
				fcManager.addFileDetailsResults(detailsList);
				detailsList.clear();
			}
		}
		catch (IOException ioe)
		{
			fcManager.raiseException(ioe, details);
		}
	}

	/**
	 * Has the FileCounter finished executing
	 * @return true or false
	 */
	boolean isFinished()
	{
		return finished;
	}
}
