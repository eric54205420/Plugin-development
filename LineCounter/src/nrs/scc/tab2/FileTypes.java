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

package nrs.scc.tab2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nrs.scc.tab3.FileDetails;

/**
 * File types manager
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileTypes
{
	// Map between file patterns and FileType objects. A single FileType may therefore
	// be referenced by several file pattern keys
	private Map<String, FileType> fileTypePatternMap = new HashMap<String, FileType>();
	
	public static final String BACKUP = "~";
		
	public FileTypes(Map<String, FileType> fileTypes)
	{
		for (FileType ft : fileTypes.values())
		{
			for (String pattern : ft.getFilePatterns())
			{
				fileTypePatternMap.put(pattern.toLowerCase(), ft);
			}
		}
	}

	/**
	 * Check if the specified file is defined
	 * @param fileType either file extension (e.g. java) or file name (e.g. SourceCodeCounterView.java)
	 * @return true if defined, false otherwise
	 */
	public boolean isKnownFileType(String fileType)
	{
		assert fileType != null : "fileType is null";

		if (fileType.endsWith(BACKUP)) {
			fileType = fileType.substring(0, fileType.lastIndexOf(BACKUP));
		}
		
		return fileTypePatternMap.containsKey(fileType.toLowerCase());
	}

	/**
	 * Get regexp patterns for the specified file type that have been selected by the user
	 * @param details FileDetails
	 * @param unknownFilesFileType what to treat unknown files as; may be null
	 * @return a (possibly empty) list of patterns
	 */
	public List<CountItem> getSelectedPatterns(FileDetails details, String unknownFilesFileType)
	{
		// check file extension first
		FileType ft = fileTypePatternMap.get(details.getFileExtension());
		// check full name
		if (ft == null) {
			ft = fileTypePatternMap.get(details.getName().toLowerCase());
		}
		// check if an override has been set
		if (ft == null && unknownFilesFileType != null)
		{
			for (FileType fte : fileTypePatternMap.values())
			{
				if (fte.getName().equals(unknownFilesFileType))
				{
					ft = fte;
					break;
				}
			}
		}

		List<CountItem> selCIs = new ArrayList<CountItem>();
		if (ft != null)
		{
			selCIs = new ArrayList<CountItem>();
			CountItem ci;
			Enumeration e = ft.children();
			while (e.hasMoreElements())
			{
				ci = (CountItem)e.nextElement();
				if (ci.isSelected())
				{
					selCIs.add(ci);
				}
			}
		}
		
		return selCIs;
	}

	/**
	 * Get the file type definition associated with the specified file extension
	 * @param filePattern - either SourceCodeCounter.java or java
	 * @return FileType
	 */
	public FileType getFileType(String filePattern)
	{
		assert filePattern != null : "filePattern cannot be null";
		return fileTypePatternMap.get(filePattern.toLowerCase());
	}

	/**
	 * Get a list of file type names
	 * @return sorted array of file type names
	 */
	public String[] getFileTypeNames()
	{
		Set<String> fileTypeNamesSet = new HashSet<String>();
		for (FileType ft : fileTypePatternMap.values())
		{
			fileTypeNamesSet.add(ft.getName());
		}

		List<String> fileTypeNameList = new ArrayList<String>(fileTypeNamesSet.size());
		Collections.addAll(fileTypeNameList, fileTypeNamesSet.toArray(new String[0]));
		Collections.sort(fileTypeNameList);

		return fileTypeNameList.toArray(new String[0]);
	}
}
