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

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * Popup menu for tables
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class TablePopupMenu extends JPopupMenu implements ActionListener
{
	private SourceCodeCounterView sccView;
	private File f;

	private static final String COPY = "copy";
	private static final String COPY_CONTENTS = "copyContents";
	private static final String SYSTEM_EDIT = "sysEdit";
	private static final String SYSTEM_VIEW = "sysView";
	private static final String VIEW = "view";

	/**
	 * Create a new TablePopupMenu
	 * @param filePath absolute file path
	 */
	public TablePopupMenu(SourceCodeCounterView sccView, String filePath)
	{
		this.sccView = sccView;
		
		assert filePath != null : "filePath is null";
		f = new File(filePath);
		assert f.exists() : filePath+" doesn't exist";
		
		ResourceMap rMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(TablePopupMenu.class);

		JMenuItem mi = new JMenuItem(rMap.getString("view.file"), rMap.getIcon("view.file.icon"));
		mi.setMnemonic(KeyEvent.VK_1);
		mi.setActionCommand(VIEW);
		mi.addActionListener(this);
		add(mi);

		addSeparator();

		mi = new JMenuItem(rMap.getString("copy.clip"), rMap.getIcon("copy.clip.icon"));
		mi.setMnemonic(KeyEvent.VK_2);
		mi.setActionCommand(COPY);
		mi.addActionListener(this);
		add(mi);

		mi = new JMenuItem(rMap.getString("copy.contents"), rMap.getIcon("copy.contents.icon"));
		mi.setMnemonic(KeyEvent.VK_3);
		mi.setActionCommand(COPY_CONTENTS);
		mi.addActionListener(this);
		add(mi);

		// system viewer/editor options
		if (Desktop.isDesktopSupported())
		{
			addSeparator();

			if (Desktop.getDesktop().isSupported(Action.OPEN))
			{
				mi = new JMenuItem(rMap.getString("sys.view"), rMap.getIcon("sys.view.icon"));
				mi.setMnemonic(KeyEvent.VK_4);
				mi.setActionCommand(SYSTEM_VIEW);
				mi.addActionListener(this);
				add(mi);
			}

			if (Desktop.getDesktop().isSupported(Action.EDIT))
			{
				mi = new JMenuItem(rMap.getString("sys.edit"), rMap.getIcon("sys.edit.icon"));
				mi.setMnemonic(KeyEvent.VK_5);
				mi.setActionCommand(SYSTEM_EDIT);
				mi.addActionListener(this);
				add(mi);
			}
		}
	}

	/**
	 * Show an error dialog
	 * @param ex the exception
	 */
	private void showError(Exception ex)
	{
		ResourceMap rMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(TablePopupMenu.class);
		JOptionPane.showMessageDialog(sccView.getFrame(), ex.getMessage(), rMap.getString("error"), JOptionPane.ERROR_MESSAGE);
	}

	/** Copy file contents to the clipboard */
	private void copyFileContents()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			BufferedReader r = new BufferedReader(new FileReader(f));
			String line;
			while ((line = r.readLine()) != null)
			{
				sb.append(line);
				sb.append("\n");
			}
			r.close();
			if (sb.length() > 1) {
				sb.setLength(sb.length()-1);
			}

			StringSelection selection = new StringSelection(sb.toString());
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(selection, selection);
		}
		catch (Exception ex)
		{
			showError(ex);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		ResourceMap rMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(TablePopupMenu.class);

		if (command.equals(COPY))
		{
			StringSelection selection = new StringSelection(f.getAbsolutePath());
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(selection, selection);
			sccView.setStatusMessage(rMap.getString("copied"));
		}
		else if (command.equals(VIEW))
		{
			sccView.showFile(f.getAbsolutePath());
		}
		else if (command.equals(COPY_CONTENTS))
		{
			copyFileContents();
			sccView.setStatusMessage(rMap.getString("copied"));
		}
		else if (command.equals(SYSTEM_EDIT))
		{
			sccView.setStatusMessage(rMap.getString("launching"));
			try {
				Desktop.getDesktop().edit(f);
			} catch (IOException ioe) {
				showError(ioe);
			}
		}
		else if (command.equals(SYSTEM_VIEW))
		{
			sccView.setStatusMessage(rMap.getString("launching"));
			try {
				Desktop.getDesktop().open(f);
			} catch (IOException ioe) {
				showError(ioe);
			}
		}
	}
}
