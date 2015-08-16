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

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Uses the default table header renderer with centre aligned text
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class CentreTableHeaderRenderer extends DefaultTableCellRenderer
{
	private TableCellRenderer renderer;
	
	public CentreTableHeaderRenderer(TableCellRenderer renderer)
	{
		this.renderer = renderer;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		JLabel label = (JLabel)renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}
}
