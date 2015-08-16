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

import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.swing.tree.TreeNode;
import org.jdom.Element;

/**
 * Definition of a counter including name and regular expression
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class CountItem implements TreeNode, Cloneable
{
	public String name;
	public Pattern regExp;
	private FileType parentFileType;
	private boolean selected = false;

	public CountItem(FileType fileType, String name, Pattern regExp)
	{
		assert name != null : "name is null";
		assert regExp != null : "regExp is null";
		
		parentFileType = fileType;
		this.name = name;
		this.regExp = regExp;
	}
	
	public void setFileType(FileType fileType)
	{
		this.parentFileType = fileType;
	}

	@Override
	public Enumeration children()
	{
		return null;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return null;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return 0;
	}

	@Override
	public TreeNode getParent()
	{
		return parentFileType;
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean equals = false;
		
		if (obj instanceof CountItem)
		{
			CountItem ci = (CountItem)obj;
			equals = name.equals(ci.name);
		}
		
		return equals;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		CountItem ci = (CountItem)super.clone();
		ci.setFileType(null);
		return ci;
	}

	/**
	 * Get an XML representation of this object
	 * @return
	 */
	public Element getXML()
	{
		Element ci = new Element(FileTypesSAXParser.COUNT_ITEM);
		ci.setAttribute(FileTypesSAXParser.ATTR_NAME, name);
		ci.setAttribute(FileTypesSAXParser.ATTR_SELECTED, Boolean.toString(selected));
		String Y = "Y";
		if ((regExp.flags() & Pattern.CANON_EQ) == Pattern.CANON_EQ) {
			ci.setAttribute(FileTypesSAXParser.ATTR_CANON_EQUIV, Y);
		}
		if ((regExp.flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE) {
			ci.setAttribute(FileTypesSAXParser.ATTR_CASE_INSENSITIVE, Y);
		}
		if ((regExp.flags() & Pattern.COMMENTS) == Pattern.COMMENTS) {
			ci.setAttribute(FileTypesSAXParser.ATTR_COMMENTS, Y);
		}
		if ((regExp.flags() & Pattern.DOTALL) == Pattern.DOTALL) {
			ci.setAttribute(FileTypesSAXParser.ATTR_DOT_ALL, Y);
		}
		if ((regExp.flags() & Pattern.LITERAL) == Pattern.LITERAL) {
			ci.setAttribute(FileTypesSAXParser.ATTR_LITERAL, Y);
		}
		if ((regExp.flags() & Pattern.UNICODE_CASE) == Pattern.UNICODE_CASE) {
			ci.setAttribute(FileTypesSAXParser.ATTR_UNICODE_CASE, Y);
		}
		if ((regExp.flags() & Pattern.UNIX_LINES) == Pattern.UNIX_LINES) {
			ci.setAttribute(FileTypesSAXParser.ATTR_UNIX_LINES, Y);
		}
		ci.setText(regExp.toString());

		return ci;
	}
}
