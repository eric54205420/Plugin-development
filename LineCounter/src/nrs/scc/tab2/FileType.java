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
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 * Definition of a file type including name and file patterns that match
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileType implements Comparable<FileType>, TreeNode
{
	private String name;
	private List<String> filePatterns = new ArrayList<String>();
	private List<CountItem> countItems = new ArrayList<CountItem>();
	private TreeNode parent;
	
	public FileType(TreeNode parent, String name)
	{
		assert parent != null : "FileType parent is null";
		assert name != null : "FileType name is null";
		
		this.parent = parent;
		this.name = name;
		
		// set default extension
		filePatterns.add("new");
	}

	/**
	 * Check to see if this FileType knows about the specified file
	 * @param fileName file name to check, e.g. SourceCodeCounter.java
	 * @param extension file name extension, e.g. java
	 * @return true if this FileType knows about this file name
	 */
	public boolean isKnownPattern(String fileName, String extension)
	{
		return filePatterns.contains(extension) || filePatterns.contains(fileName);
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setFilePatterns(List<String> filePatterns)
	{
		this.filePatterns = filePatterns;
	}
	
	public List<String> getFilePatterns()
	{
		return filePatterns;
	}

	public List<CountItem> getCountItems()
	{
		return countItems;
	}

	public void setCountItems(List<CountItem> countItems)
	{
		this.countItems = countItems;
	}

	public boolean addCountItem(CountItem ci)
	{
		assert ci != null : "CountItem is null";

		boolean added = false;
		if (!countItems.contains(ci))
		{
			countItems.add(ci);
			added = true;
		}
		return added;
	}
	
	public void removeCountItem(CountItem ci)
	{
		countItems.remove(ci);
	}

	@Override
	public Enumeration children()
	{
		return Collections.enumeration(countItems);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return countItems.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return countItems.size();
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return countItems.indexOf(node);
	}

	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	@Override
	public boolean isLeaf()
	{
		return !countItems.isEmpty();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(FileType ft)
	{
		return name.compareTo(ft.name);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FileType)
		{
			FileType ft = (FileType)obj;
			return name.equals(ft.name);
		}
		
		return false;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	public String getFilePatternsAsString()
	{
		StringBuilder sb = new StringBuilder();
		for (String item : filePatterns)
		{
			sb.append(item);
			sb.append(",");
		}
		if (sb.length() > 1) {
			sb.setLength(sb.length()-1);
		}

		return sb.toString();
	}
}
