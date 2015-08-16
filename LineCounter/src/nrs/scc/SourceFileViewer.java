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

package nrs.scc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import nrs.scc.tab2.CountItem;
import nrs.scc.tab3.FileCounter;
import nrs.scc.tab3.NumberCellRenderer;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * Source file viewer
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SourceFileViewer extends JDialog
{
	private final static Logger log = Logger.getLogger(SourceFileViewer.class.getName());
	private String text;
	private DefaultStyledDocument doc;
	private Style highlightStyle;
	private static final Matcher NEW_LINE_MATCHER = Pattern.compile(FileCounter.NEW_LINE_PATTERN, Pattern.MULTILINE).matcher("");

	public SourceFileViewer(Frame parent)
	{
		super(parent, false);
		initComponents();
	}

    /** Creates new form SourceFileViewer */
    public SourceFileViewer(JFrame parent, String sourceFileName, String charSet)
	{
        super(parent, false);
        initComponents();
		
		assert sourceFileName != null : "Source file specified is null";

		StyleContext sc = StyleContext.getDefaultStyleContext();
		Style defaultStyle = textPane.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
		highlightStyle = sc.addStyle("HighlightStyle", defaultStyle);
		StyleConstants.setBold(highlightStyle, true);
		StyleConstants.setForeground(highlightStyle, Color.YELLOW);
		StyleConstants.setBackground(highlightStyle, Color.BLUE);
		doc = (DefaultStyledDocument)textPane.getStyledDocument();
		
		try
		{
			File sourceFile = new File(sourceFileName);
			byte[] readBuffer = new byte[5000];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			while ((read = bis.read(readBuffer, 0, 5000)) != -1)
			{
				baos.write(readBuffer, 0, read);
			}
			bis.close();

			// TextPane will always 'display' newlines as \n so when searching
			// we need to match up otherwise any highlights will be in the wrong place
			text = baos.toString(charSet).replace("\r\n", "\n");

			textPane.setText(text);
			textPane.setCaretPosition(0);
			setTabStops();
			
			setTitle(getTitle()+" - "+sourceFile.getName());

			// capture the escape key for quick closure of the dialog
			KeyListener escListener = new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						dispose();
					}
				}
			};
			textPane.addKeyListener(escListener);
			regexTable.addKeyListener(escListener);
		}
		catch (FileNotFoundException fnex)
		{
			log.log(Level.WARNING, "File does not exist "+sourceFileName, fnex);
		}
		catch (IOException ioex)
		{
			log.log(Level.WARNING, "Cannot read from file "+sourceFileName, ioex);
		}
    }

	private void setTabStops()
	{
		FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
		int charWidth = fm.charWidth('m');
		int tabWidth = charWidth * 4;

		TabStop[] tabs = new TabStop[10];
		for (int j=0; j<tabs.length; j++)
		{
			int tab = j + 1;
			tabs[j] = new TabStop(tab * tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = textPane.getDocument().getLength();
		textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, true);
	}

	/**
	 * Count the number of lines in a piece of text
	 * @param text the text to process
	 * @return number of lines
	 */
	private int countLines(CharSequence text)
	{
		NEW_LINE_MATCHER.reset(text);
		int newLines = 0;
		while (NEW_LINE_MATCHER.find())
		{
			newLines++;
		}

		if (text.length() > 0)
		{
			// one line of text not terminated by a new line
			if (newLines == 0) {
				newLines = 1;
			}
			// count last line as most regex matches won't finish on a new line
			else if (text.charAt(text.length()-1) != '\n') {
				newLines++;
			}
		}

		return newLines;
	}

	/**
	 * Applies the Pattern to the text to find which ranges match
	 * @param p the Pattern to apply
	 * @return the number of matching lines
	 */
	private int addRange(Pattern p, boolean highlight)
	{
		int matchingLines = 0;
		int nl;
		
		Matcher m = p.matcher(text);
		while (m.find())
		{
			if (highlight) {
				doc.setCharacterAttributes(m.start(), m.end()-m.start(), highlightStyle, true);
			}
			nl = countLines(m.group());
			matchingLines += (nl == 0 ? 1 : nl);
		}
		
		return matchingLines;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(800, 600);
	}

	/**
	 * Set the regexes that are relevant for this file type
	 * @param countItems
	 */
	public void setRegexes(List<CountItem> countItems)
	{
		DefaultTableModel dtm = (DefaultTableModel)regexTable.getModel();
		Vector<Object> row;
		int matchingLines;

		// add blank lines
		matchingLines = addRange(Pattern.compile(FileCounter.BLANK_LINE_PATTERN, Pattern.MULTILINE), false);
		ResourceMap resourceMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(SourceFileViewer.class);
		row = new Vector<Object>(3);
		row.add(resourceMap.getString("blank.lines"));
		row.add(null);
		row.add(matchingLines);
		dtm.addRow(row);

		// add total lines
		matchingLines = countLines(text);
		row = new Vector<Object>(3);
		row.add(resourceMap.getString("total.lines"));
		row.add(null);
		row.add(matchingLines);
		dtm.addRow(row);

		// create line numbers
		lineNums.setRows(matchingLines);
		StringBuilder sb = new StringBuilder(matchingLines*3);
		for (int i=1; i<=matchingLines; i++)
		{
			sb.append(i);
			sb.append("\n");
		}
		sb.setLength(sb.length()-1);
		lineNums.setText(sb.toString());
		lineNums.setCaretPosition(0);

		if (!countItems.isEmpty())
		{
			for (CountItem ci : countItems)
			{
				matchingLines = addRange(ci.regExp, true);
				row = new Vector<Object>(3);
				row.add(ci.name);
				row.add(ci.regExp.toString());
				row.add(matchingLines);
				dtm.addRow(row);
			}
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        textScroll = new javax.swing.JScrollPane();
        tablePanel = new javax.swing.JPanel();
        lineNums = new javax.swing.JTextArea();
        textPane = new javax.swing.JTextPane();
        regexScroll = new javax.swing.JScrollPane();
        regexTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(nrs.scc.SourceCodeCounter.class).getContext().getResourceMap(SourceFileViewer.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        splitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        splitPane.setDividerLocation(450);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setName("splitPane"); // NOI18N
        splitPane.setOneTouchExpandable(true);

        textScroll.setName("textScroll"); // NOI18N

        tablePanel.setName("tablePanel"); // NOI18N
        tablePanel.setLayout(new java.awt.BorderLayout());

        lineNums.setColumns(4);
        lineNums.setEditable(false);
        lineNums.setFont(resourceMap.getFont("lineNums.font")); // NOI18N
        lineNums.setRows(5);
        lineNums.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        lineNums.setName("lineNums"); // NOI18N
        tablePanel.add(lineNums, java.awt.BorderLayout.WEST);

        textPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        textPane.setEditable(false);
        textPane.setFont(resourceMap.getFont("textPane.font")); // NOI18N
        textPane.setName("textPane"); // NOI18N
        tablePanel.add(textPane, java.awt.BorderLayout.CENTER);

        textScroll.setViewportView(tablePanel);

        splitPane.setTopComponent(textScroll);

        regexScroll.setName("regexScroll"); // NOI18N

        regexTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Description", "Regular Expression", "Count"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        regexTable.setColumnSelectionAllowed(true);
        regexTable.setName("regexTable"); // NOI18N
        regexTable.getTableHeader().setReorderingAllowed(false);
        regexScroll.setViewportView(regexTable);
        regexTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        regexTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("regexTable.columnModel.title0")); // NOI18N
        regexTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("regexTable.columnModel.title1")); // NOI18N
        regexTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("regexTable.columnModel.title2")); // NOI18N
        regexTable.getColumnModel().getColumn(2).setCellRenderer(new NumberCellRenderer());

        splitPane.setBottomComponent(regexScroll);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[])
	{
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SourceFileViewer dialog = new SourceFileViewer(new javax.swing.JFrame());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea lineNums;
    private javax.swing.JScrollPane regexScroll;
    private javax.swing.JTable regexTable;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextPane textPane;
    private javax.swing.JScrollPane textScroll;
    // End of variables declaration//GEN-END:variables
}
