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

import java.io.File;
import java.util.Date;

/**
 * Details for a file
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileDetails
{
	private String filePath;
	private String fileName;
	private String fileExtension;
	private long size;
	private long lastModified;
	public int totalLines;
	public int blankLines;
	public int countedLines;
	public int sourceLines;

	private final static char DOT = '.';
	
	public FileDetails(File file)
	{
		filePath = file.getPath();
		fileName = file.getName();
		fileExtension = getFileExtension(file.getName());
		size = file.length();
		lastModified = file.lastModified();
	}

	/**
	 * Constructor used for creating an object just to hold the totals
	 */
	public FileDetails()
	{
	}

	/** Set all the totals to 0 */
	public void resetTotals()
	{
		totalLines = blankLines = countedLines = sourceLines = 0;
	}

	/**
	 * Sort of hack to allow use of a Total row in the summary results table
	 * @param fileName
	 */
	public void setTotal(String total)
	{
		fileName = total;
	}

	/**
	 * Deduce the file extension for a given file
	 * @param fileName file name not including any preceding path
	 * @return file extension as lower case or null if not present
	 */
	public static String getFileExtension(String fileName)
	{
		String ext = null;
		int dotIndex = fileName.lastIndexOf(DOT);
		if (dotIndex > -1 && dotIndex < fileName.length()-1) {
			ext = fileName.substring(dotIndex+1).toLowerCase();
		}

		return ext;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(filePath);
		sb.append(", TL:");
		sb.append(totalLines);
		sb.append(", BL:");
		sb.append(blankLines);
		sb.append(", CL:");
		sb.append(countedLines);
		sb.append(", SL:");
		sb.append(sourceLines);
		
		return sb.toString();
	}

	/**
	 * Full path to the file name
	 * @return the file name including path; may be null if this is a pseudo file
	 */
	public String getPath()
	{
		return filePath;
	}

	/**
	 * Get the name of the file excluding the preceding path
	 * @return file name excluding path
	 */
	public String getName()
	{
		return fileName;
	}

	/**
	 * Get the file extension, e.g. java or html
	 * @return the file extension
	 */
	public String getFileExtension()
	{
		return fileExtension;
	}

	/**
	 * Get the file type
	 * @return either the file extension if it has one, otherwise the file name excluding the path
	 */
	public String getFileType()
	{
		return fileExtension == null ? fileName : fileExtension;
	}

	/**
	 * Get the size of the file
	 * @return the size
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * Get the modified date
	 * @return the date
	 */
	public Date getModifiedDate()
	{
		return new Date(lastModified);
	}

	/**
	 * Get the File reference
	 * @return the file
	 */
	public File getFile()
	{
		return new File(filePath);
	}
	
	@Override public boolean equals(Object o)
	{
		if (o == null || !(o instanceof FileDetails)) {
			return false;
		}
		
		FileDetails fd = (FileDetails)o;
		if (filePath == null) {
			return (fd.getPath() == null);
		} else {
			return filePath.equals(fd.getPath());
		}
	}
	
	@Override public int hashCode()
	{
		return (filePath == null ? 0 :filePath.hashCode());
	}
}
