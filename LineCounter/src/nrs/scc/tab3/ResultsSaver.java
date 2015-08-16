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
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.table.TableModel;

/**
 * Saves the contents of a table model to a csv file
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class ResultsSaver
{
	private TableModel tm;
	private static final String COMMA = ",";
	
	public ResultsSaver(TableModel tm)
	{
		this.tm = tm;
	}
	
	public void saveFile(File saveFile) throws IOException
	{
		PrintWriter writer = new PrintWriter(saveFile);
		StringBuilder sb = new StringBuilder();
		
		// write header
		int col;
		int colCount = tm.getColumnCount();
		for (col=0; col<colCount; col++)
		{
			sb.append(tm.getColumnName(col));
			sb.append(COMMA);
		}
		sb.setLength(sb.length()-1);
		writer.println(sb.toString());
		
		// write lines
		for (int row=0; row<tm.getRowCount(); row++)
		{
			sb.setLength(0);
			for (col=0; col<colCount; col++)
			{
				sb.append(tm.getValueAt(row, col));
				sb.append(COMMA);
			}
			sb.setLength(sb.length()-1);
			writer.println(sb.toString());
		}
		writer.close();
	}
}
