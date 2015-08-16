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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import nrs.scc.SourceCodeCounter;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * Stored summary results after applying the search and acts as the table model
 * for the bottom table on the 3rd tab
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SummaryResultsTableModel implements TableModel
{
	private String[] colNames;
	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
	private List<List<Object>> rows = new ArrayList<List<Object>>();

	public SummaryResultsTableModel(String[] colNames)
	{
		this.colNames = colNames;
	}

	private void notifyListeners(TableModelEvent e)
	{
		for (TableModelListener l : listeners)
		{
			l.tableChanged(e);
		}
	}

	private List<Object> getGrandTotalRow(String grandTotal, String counter, int total)
	{
		List<Object> row = new ArrayList<Object>(3);
		row.add(grandTotal);
		row.add(counter);
		row.add(total);
		return row;
	}

	/**
	 * Set the summary details
	 * @param summary
	 */
	public void setSummaryDetails(SummaryDetails summary)
	{
		Map<String, Map<String, Integer>> totals = summary.getSummaryTotals();
		Map<String, Integer> typeTotal;
		List<Object> row;
		for (String type : totals.keySet())
		{
			typeTotal = totals.get(type);
			for (String counter : typeTotal.keySet())
			{
				row = new ArrayList<Object>(3);
				row.add(type);
				row.add(counter);
				row.add(typeTotal.get(counter));
				rows.add(row);
			}
		}

		// add grand totals
		ResourceMap rMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(SummaryResultsTableModel.class);
		String gt = rMap.getString("grand.total");
		rows.add(getGrandTotalRow(gt, rMap.getString("blank.lines"), summary.getTotalBlankLines()));
		rows.add(getGrandTotalRow(gt, rMap.getString("counted.lines"), summary.getTotalCountedLines()));
		rows.add(getGrandTotalRow(gt, rMap.getString("rem.lines"), summary.getTotalSourceLines()));
		rows.add(getGrandTotalRow(gt, rMap.getString("total.lines"), summary.getTotalLines()));

		if (!rows.isEmpty())
		{
			TableModelEvent e = new TableModelEvent(this, 0, rows.size()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
			notifyListeners(e);
		}
	}

	@Override public String getColumnName(int column)
	{
		return colNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
			case 1:
				return String.class;
				
			case 2:
				return Integer.class;
				
			default:
				return String.class;
		}
	}

	public String[] getColNames()
	{
		return colNames;
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
		List row = rows.get(rowIndex);
		return row.get(columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		// no-op
	}
	
	// remove existing data
	public void clear()
	{
		if (!rows.isEmpty())
		{
			rows.clear();
			TableModelEvent tme = new TableModelEvent(this, 0, rows.size()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
			notifyListeners(tme);
		}
	}
}
