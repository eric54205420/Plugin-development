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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nrs.scc.SourceCodeCounterView;
import nrs.scc.tab2.CountItem;
import nrs.scc.tab2.FileTypes;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

/**
 * Class that actually does the work of counting the number of lines
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileCounterManager extends Task
{
	private SourceCodeCounterView sccView;
	private Iterator<FileDetails> fileIterator;
	private FileTypes fileTypes;
	private ResultsTableModel rtm;
	private SummaryResultsTableModel srtm;
	private Map<String, List<CountItem>> patternCache = new HashMap<String, List<CountItem>>();
	private int errorCount;
	private String charset;
	private boolean countBlankLines = true;
	private int threadCount = 1;
	private String unknownFilesFileType;
	private ThreadGroup tGroup;
	private SummaryDetails summaryDetails = new SummaryDetails();
	private final static Logger log = Logger.getLogger(FileCounterManager.class.getName());

	static String BLANK_LINES;
	static String COUNT_LINES;
	static String REM_LINES;
	static String TOTAL_LINES;

	// timing info
	private Date start;
	private int fileCount;
	private float processedFileCount;
	private int totalFileCount;

	// declared as class variables to minimise primitive creation
	private long timeSoFar;
	private float timeSoFarSeconds;
	private int eta;

	/** When enough files have been counted send them in a block to the ResultsTableModel
	 * to reduce the number of row insert events fired */
	private final static int DETAILS_CACHE_SIZE = 100;
	private List<FileDetails> detailsListCache = new ArrayList<FileDetails>(DETAILS_CACHE_SIZE);

	private Map<String, String> errors = new TreeMap<String, String>();
	
	/**
	 * Create a new FileCounter
	 */
	public FileCounterManager(SourceCodeCounterView sccView, int totalFileCount, Iterator<FileDetails> fileIterator, FileTypes fileTypes,
		ResultsTableModel rtm, SummaryResultsTableModel srtm, String charset, int fileCountThreads, boolean countBlankLines,
		String unknownFilesFileType)
	{
		super(sccView.getApplication());

		this.sccView = sccView;
		this.totalFileCount = totalFileCount;
		this.fileIterator = fileIterator;
		this.fileTypes = fileTypes;
		this.rtm = rtm;
		this.srtm = srtm;
		this.charset = charset;
		this.countBlankLines = countBlankLines;
		this.unknownFilesFileType = unknownFilesFileType;

		ResourceMap resourceMap = getResourceMap();
		BLANK_LINES = resourceMap.getString("blank.lines");
		COUNT_LINES = resourceMap.getString("count.lines");
		REM_LINES = resourceMap.getString("rem.lines");
		TOTAL_LINES = resourceMap.getString("total.lines");

		assert fileCountThreads > 0 : "fileCountThreads must be > 0";
		this.threadCount = fileCountThreads;
	}
	
	/**
	 * Get the next FileDetails
	 * @return FileDetails or null if the end of the list is reached
	 */
	synchronized FileDetails nextFileDetails()
	{
		FileDetails fd = null;
		if (fileIterator.hasNext())
		{
			fd = fileIterator.next();
			setProgress(fileCount, 0, totalFileCount);
			//message("counting", fileCount);
			fileCount++;
		}
		return fd;
	}

	/**
	 * See if there are any selected counters for the defined file
	 * @param fileDetails
	 * @return selected patterns or an empty list if none have been defined and selected
	 */
	synchronized List<CountItem> getSelectedPatterns(FileDetails fileDetails)
	{
		List<CountItem> cis = patternCache.get(fileDetails.getFileType().toLowerCase());

		if (cis == null)
		{
			cis = fileTypes.getSelectedPatterns(fileDetails, unknownFilesFileType);
			patternCache.put(fileDetails.getFileType().toLowerCase(), cis); // storing empty list will prevent future lookups that will also fail
		}

		return cis;
	}

	/**
	 * A FileCounter encountered an error
	 * @param ex
	 * @param details
	 */
	synchronized void raiseException(Exception ex, FileDetails details)
	{
		message("errorMessage", details.getPath());
		log.log(Level.WARNING, "Exception processing "+details.getPath(), ex);
		errors.put(details.getPath(), ex.getMessage());
		errorCount++;
	}

	@Override public Object doInBackground() throws Exception
	{
		log.entering("FileCounterManager", "doInBackground");

		message("startMessage", totalFileCount);
		
		start = new Date();
		setProgress(0f);

		// create and start the required number of FileCounters
		tGroup = new ThreadGroup("FileCounters");
		List<FileCounter> fileCounters = new ArrayList<FileCounter>(threadCount);
		FileCounter fc;
		for (int i=0; i<threadCount; i++)
		{
			fc = new FileCounter(this, charset, countBlankLines);
			fileCounters.add(fc);
			new Thread(tGroup, fc, "FileCounter-"+i).start();
		}

		// wait for all the tasks to complete
		try
		{
			int finishedTotal;
			do
			{
				finishedTotal = 0;
				for (FileCounter counter : fileCounters)
				{
					if (counter.isFinished()) {
						finishedTotal++;
					}
				}
				Thread.sleep(500); // sleep for half a second
			} while (finishedTotal < fileCounters.size());

			for (FileCounter counter : fileCounters)
			{
				summaryDetails.mergeSummaryDetails(counter.getSummaryDetails());
			}
		}
		catch (InterruptedException ie)
		{
			log.fine("FileCounterManager interrupted");
		}

		log.exiting("FileCounterManager", "doInBackground");
		
		return null;
	}

	/**
	 * Add a list of FileDetails
	 * @param detailsList
	 */
	synchronized void addFileDetailsResults(List<FileDetails> detailsList)
	{
		detailsListCache.addAll(detailsList);
		processedFileCount += detailsList.size();
		if (detailsListCache.size() >= DETAILS_CACHE_SIZE)
		{
			rtm.addFileDetails(detailsListCache);
			detailsListCache.clear();

			// using class variables to minimise primitive creation
			timeSoFar = new Date().getTime() - start.getTime();
			timeSoFarSeconds = timeSoFar / 1000f;
			eta = (int)((totalFileCount - processedFileCount) * (timeSoFar / processedFileCount)) / 1000;
			message("processing", processedFileCount, timeSoFarSeconds, eta);
		}
	}

	@Override protected void finished()
	{
		log.entering("FileCounterManager", "finished");

		// add any final details
		if (!detailsListCache.isEmpty())
		{
			rtm.addFileDetails(detailsListCache);
			detailsListCache.clear();
		}

		srtm.setSummaryDetails(summaryDetails);
		long diff = new Date().getTime() - start.getTime();
		float time = (float)diff/1000;
		message("finishedMessage", fileCount, errorCount, time);
		sccView.countFinished(fileCount, time, errors);
		log.exiting("FileCounterManager", "finished");
	}

	@Override protected void interrupted(InterruptedException arg0)
	{
		log.entering("FileCounterManager", "interrupted");
		tGroup.interrupt();
		log.exiting("FileCounterManager", "interrupted");
	}

	@Override protected void cancelled()
	{
		log.entering("FileCounterManager", "cancelled");
		tGroup.interrupt();
		log.exiting("FileCounterManager", "cancelled");
	}

	@Override
	protected void finalize() throws Throwable
	{
		log.finest("finalize");
	}
}
