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

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import nrs.scc.tab2.FileTypes;

/**
 * Highlights unknown file types in red
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileTypesListRenderer extends DefaultListCellRenderer
{
	private FileTypes fileTypes;
	
	public FileTypesListRenderer()
	{
	}

	/**
	 * Set the file types object used to check for valid types
	 * @param fileTypes
	 */
	public void setFileTypes(FileTypes fileTypes)
	{
		this.fileTypes = fileTypes;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (fileTypes != null)
		{
			String[] parts = value.toString().split(" "); // file type and number, e.g. java (5)
			if (fileTypes.isKnownFileType(parts[0]))
			{
				if (isSelected)
				{
					c.setForeground(Color.WHITE);
				}
				else
				{
					c.setForeground(Color.BLACK);
				}
			}
			else
			{
				if (isSelected)
				{
					c.setForeground(Color.WHITE);
					c.setBackground(Color.RED);
				}
				else
				{
					c.setForeground(Color.RED);
					c.setBackground(Color.WHITE);
				}
			}
		}
		
		return c;
	}
}
