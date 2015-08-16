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

import java.util.Comparator;

/**
 * Compares file names
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileComparator implements Comparator<String>
{
	private final static String SLASH = System.getProperty("file.separator");
	private String total = "Total";

	public FileComparator(String total)
	{
		this.total = total;
	}

	public int compare(String s1, String s2)
	{
		if (total.equals(s1)) {
			return 1;
		} else if (total.equals(s2)) {
			return -1;
		}
		
		int slashIndex1 = s1.lastIndexOf(SLASH);
		int slashIndex2 = s2.lastIndexOf(SLASH);
		
		if (slashIndex1 > -1 && slashIndex2 > -1)
		{
			String filePath1 = s1.substring(0, slashIndex1);
			String filePath2 = s2.substring(0, slashIndex2);

			if (filePath1.equals(filePath2)) {
				return s1.compareTo(s2);
			}

			if (filePath1.contains(filePath2)) {
				return 1;
			}

			if (filePath2.contains(filePath1)) {
				return -1;
			}
			
			String[] path1 = filePath1.split("\\"+SLASH);
			String[] path2 = filePath2.split("\\"+SLASH);
			String part1, part2;
			for (int i=0; i<path1.length; i++)
			{
				if (i < path2.length)
				{
					part1 = path1[i];
					part2 = path2[i];
					
					if (part1.equals(part2)) {
						continue;
					} else {
						return part1.compareTo(part2);
					}
				}
				else
				{
					return 1;
				}
			}
		}
		
		return s1.compareTo(s1);
	}
}
