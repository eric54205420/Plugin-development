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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;

/**
 * File types list backing model
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileTypesListModel extends DefaultListModel
{
	private List<String> fileTypesList = new ArrayList<String>(); // backing model for the list

	@Override
	public Object get(int index)
	{
		return fileTypesList.get(index);
	}

	/**
	 * Gets the element at the specified index
	 * @param index
	 * @return
	 * @see #getFileTypeAt(int)
	 */
	@Override
	public Object getElementAt(int index)
	{
		return get(index);
	}

	/**
	 * Returns the file type at the specified index but without the (x) part on the end
	 * @param index
	 * @return file type
	 * @see #getElementAt(int)
	 */
	public String getFileTypeAt(int index)
	{
		String item = fileTypesList.get(index);
		return (item == null ? null : item.substring(0, item.lastIndexOf("(")-1));
	}

	@Override
	public int size()
	{
		return fileTypesList.size();
	}

	@Override
	public int getSize()
	{
		return size();
	}
	
	public void addFileTypes(Map<String, Integer> fileTypes)
	{
		fileTypesList.clear();
		for (String fileType : fileTypes.keySet())
		{
			fileTypesList.add(fileType+" ("+fileTypes.get(fileType)+")");
		}
		Collections.sort(fileTypesList);
		fireIntervalAdded(this, 0, size());
	}
	
	public void setFileTypes(Map<String, Integer> fileTypes)
	{
		int curSize = size();
		fileTypesList.clear();
		fireIntervalRemoved(this, 0, curSize);
		addFileTypes(fileTypes);
	}

	@Override
	public void clear()
	{
		int size = fileTypesList.size();
		fileTypesList.clear();
		fireIntervalRemoved(this, 0, size);
	}

	@Override
	public void removeAllElements()
	{
		clear();
	}
}
