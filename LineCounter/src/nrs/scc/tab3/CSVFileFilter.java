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
import javax.swing.filechooser.FileFilter;

/**
 * CSV file filter
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class CSVFileFilter extends FileFilter
{
	private String filterDescription;

	public CSVFileFilter(String filterDescription)
	{
		this.filterDescription = filterDescription;
	}

	/**
	 * See if the file should be visible or not
	 * @param f file to check
	 * @return true if the file represents a directory or ends with .csv
	 */
	@Override public boolean accept(File f)
	{
		if (f.isDirectory()) {
			return true;
		}

		int index = f.getName().lastIndexOf('.');
		if (index > -1)
		{
			String ending = f.getName().substring(index+1);
			return ending.equalsIgnoreCase("csv");
		}
		return false;
	}

	/**
	 * Return a description for this file filter
	 * @return CSV Filter
	 */
	@Override public String getDescription()
	{
		return filterDescription;
	}
}
