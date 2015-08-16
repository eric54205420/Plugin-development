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

import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import nrs.scc.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.TransferHandler;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * Handles dropping files and directories onto the file selection table
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileSelectionTransferHandler extends TransferHandler
{
	private SourceCodeCounterView sccView;
	private FileSelectionTableModel fstm;
	private Application app;
	private TaskMonitor monitor;
	private static final String FILE="file://";
	private static final Logger log = Logger.getLogger(FileSelectionTableModel.class.getName());
	
	private class AddFilesTask extends Task
	{
		private boolean cancel = false;
		private static final int FILE_CACHE_SIZE = 50;
		private List<File> fileList;
		private List<File> fileListCache = new ArrayList<File>(FILE_CACHE_SIZE);
		
		public AddFilesTask(List<File> fileList)
		{
			super(app);
			this.fileList = fileList;
		}

		private void addFile(File f)
		{
			fileListCache.add(f);
			if (fileListCache.size() == FILE_CACHE_SIZE)
			{
				fstm.addFiles(fileListCache);
				fileListCache.clear();
			}
		}

		private void addFilesInDirectory(File f)
		{
			assert f.isDirectory() : "File is not a directory "+f.getAbsolutePath();

			List<File> dirList = new ArrayList<File>();

			for (File file : f.listFiles())
			{
				message("adding", file.getAbsolutePath());
				if (file.isDirectory()) {
					dirList.add(file);
				} else {
					addFile(file);
				}
			}

			for (File dir : dirList)
			{
				addFilesInDirectory(dir);
			}
		}
	
		@Override protected Object doInBackground() throws Exception
		{
			List<File> dirList = new ArrayList<File>();
			Iterator it = fileList.iterator();
			File file;
			while (it.hasNext() && !cancel)
			{
				file = (File)it.next();
				message("adding", file.getAbsolutePath());
				if (file.isDirectory()) {
					dirList.add(file);
				} else {
					addFile(file);
				}
			}

			for (File dir : dirList)
			{
				addFilesInDirectory(dir);
			}

			fileList = null;
			
			return null;
		}

		@Override protected void succeeded(Object arg0)
		{
			if (!fileListCache.isEmpty())
			{
				fstm.addFiles(fileListCache);
				fileListCache.clear();
			}
			sccView.dropFilesFinished(fstm.getFileTypesAdded());
		}

		@Override protected void cancelled()
		{
			cancel = true;
			if (!fileListCache.isEmpty())
			{
				fstm.addFiles(fileListCache);
				fileListCache.clear();
			}
			sccView.dropFilesFinished(fstm.getFileTypesAdded());
		}
	}
	
	public FileSelectionTransferHandler(SourceCodeCounterView sccView, Application app, TaskMonitor monitor, FileSelectionTableModel fstm)
	{
		this.sccView = sccView;
		this.app = app;
		this.monitor = monitor;
		this.fstm = fstm;
	}
	
	@Override
	public boolean canImport(TransferSupport support)
	{
		for (DataFlavor flavor : support.getDataFlavors())
		{
			if (flavor.equals(DataFlavor.javaFileListFlavor) || flavor.equals(DataFlavor.stringFlavor)) {
				return true;
			}
		}

		return false;
	}
	
	public void addFiles(List<File> files)
	{
		if (files != null && files.size() > 0)
		{
			sccView.dropFilesStarted();
			AddFilesTask addFiles = new AddFilesTask(files);
			monitor.setForegroundTask(addFiles);
			addFiles.execute();
		}
	}
	
	@Override public boolean importData(TransferSupport support)
	{
		boolean imported = false;

		if (canImport(support))
		{
			sccView.clearSelections();
			try
			{
				// need to figure out best way of doing the drop as different OS's do it differently
				DataFlavor[] flavors = support.getTransferable().getTransferDataFlavors();
				for (DataFlavor flavor : flavors)
				{
					// Windows way of dropping files
					if (flavor.equals(DataFlavor.javaFileListFlavor))
					{
						@SuppressWarnings("unchecked")
						List<File> fileList = (List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						addFiles(fileList);
					}
					// Linux way of dropping files!?!
					else if (flavor.equals(DataFlavor.stringFlavor))
					{
						String fileNameList = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
						String[] fileNames = fileNameList.split("\n");
						URI u;
						File f;
						List<File> fileList = new ArrayList<File>(fileNames.length);
						for (String fileName : fileNames)
						{
							if (fileName.startsWith(FILE))
							{
								u = new URI(fileName);
								f = new File(u.getPath());
							}
							else {
								f = new File(fileName);
							}
							if (f.exists()) {
								fileList.add(f);
							}
						}
						fileNameList = null;
						fileNames = null;
						addFiles(fileList);
					}
				}
				imported = true;
			} catch (Exception ex) {
				log.log(Level.WARNING, "Cannot import data", ex);
			}
		}

		return imported;
	}
}
