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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.LocalStorage;
import org.jdesktop.application.SingleFrameApplication;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

/**
 * The main class of the application.
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SourceCodeCounter extends SingleFrameApplication
{
	private SourceCodeCounterView sccView;
	private File fileToLoad;
	private final static String USER_PREFS= "userprefs.xml";
	private final static String USER_FILE_TYPES = "userfiletypes.xml";
	private final static Logger log = Logger.getLogger(SourceCodeCounter.class.getName());

	/**
	 * Initialise the app by reading any command line arguments
	 * @param args
	 */
	@Override
	protected void initialize(String[] args)
	{
		log.entering("SourceCodeCounter", "initialize", args.length);
		for (String arg : args)
		{
			if (arg.equalsIgnoreCase("-help"))
			{
				System.out.println("SourceCodeCounter <filen> where file can either be a list of files or a profile");
			}
			else
			{
				File file = new File(arg);
				if (file.exists()) {
					fileToLoad = file;
				} else {
					log.warning("Cannot load file "+args[0]);
				}
			}
		}
		log.exiting("SourceCodeCounter", "initialize");
	}

    /** At startup create and show the main frame of the application. */
    @Override
	protected void startup()
	{
		log.entering("SourceCodeCounter", "startup");
		sccView = new SourceCodeCounterView(this);
        show(sccView);
		log.exiting("SourceCodeCounter", "startup");
    }

	@Override
	protected void ready()
	{
		log.entering("SourceCodeCounter", "ready");

		String ftString = null;
		try
		{
			@SuppressWarnings("unchecked")
			Map<String, String> userPrefs = (Map<String, String>)getContext().getLocalStorage().load(USER_PREFS);
			ftString = (String)getContext().getLocalStorage().load(USER_FILE_TYPES);
			sccView.finishedInit(userPrefs);
		}
		catch (IOException ioex)
		{
			log.warning(ioex.getMessage());
			sccView.finishedInit(null);
		}

		boolean profileLoaded = false;
		if (fileToLoad != null)
		{
			if (fileToLoad.getName().endsWith("txt"))
			{
				sccView.loadFileSelection(fileToLoad);
			}
			else
			{
				sccView.loadProfile(fileToLoad);
				profileLoaded = true;
			}
		}

		// only load previous file types if nothing specified on the command line
		if (ftString != null && !profileLoaded) {
			sccView.loadFileTypes(new InputSource(new StringReader(ftString)));
		}

		log.exiting("SourceCodeCounter", "ready");
	}

	@Override
	protected void shutdown()
	{
		log.entering("SourceCodeCounter", "shutdown");
		// don't store the tabbed pane state as we always want to start from the first tab
		getContext().getSessionStorage().putProperty(JTabbedPane.class, null);

		// save file types state
		try
		{
			LocalStorage ls = getContext().getLocalStorage();
			ls.save(sccView.getUserPrefs(), USER_PREFS);
			XMLOutputter xmlOut = new org.jdom.output.XMLOutputter();
			xmlOut.setFormat(Format.getCompactFormat());
			ls.save(xmlOut.outputString(sccView.getFileTypesXML()), USER_FILE_TYPES);
		} catch (IOException ioe) {
			log.warning(ioe.getMessage());
		}
		log.exiting("SourceCodeCounter", "shutdown");
	}

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SourceCodeCounter
     */
    public static SourceCodeCounter getApplication() {
        return Application.getInstance(SourceCodeCounter.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SourceCodeCounter.class, args);
    }
}
