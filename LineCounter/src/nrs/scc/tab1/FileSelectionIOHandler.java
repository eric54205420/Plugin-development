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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;

/**
 * Saves a file selection to a file and can read a previously saved file
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileSelectionIOHandler
{
	public static void saveFileSelection(TableModel tm, File f) throws IOException
	{
		assert tm != null : "TableModel is null";
		assert f != null : "File to save to is null";
		
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			for (int row=0; row<tm.getRowCount(); row++)
			{
				writer.println(tm.getValueAt(row, 0));
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Loads a list of files from the specified file
	 * @param fstHandler
	 * @param f the text file containing a list of file name
	 * @throws java.io.IOException
	 */
	public static void readFileSelection(FileSelectionTransferHandler fstHandler, File f) throws IOException
	{
		assert fstHandler != null : "FileSelectionTransferHandler is null";
		assert f != null : "File to read from is null";
		
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(f));
			String line;
			List<File> files = new ArrayList<File>();
			File inputFile;
			while ((line = reader.readLine()) != null)
			{
				inputFile = new File(line);
				if (inputFile.exists()) {
					files.add(inputFile);
				}
			}
			fstHandler.addFiles(files);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
