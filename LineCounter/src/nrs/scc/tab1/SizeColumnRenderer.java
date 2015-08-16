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
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Returns a formatted view of the size column
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SizeColumnRenderer extends DefaultTableCellRenderer
{
	private final static DecimalFormat formatter = new DecimalFormat("#0.##");
	private final static double MB = Math.pow(2, 20);
	private final static double KB = 1024;

	private String BYTES_LABEL = " bytes";
	private String KB_LABEL = " KB";
	private String MB_LABEL = " MB";

	/**
	 * New size renderer. Params are I18N labels.
	 * @param bytesLabel
	 * @param kbLabel
	 * @param mbLabel
	 */
	public SizeColumnRenderer(String bytesLabel, String kbLabel, String mbLabel)
	{
		setHorizontalAlignment(SwingConstants.CENTER);

		BYTES_LABEL = " "+bytesLabel;
		KB_LABEL = " "+kbLabel;
		MB_LABEL = " "+mbLabel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column)
	{
		JLabel comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if (value != null)
		{
			Long l = (Long)value;
			double dv;
			String suffix;
			if (l < KB)
			{
				suffix = BYTES_LABEL;
				dv = l;
			}
			else if (l < MB)
			{
				suffix = KB_LABEL;
				dv = (double)l/1024;
			}
			else
			{
				suffix = MB_LABEL;
				dv = (double)l / MB;
			}
			
			comp.setText(formatter.format(dv)+suffix);
		}
		return comp;
	}
}
