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

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying the results
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class ResultsTableModel extends AbstractTableModel
{
	private String[] colNames;
	private Class[] colTypes = {String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class};
	private List<FileDetails> rows = new ArrayList<FileDetails>();

	public ResultsTableModel(String[] colNames)
	{
		this.colNames = colNames;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		Class c = null;
		if (columnIndex > -1 && columnIndex < colTypes.length) {
			c = colTypes[columnIndex];
		}
		return c;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		String name = null;
		if (columnIndex > -1 && columnIndex < colNames.length) {
			name = colNames[columnIndex];
		}
		return name;
	}

	@Override
	public int getColumnCount()
	{
		return colNames.length;
	}

	@Override
	public int getRowCount()
	{
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		FileDetails details = rows.get(rowIndex);
		if (details != null)
		{
			switch (columnIndex)
			{
				case 0: return details.getPath();
				case 1: return details.getFileType();
				case 2: return details.totalLines;
				case 3: return details.blankLines;
				case 4: return details.countedLines;
				case 5: return details.sourceLines;
			}
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
		
	public void clear()
	{
		if (!rows.isEmpty())
		{
			int size = rows.size();
			rows.clear();
			fireTableRowsDeleted(0, size-1);
		}
	}

	/**
	 * Add a number of FileDetails to the table
	 * @param detailsList
	 */
	public void addFileDetails(List<FileDetails> detailsList)
	{
		assert detailsList != null : "detailsList cannot be null";
		assert !detailsList.isEmpty() : "detailsList should be > 0";

		int firstRow = rows.size();
		rows.addAll(detailsList);
		int lastRow = rows.size()-1;
		fireTableRowsInserted(firstRow, lastRow);
	}
}
