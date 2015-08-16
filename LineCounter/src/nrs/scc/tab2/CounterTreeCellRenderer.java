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

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

/**
 * Provides the rendering for the file types tree
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class CounterTreeCellRenderer implements TreeCellRenderer
{
	private JLabel fileTypeLabel = new JLabel();
	private JCheckBox countItemCheckBox = new JCheckBox();

	// set some defaults
	{	
		fileTypeLabel.setOpaque(true);
		countItemCheckBox.setBorderPaintedFlat(true);
		countItemCheckBox.setOpaque(true);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		JComponent comp;
		
		if (value instanceof FileType)
		{
			FileType ft = (FileType)value;
			fileTypeLabel.setText(ft.getName());
			if (!ft.getFilePatterns().isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				for (String pattern : ft.getFilePatterns())
				{
					sb.append(pattern);
					sb.append(",");
				}
				sb.setLength(sb.length()-1);
				fileTypeLabel.setToolTipText(sb.toString());
			}
			else
			{
				fileTypeLabel.setToolTipText(null);
			}
			comp = fileTypeLabel;
		}
		else if (value instanceof CountItem)
		{
			CountItem ci = (CountItem)value;
			countItemCheckBox.setText(ci.name);
			countItemCheckBox.setToolTipText(ci.regExp.pattern());
			countItemCheckBox.setSelected(ci.isSelected());
			comp = countItemCheckBox;
		}
		else
		{
			fileTypeLabel.setText(value.toString());
			comp = fileTypeLabel;
		}
		
		if (selected) {
			comp.setBackground(UIManager.getColor("Tree.selectionBackground"));
			comp.setForeground(UIManager.getColor("Tree.selectionForeground"));
		} else {
			comp.setBackground(UIManager.getColor("Tree.background"));
			comp.setForeground(UIManager.getColor("Tree.foreground"));
		}
		
		return comp;
	}
}
