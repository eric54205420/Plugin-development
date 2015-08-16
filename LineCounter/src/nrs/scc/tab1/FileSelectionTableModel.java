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

package nrs.scc.tab1;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import nrs.scc.tab2.FileTypes;
import nrs.scc.tab3.FileDetails;

/**
 * Selected files
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileSelectionTableModel extends AbstractTableModel
{
	// use a set to prevent having to iterate through the list every time
	private Set<String> addedFiles = new HashSet<String>();
	private List<FileDetails> files = new ArrayList<FileDetails>();
	private Map<String, Integer> fileTypesCount = new HashMap<String, Integer>();
	private Map<String, Set<Integer>> patternIndex = new HashMap<String, Set<Integer>>();

	// required so that iterator can determine correct file types to iterate over
	private FileTypes fileTypes;
	private int knownFileCount = 0;

	private final static Class[] colClasses = { String.class, String.class, Long.class, Long.class };
	private String[] colNames;

	/**
	 * Iterator over the files that checks that the files are known file types.
	 * If not, they are excluded from the iteration
	 */
	private class FileIterator implements Iterator<FileDetails>
	{
		private boolean iterateOverUnknown = false;
		private int index = 0;

		public FileIterator(boolean iterateOverUnknown)
		{
			this.iterateOverUnknown = iterateOverUnknown;
		}

		public boolean hasNext()
		{
			while (index < files.size())
			{
				FileDetails fd = files.get(index);
				if (iterateOverUnknown || fileTypes.isKnownFileType(fd.getFileType())) {
					return true;
				}
				index++;
			}

			return false;
		}

		public FileDetails next() throws NoSuchElementException
		{
			if (index < files.size())
			{
				return files.get(index++);
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
		{
			// no-op
		}
	}

	public FileSelectionTableModel(String[] colNames)
	{
		this.colNames = colNames;
	}

	/**
	 * Required so that the iterator can determine which files are known
	 * @param ft
	 */
	public void setFileTypes(FileTypes ft)
	{
		fileTypes = ft;
	}

	/**
	 * Return the number of files which have known file extensions.
	 * This may be less than or equal to the total number of files.
	 * @param iterateOverUnknown if true then return total number of files; else known files only
	 * @return
	 */
	public int getFilesCount(boolean iterateOverUnknown)
	{
		return (iterateOverUnknown ? files.size() : knownFileCount);
	}

	/**
	 * Gets an iterator that takes into account whether the files are known or not
	 * @param iterateOverUnknown if true the iterator will also return unknown files,
	 * assuming that the user has chosen to identify them as a specific file type
	 * @return the files
	 */
	public Iterator<FileDetails> getFileIterator(boolean iterateOverUnknown)
	{
		return new FileIterator(iterateOverUnknown);
	}

	/**
	 * Return the indexes of files with the specified file extension
	 * @param filePattern file pattern, e.g. SourceCodeCounter.java or java
	 * @return Set of indexes; may be null
	 */
	public Set<Integer> getIndexOfFilesMatchingPattern(String filePattern)
	{
		return patternIndex.get(filePattern);
	}
	
	public int getColumnCount()
	{
		return colNames.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return colNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return colClasses[columnIndex];
	}
	
	public int getRowCount()
	{
		return files.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		assert rowIndex <= getRowCount();
		assert columnIndex < colNames.length;
		
		FileDetails details = files.get(rowIndex);

		Object value;
		switch (columnIndex)
		{
			case 1: 
				value = details.getFileType();
				break;

			case 2:
				value = details.getSize();
				break;

			case 3:
				value = details.getModifiedDate();
				break;

			default:
				value = details.getPath(); // case 0
				break;
		}
		
		return value;
	}
	
	public void clear()
	{
		int lastRow = files.size()-1;
		files.clear();
		addedFiles.clear();
		fileTypesCount.clear();
		fireTableRowsDeleted(0, lastRow);
	}

	/**
	 * Add the specified file to the table model
	 * @param filesList
	 */
	public void addFiles(List<File> filesList)
	{
		assert filesList != null : "Cannot add a null list of files";
		assert !filesList.isEmpty() : "Cannot add an empty list";

		int firstRow = files.size();
		String absPath;
		for (File file : filesList)
		{
			// only add the file if it's not already been added
			absPath = file.getAbsolutePath();
			if (!addedFiles.contains(absPath))
			{
				FileDetails fd = new FileDetails(file);
				files.add(fd);
				addedFiles.add(absPath);

				// increment files types count
				String fileType = fd.getFileType();
				Integer count = fileTypesCount.get(fileType);
				if (count == null) {
					count = 1;
				} else {
					count++;
				}
				fileTypesCount.put(fileType, count);

				if (fileTypes.isKnownFileType(fd.getFileType())) {
					knownFileCount++;
				}

				// increment pattern index
				Set<Integer> pI = patternIndex.get(fileType);
				if (pI == null) {
					pI = new HashSet<Integer>();
				}
				pI.add(files.size()-1); // items get added to the end
				patternIndex.put(fileType, pI);
			}
		}

		int lastRow = files.size() - 1;
		if (lastRow >= firstRow) {
			fireTableRowsInserted(firstRow, lastRow);
		}
	}

	/**
	 * Get a count of the number of each file type added
	 * @return
	 */
	public Map<String, Integer> getFileTypesAdded()
	{
		return fileTypesCount;
	}

	private void removeRows(int firstRow, int lastRow)
	{
		for (int row=lastRow; row>=firstRow; row--)
		{
			files.remove(row);
		}
		fireTableRowsDeleted(firstRow, lastRow);
	}

	/**
	 * Remove the specified rows from the table model
	 * @param rows row indexes from the model
	 * @return FileTypes
	 */
	public Map<String, Integer> removeRows(int[] rows)
	{
		// sort into ascending order
		Arrays.sort(rows);

		// remove entries starting from the end
		int row;
		int firstRow = -1;
		int lastRow = -1;
		int lastDeleteRow = -1;
		FileDetails fd;
		String fileType;
		int count;
		for (int listIndex = rows.length-1; listIndex>-1; listIndex--)
		{
			row = rows[listIndex];
			fd = files.get(row);
			addedFiles.remove(fd.getPath());

			// update file types count
			fileType = fd.getFileType();
			count = fileTypesCount.get(fileType)-1;

			if (count == 0) {
				fileTypesCount.remove(fileType);
			} else {
				fileTypesCount.put(fileType, count);
			}

			if (fileTypes.isKnownFileType(fd.getFileType())) {
				knownFileCount--;
			}

			if (firstRow == -1) {
				firstRow = lastRow = row;
			}

			if (firstRow - row > 1)
			{
				removeRows(firstRow, lastRow);
				lastDeleteRow = firstRow;
				lastRow = row;
			}
			
			firstRow = row;
		}

		if (lastDeleteRow != firstRow)
		{
			removeRows(firstRow, lastRow);
		}

		// update pattern index
		Set<Integer> pIndexes;
		patternIndex.clear();
		for (row=0; row<files.size(); row++)
		{
			fd = files.get(row);
			fileType = fd.getFileType();
			pIndexes = patternIndex.get(fileType);
			if (pIndexes == null)
			{
				pIndexes = new HashSet<Integer>();
				patternIndex.put(fileType, pIndexes);
			}
			pIndexes.add(row);
		}

		return fileTypesCount;
	}
}
