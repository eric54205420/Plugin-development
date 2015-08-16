package nrs.scc.tab3;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class NumberCellRenderer extends DefaultTableCellRenderer
{
	private NumberFormat formatter = NumberFormat.getNumberInstance();
	
	public NumberCellRenderer()
	{
		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		label.setText(formatter.format(value));
		return label;
	}
}
