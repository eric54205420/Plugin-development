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

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import nrs.scc.tab1.CentreTableHeaderRenderer;

/**
 * Provides tool tips for the results table header columns
 * 
 * @author Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class ResultsTableHeader extends JTableHeader
{
	private String counted;
	private String remaining;
	
	public ResultsTableHeader(TableCellRenderer headerRenderer, TableColumnModel cm, String counted, String rem)
	{
		super(cm);

		this.counted = counted;
		this.remaining = rem;

		CentreTableHeaderRenderer cthRenderer = new CentreTableHeaderRenderer(headerRenderer);
		for (int i=1; i<cm.getColumnCount(); i++)
		{
			cm.getColumn(i).setHeaderRenderer(cthRenderer);
		}
	}
	
	@Override
	public String getToolTipText(MouseEvent event)
	{
		Point p = event.getPoint();
		int index = columnModel.getColumnIndexAtX(p.x);
		int realIndex = columnModel.getColumn(index).getModelIndex();
		String tt = null;
		switch (realIndex)
		{
			case 4:
				tt = counted;
				break;

			case 5:
				tt = remaining;
				break;

			default:
				tt = null;
				break;
		}

		return tt;
	}
}
