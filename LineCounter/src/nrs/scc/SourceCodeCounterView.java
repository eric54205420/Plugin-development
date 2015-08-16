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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import nrs.scc.tab1.CentreTableCellRenderer;
import nrs.scc.tab1.FileSelectionTableModel;
import nrs.scc.tab2.CountItem;
import nrs.scc.tab2.FileType;
import nrs.scc.tab2.FileTypes;
import nrs.scc.tab2.CounterTreeModel;
import nrs.scc.tab2.ValidPatternDocumentListener;
import nrs.scc.tab3.CSVFileFilter;
import nrs.scc.tab3.FileComparator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import nrs.scc.tab1.FileSelectionTransferHandler;
import nrs.scc.tab1.DateColumnRenderer;
import javax.swing.DropMode;
import nrs.scc.tab1.FileTypesListModel;
import nrs.scc.tab1.SizeColumnRenderer;
import nrs.scc.tab3.FileCounterManager;
import nrs.scc.tab3.FileDetails;
import nrs.scc.tab3.NumberCellRenderer;
import nrs.scc.tab3.ResultsTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashSet;
import java.util.logging.ConsoleHandler;
import java.util.regex.PatternSyntaxException;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import nrs.scc.tab1.CentreTableHeaderRenderer;
import nrs.scc.tab1.FileSelectionIOHandler;
import nrs.scc.tab1.FileTypesListRenderer;
import nrs.scc.tab1.SCCPFileFilter;
import nrs.scc.tab2.FileTypesSAXParser;
import nrs.scc.tab2.CounterTreeCellRenderer;
import nrs.scc.tab3.ResultsDialog;
import nrs.scc.tab3.ResultsSaver;
import nrs.scc.tab3.ResultsTableHeader;
import nrs.scc.tab3.SummaryResultsTableModel;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The application's main frame.
 * @todo allow usage from the command line.
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SourceCodeCounterView extends FrameView
{
	private FileTypesListModel ftLM;
	private FileTypesListRenderer ftLR = new FileTypesListRenderer();
	private ResultsTableModel resultsTM;
	private CounterTreeModel counterTM = new CounterTreeModel();
	private FileTypes fileTypes;
	private SummaryResultsTableModel summaryResultsTM;
	private TaskMonitor taskMonitor;
	private FileSelectionTransferHandler fstHandler;
	private FileSelectionTableModel fsTM;
	private String characterSet = Charset.defaultCharset().name();
	private int fileCountThreads = 1;
	private String unknownFilesFileType;

	private static final String DEFAULT_FILE_TYPES = "/nrs/scc/tab2/filetypes.xml";
	private static final String CHAR_SET = "charSet";
	private static final String THREADS = "threads";
	private static final String CBL = "countBlankLines";
	private static final String UTF8 = "UTF-8";
	private static final String SCCP = "SCCProfile.sccp";
	private static final String SCC_FILE_SELECTION = "SCC-FileSelection.txt";
	private static final String UNKNOWN_FILES_FILE_TYPE = "unknownFilesFileType";

	private final static Logger log = Logger.getLogger(SourceCodeCounterView.class.getName());

	private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String XML_ROOT = "<SCCProfile xmlns=\"http://www.nsydenham.net/java/SCC/profile/1\">";

	// logging set-up
	{
		Logger topLevel = Logger.getLogger("nrs.scc");
		topLevel.setLevel(Level.WARNING);
		Handler h = new ConsoleHandler();
		h.setLevel(Level.ALL);
		topLevel.addHandler(h);
	}

	class FileTypesTreeSelectionListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent e)
		{
			TreePath path = e.getPath();
			TreeNode node = (TreeNode)path.getLastPathComponent();
			if (node instanceof CountItem)
			{
				CountItem ci = (CountItem)node;
				ci.setSelected(!ci.isSelected());
			}
		}
	}

    public SourceCodeCounterView(SingleFrameApplication app)
	{
        super(app);

        initComponents();

		// set platform default character set
		Enumeration<AbstractButton> benum = csButtonGroup.getElements();
		AbstractButton button;
		while (benum.hasMoreElements())
		{
			button = benum.nextElement();
			if (characterSet.equals(button.getText()))
			{
				button.setSelected(true);
				break;
			}
		}
		otherCSMI.setToolTipText(characterSet);
		
        // connecting action tasks to status bar via TaskMonitor
        taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName))
				{
                    if (!busyIconTimer.isRunning())
					{
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                }
				else if ("done".equals(propertyName))
				{
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                }
				else if ("message".equals(propertyName))
				{
                    String text = (String)(evt.getNewValue());
					setStatusMessage((text == null) ? "" : text);
                }
				else if ("progress".equals(propertyName))
				{
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
		
		ResourceMap resourceMap = getResourceMap();

		// tab 1
		statusMessageLabel.setText(resourceMap.getString("tab1.status"));
		String[] colNames = { resourceMap.getString("file.name"), resourceMap.getString("file.type"),
			resourceMap.getString("file.size"), resourceMap.getString("file.date") };
		fsTM = new FileSelectionTableModel(colNames);
		fileSelectionTable.setModel(fsTM);
		
		// set renderers for columns
		TableColumnModel fsTCM = fileSelectionTable.getColumnModel();
		CentreTableCellRenderer ctcRenderer = new CentreTableCellRenderer();
		fsTCM.getColumn(1).setCellRenderer(ctcRenderer);
		fsTCM.getColumn(2).setCellRenderer(new SizeColumnRenderer(resourceMap.getString("bytes"), resourceMap.getString("kb"), resourceMap.getString("mb")));
		fsTCM.getColumn(3).setCellRenderer(new DateColumnRenderer());
		
		// set renderers for headers
		CentreTableHeaderRenderer cthRenderer = new CentreTableHeaderRenderer(fileSelectionTable.getTableHeader().getDefaultRenderer());
		for (int i=1; i<fsTCM.getColumnCount(); i++)
		{
			fsTCM.getColumn(i).setHeaderRenderer(cthRenderer);
		}

		fileSelectionTable.setDropMode(DropMode.INSERT_ROWS);
		fileSelectionTable.setAutoCreateRowSorter(true);
		TableRowSorter<FileSelectionTableModel> rowSorter = new TableRowSorter<FileSelectionTableModel>(fsTM);
		rowSorter.setComparator(0, new FileComparator(resourceMap.getString("total")));
		fileSelectionTable.setRowSorter(rowSorter);

		fstHandler = new FileSelectionTransferHandler(this, getApplication(), taskMonitor, fsTM);
		fileSelectionScroll.setTransferHandler(fstHandler);
		
		ftLM = new FileTypesListModel();
		fileTypesList.setModel(ftLM);
		fileTypesList.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e)
			{
				int[] indexes = fileTypesList.getSelectedIndices();
				Set<String> filePatternSet = new HashSet<String>(indexes.length);
				for (int i=0; i<indexes.length; i++)
				{
					 filePatternSet.add(ftLM.getFileTypeAt(indexes[i]));
				}
				highlightFileTypes(filePatternSet);
			}
		});

		// tab 2
		try
		{
			loadFileTypes(new InputSource(getClass().getResourceAsStream(DEFAULT_FILE_TYPES)));

			fileTypesTree.setModel(counterTM);

			fileTypesList.setCellRenderer(ftLR);
			
			ToolTipManager.sharedInstance().registerComponent(fileTypesTree);
			fileTypesTree.setCellRenderer(new CounterTreeCellRenderer());
			fileTypesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			fileTypesTree.addTreeSelectionListener(new FileTypesTreeSelectionListener());
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Invalid file types XML", ex);
			System.exit(1);
		}

		// add a listener to highlight good/bad patterns
		regExp.setInputVerifier(new ValidPatternDocumentListener(this));
		
		// tab 3
		String[] resultsColNames = {resourceMap.getString("file.name"), resourceMap.getString("file.type"), resourceMap.getString("total.lines"),
			resourceMap.getString("blank.lines"), resourceMap.getString("counted.lines"), resourceMap.getString("rem.lines")};
		resultsTM = new ResultsTableModel(resultsColNames);
		resultsTable.setModel(resultsTM);
		resultsTable.setTableHeader(
			new ResultsTableHeader(resultsTable.getTableHeader().getDefaultRenderer(), resultsTable.getColumnModel(),
				resourceMap.getString("results.counted"), resourceMap.getString("results.remaining")));
		NumberCellRenderer ncr = new NumberCellRenderer();
		resultsTable.setDefaultRenderer(Number.class, ncr);
		resultsTable.getColumnModel().getColumn(1).setCellRenderer(ctcRenderer);
		TableRowSorter<ResultsTableModel> rowSorterResults = new TableRowSorter<ResultsTableModel>(resultsTM);
		rowSorterResults.setComparator(0, new FileComparator(resourceMap.getString("total")));
		resultsTable.setRowSorter(rowSorterResults);

		String[] summaryColNames = {resourceMap.getString("file.type"), resourceMap.getString("counter"), resourceMap.getString("total")};
		summaryResultsTM = new SummaryResultsTableModel(summaryColNames);
		resultsSummaryTable.setModel(summaryResultsTM);
		resultsSummaryTable.setDefaultRenderer(Number.class, ncr);
		resultsSummaryTable.setAutoCreateRowSorter(true);

		// tabbed pane mnemonics
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

		// set icon image
		getFrame().setIconImage(resourceMap.getImageIcon("mainFrame.imageIcon").getImage());
    }

	/**
	 * Set the message to display in the status bar
	 * @param message
	 */
	public void setStatusMessage(String message)
	{
		statusMessageLabel.setText(message);
		messageTimer.restart();
	}

	/**
	 * Parse an XML stream representing the File Types
	 * @param is the stream to parse
	 * @throws IOException cannot open the stream
	 * @throws SAXException error parsing the stream
	 */
	private Map<String, FileType> parseFileTypes(InputSource is) throws IOException, SAXException
	{
		log.entering("SourceCodeCounterView", "parseFileTypes");

		XMLReader reader = XMLReaderFactory.createXMLReader();
		FileTypesSAXParser saxParser = new FileTypesSAXParser(counterTM.getRoot());
		reader.setContentHandler(saxParser);
		reader.setErrorHandler(saxParser);
		reader.parse(is);

		log.exiting("SourceCodeCounterView", "parseFileTypes");
		return saxParser.getFileTypes();
	}

    @Action
    public void showAboutBox()
	{
        if (aboutBox == null)
		{
			JFrame mainFrame = SourceCodeCounter.getApplication().getMainFrame();
            aboutBox = new SourceCodeCounterAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SourceCodeCounter.getApplication().show(aboutBox);
    }

	/**
	 * Load file types from the specified input source and update
	 * interested parties
	 * @param is
	 */
	void loadFileTypes(InputSource is)
	{
		log.entering("SourceCodeCounterView", "loadFileTypes");

		try
		{
			Map<String, FileType> ftMap = parseFileTypes(is);
			fileTypes = new FileTypes(ftMap);
			counterTM.setFileTypes(ftMap);
			fsTM.setFileTypes(fileTypes);
			ftLR.setFileTypes(fileTypes);
			tab2FTLabel.setForeground(Color.RED);
			tab2FTLabel.setToolTipText(getResourceMap().getString("changed.ft"));
		} catch (IOException ioex) {
			log.log(Level.SEVERE, "Error opening user file types", ioex);
		} catch (SAXException sex) {
			log.log(Level.SEVERE, "Error parsing user file types", sex);
		}

		log.exiting("SourceCodeCounterView", "loadFileTypes");
	}

	/**
	 * Application has been created and set-up. Now apply any previously stored config.
	 * @param userPrefs user preferences
	 */
	void finishedInit(Map<String, String> userPrefs)
	{
		log.entering("SourceCodeCounterView", "finishedInit");
		if (userPrefs != null)
		{
			String cs = userPrefs.get(CHAR_SET);
			if (cs != null)
			{
				try
				{
					Charset.forName(cs); // check it's valid
					setSelectedCharacterSet(cs);
				} catch (IllegalCharsetNameException ice) {
					log.warning("Invalid character set specified: "+cs);
				}
			}

			if (userPrefs.get(THREADS) != null)
			{
				try {
					fileCountThreads = Integer.parseInt(userPrefs.get(THREADS));
					switch (fileCountThreads)
					{
						case 2:
							twoThreadMI.setSelected(true);
							break;

						case 4:
							fourThreadMI.setSelected(true);
							break;

						case 8:
							eightThreadMI.setSelected(true);
							break;

						default:
							oneThreadMI.setSelected(true);
							break;
					}
				} catch (NumberFormatException nfex) {
					log.warning("Invalid number of threads: "+userPrefs.get(THREADS));
				}
			}

			if (userPrefs.get(CBL) != null)
			{
				countBlankLinesMI.setSelected(Boolean.valueOf(userPrefs.get(CBL)));
			}

			if (userPrefs.get(UNKNOWN_FILES_FILE_TYPE) != null)
			{
				unknownFilesFileType = userPrefs.get(UNKNOWN_FILES_FILE_TYPE).toString();
				otherCBMI.setSelected(true);
				otherCBMI.setToolTipText(userPrefs.get(UNKNOWN_FILES_FILE_TYPE));
			}
		}
		
		log.exiting("SourceCodeCounterView", "finishedInit");
	}

	/** Notification method that the user has dropped some files onto the file selection table */
	public void dropFilesStarted()
	{
		log.entering("SourceCodeCounterView", "dropFilesStarted");
		progressBar.setIndeterminate(true);
		toggleScreenBlock(false);
		log.exiting("SourceCodeCounterView", "dropFilesStarted");
	}
	
	/**
	 * Open the file types tree so that relevant file types are expanded
	 * @param fileExtensions - if null, all children will be collapsed
	 */
	private void setExpandedFileTypes(Map<String, Integer> fileExtensions)
	{
		// collapse all children
		TreePath child;
		for (int i=0; i<counterTM.getChildCount(counterTM.getRoot()); i++)
		{
			Object[] path = { counterTM.getRoot(), counterTM.getChild(counterTM.getRoot(), i) };
			child = new TreePath(path);
			fileTypesTree.collapsePath(child);
		}

		if (fileExtensions != null)
		{
			FileType ft;
			List<FileType> fileTypeExtList = new ArrayList<FileType>();
			for (String ext : fileExtensions.keySet())
			{
				ft = fileTypes.getFileType(ext);
				if (ft != null) {
					fileTypeExtList.add(ft);
				}
			}

			// expacnd selected nodes
			List<TreePath> tpaths = counterTM.getSelectedTreePaths(fileTypeExtList);
			for (TreePath path : tpaths)
			{
				fileTypesTree.expandPath(path);
			}
		}
	}

	/**
	 * The file selection has been updated
	 * @param fileTypes
	 */
	public void dropFilesFinished(Map<String, Integer> fileTypes)
	{
		log.entering("SourceCodeCounterView", "dropFilesFinished");
		assert fileTypes != null : "fileTypes cannot be null";
		
		if (!fileTypes.isEmpty())
		{
			ftLM.addFileTypes(fileTypes);
			totalField.setText(Integer.toString(fsTM.getRowCount()));
			setExpandedFileTypes(fileTypes);
			fileSelectionTable.setRowSelectionInterval(0, 0);
			toggleFileRelatedButtons(true);
		}

		toggleScreenBlock(true);
		log.exiting("SourceCodeCounterView", "dropFilesFinished");
	}
	
	/**
	 * Highlight files with the file type specified
	 * @param fileType
	 */
	private void highlightFileTypes(Set<String> filePatternSet)
	{
		log.entering("SourceCodeCounterView", "highlightFileTypes");
		assert filePatternSet != null : "filePatternSet cannot be null";
		
		fileSelectionTable.getSelectionModel().clearSelection();

		Set<Integer> findexes;
		List<Integer> indexes = new ArrayList<Integer>();
		for (String filePattern : filePatternSet)
		{
			findexes = fsTM.getIndexOfFilesMatchingPattern(filePattern);
			indexes.addAll(findexes);
		}

		int viewRow = 0;
		int firstRow = 99999999;
		for (int row : indexes)
		{
			viewRow = fileSelectionTable.convertRowIndexToView(row);
			fileSelectionTable.getSelectionModel().addSelectionInterval(viewRow, viewRow);
			if (viewRow < firstRow) {
				firstRow = viewRow;
			}
		}
		
		if (firstRow < fsTM.getRowCount())
		{
			fileSelectionTable.scrollRectToVisible(fileSelectionTable.getCellRect(firstRow, 0, true));
		}
		setStatusMessage(getResourceMap().getString("select.files", indexes.size()));
		log.exiting("SourceCodeCounterView", "highlightFileTypes");
	}

	/**
	 * This method is called when the FileCounterManager has finished
	 * @param countedFiles number of files processed
	 * @param time time taken to count the files
	 * @param errors a list of errors that were encountered
	 */
	public void countFinished(int countedFiles, float time, Map<String, String> errors)
	{
		log.entering("SourceCodeCounterView", "countFinished");
		
		countedField.setText(Integer.toString(countedFiles));
		String timeTaken = MessageFormat.format("{0,number}s", time);
		float msgsSec = (float)countedFiles/time;
		String msgsSecFormat = MessageFormat.format("{0,number,#.##}", msgsSec);
		countedField.setToolTipText(timeTaken);
		saveResultsButton.setEnabled(true);
		saveSummaryResultsButton.setEnabled(true);
		countButton.setEnabled(true);

		ResultsDialog resultsD = new ResultsDialog(getFrame(), countedFiles, timeTaken, fileCountThreads, msgsSecFormat, errors);
		resultsD.pack();
		resultsD.setLocationRelativeTo(getFrame());
		resultsD.setVisible(true);

		log.exiting("SourceCodeCounterView", "countFinished");
	}

	/**
	 * Get user preferences
	 * @return map of key/value pairs
	 * @see #finishedInit(java.util.Map) 
	 */
	Map<String, String> getUserPrefs()
	{
		Map<String, String> props = new HashMap<String, String>(2);
		props.put(CHAR_SET, characterSet);
		props.put(THREADS, Integer.toString(fileCountThreads));
		props.put(CBL, Boolean.toString(countBlankLinesMI.isSelected()));
		props.put(UNKNOWN_FILES_FILE_TYPE, unknownFilesFileType);

		return props;
	}

	/**
	 * Get an XML representation of the file types XML
	 * @return Element
	 */
	Element getFileTypesXML()
	{
		return counterTM.getXML();
	}

	/**
	 * Display the contents of the selected file
	 * @param fileName full file name, cannot be null
	 */
	void showFile(String fileName)
	{
		assert fileName != null : "fileName cannot be null";

		SourceFileViewer sfViewer = new SourceFileViewer(getFrame(), fileName, characterSet);
		List<CountItem> countItems = fileTypes.getSelectedPatterns(new FileDetails(new File(fileName)), unknownFilesFileType);
		if (countItems != null) {
			sfViewer.setRegexes(countItems);
		}
		sfViewer.pack();
		sfViewer.setLocationRelativeTo(getFrame());
		sfViewer.setVisible(true);
	}

	private void setRegExpFlags(CountItem ci)
	{
		int flags = ci.regExp.flags();
		Map<Integer, JCheckBox> optionMap = new HashMap<Integer, JCheckBox>(7);
		optionMap.put(Pattern.CANON_EQ, optionCE);
		optionMap.put(Pattern.CASE_INSENSITIVE, optionCI);
		optionMap.put(Pattern.COMMENTS, optionComments);
		optionMap.put(Pattern.DOTALL, optionDMA);
		optionMap.put(Pattern.LITERAL, optionLiteral);
		optionMap.put(Pattern.UNICODE_CASE, optionUnicodeCI);
		optionMap.put(Pattern.UNIX_LINES, optionUL);

		for (int flag: optionMap.keySet())
		{
			if ((flags & flag) == flag) {
				optionMap.get(flag).setSelected(true);
			} else {
				optionMap.get(flag).setSelected(false);
			}
		}
	}

	private void addCountItem(FileType parent)
	{
		CountItem ci = counterTM.addCountItem(parent);
		if (ci == null)
		{
			ResourceMap resourceMap = getResourceMap();
			JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("change.ci"),
				resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			Object[] path = { counterTM.getRoot(), parent, ci };
			TreePath tpath = new TreePath(path);
			fileTypesTree.getSelectionModel().setSelectionPath(tpath);
			fileTypesTree.scrollPathToVisible(tpath);
		}
	}

	private void saveCSVFile(TableModel tm, String defaultFileName)
	{
		if (tm.getRowCount() == 0) {
			return;
		}

		File f = null;
		JFileChooser jfc = new JFileChooser();
		jfc.setSelectedFile(new File(defaultFileName));
		jfc.setFileFilter(new CSVFileFilter(getResourceMap().getString("csv.filter")));

		if (jfc.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			ResourceMap resourceMap = getResourceMap();
			f = jfc.getSelectedFile();
			if (f.exists() && JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("overwrite", f.getName()),
				resourceMap.getString("confirm.overwite"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			{
				return;
			}

			try
			{
				ResultsSaver saver = new ResultsSaver(tm);
				saver.saveFile(f);
				setStatusMessage(resourceMap.getString("saved.to", f.getName()));
			}
			catch (IOException ioex)
			{
				log.log(Level.WARNING, "Cannot save csv file", ioex);
				JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("cannot.save", ioex.getMessage()),
					resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * User has selected an alternative character set
	 * @param selectedCharacterSet
	 */
	void setSelectedCharacterSet(String selectedCharacterSet)
	{
		otherCSMI.setToolTipText(selectedCharacterSet);
		characterSet = selectedCharacterSet;

		csButtonGroup.clearSelection();

		// see if one of the pre-defined charsets was selected
		Enumeration<AbstractButton> benum = csButtonGroup.getElements();
		AbstractButton button;
		while (benum.hasMoreElements())
		{
			button = benum.nextElement();
			if (button.getText().equals(characterSet))
			{
				button.setSelected(true);
				break;
			}
		}

	//	setStatusMessage(getResourceMap().getString("cs.set", characterSet));
	}

	/**
	 * Enable or diable all the buttons that rely on their being some files in the file selection table
	 * @param enabled true to enable, false to disable
	 */
	private void toggleFileRelatedButtons(boolean enabled)
	{
		removeAllButton.setEnabled(enabled);
		removeSelectedButton.setEnabled(enabled);
		countButton.setEnabled(enabled);
		selectUnknownButton.setEnabled(enabled);
		saveFSMI.setEnabled(enabled);
		saveProfileMI.setEnabled(enabled);
	}

	/**
	 * Enable or disable parts of the window
	 * @param toggle true for enabled, false for disabled
	 */
	private void toggleScreenBlock(boolean toggle)
	{
		tabbedPane.setEnabled(toggle);
		toggleFileRelatedButtons(toggle);
		addFilesButton.setEnabled(toggle);
		saveResultsButton.setEnabled(toggle);
		saveSummaryResultsButton.setEnabled(toggle);

		for (int i=0; i<menuBar.getMenuCount(); i++)
		{
			menuBar.getMenu(i).setEnabled(toggle);
		}
	}
	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
	 * @todo I18N mnemonics
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        step1SplitPane = new javax.swing.JSplitPane();
        fileTypesListPanel = new javax.swing.JPanel();
        fileTypesListScroll = new javax.swing.JScrollPane();
        fileTypesList = new javax.swing.JList();
        fileTypesLabel = new javax.swing.JLabel();
        fileSelectionPane = new javax.swing.JPanel();
        fstLabel = new javax.swing.JLabel();
        fileSelectionActionsPane = new javax.swing.JPanel();
        removeAllButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        totalLabel = new javax.swing.JLabel();
        totalField = new javax.swing.JTextField();
        addFilesButton = new javax.swing.JButton();
        selectUnknownButton = new javax.swing.JButton();
        fileSelectionScroll = new javax.swing.JScrollPane();
        fileSelectionTable = new javax.swing.JTable();
        step2SplitPane = new javax.swing.JSplitPane();
        fileTypesPanel = new javax.swing.JPanel();
        tab2FTLabel = new javax.swing.JLabel();
        fileTypesScroll = new javax.swing.JScrollPane();
        fileTypesTree = new javax.swing.JTree();
        ftButtonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        reloadButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        fileTypesEditPanel = new javax.swing.JPanel();
        fileTypeNameLabel = new javax.swing.JLabel();
        fileTypeNameTextField = new javax.swing.JTextField();
        filePatternsLabel = new javax.swing.JLabel();
        filePatternsTextField = new javax.swing.JTextField();
        regExpLabel = new javax.swing.JLabel();
        fileTypeUpdateButton = new javax.swing.JButton();
        regExpScroll = new javax.swing.JScrollPane();
        regExp = new javax.swing.JTextArea();
        optionsPanel = new javax.swing.JPanel();
        optionCI = new javax.swing.JCheckBox();
        optionUnicodeCI = new javax.swing.JCheckBox();
        optionCE = new javax.swing.JCheckBox();
        optionDMA = new javax.swing.JCheckBox();
        optionComments = new javax.swing.JCheckBox();
        optionLiteral = new javax.swing.JCheckBox();
        optionUL = new javax.swing.JCheckBox();
        resultsPane = new javax.swing.JPanel();
        resultsSplit = new javax.swing.JSplitPane();
        resultsScroll = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        resultSummaryScroll = new javax.swing.JScrollPane();
        resultsSummaryTable = new javax.swing.JTable();
        resultsButtonPanel = new javax.swing.JPanel();
        countButton = new javax.swing.JButton();
        saveResultsButton = new javax.swing.JButton();
        saveSummaryResultsButton = new javax.swing.JButton();
        countedLabel = new javax.swing.JLabel();
        countedField = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        saveFSMI = new javax.swing.JMenuItem();
        loadFSMI = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        saveProfileMI = new javax.swing.JMenuItem();
        loadProfileMI = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        countBlankLinesMI = new javax.swing.JCheckBoxMenuItem();
        charSetMenu = new javax.swing.JMenu();
        isoCSMI = new javax.swing.JRadioButtonMenuItem();
        asciiCSMI = new javax.swing.JRadioButtonMenuItem();
        utf8CSMI = new javax.swing.JRadioButtonMenuItem();
        utf16CSMI = new javax.swing.JRadioButtonMenuItem();
        csSeparator = new javax.swing.JSeparator();
        otherCSMI = new javax.swing.JMenuItem();
        fileCountersMenu = new javax.swing.JMenu();
        oneThreadMI = new javax.swing.JRadioButtonMenuItem();
        twoThreadMI = new javax.swing.JRadioButtonMenuItem();
        fourThreadMI = new javax.swing.JRadioButtonMenuItem();
        eightThreadMI = new javax.swing.JRadioButtonMenuItem();
        treatUnknownFilesMenu = new javax.swing.JMenu();
        ignoreCBMI = new javax.swing.JRadioButtonMenuItem();
        unknownSEparator = new javax.swing.JSeparator();
        otherCBMI = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        csButtonGroup = new javax.swing.ButtonGroup();
        threadButtonGroup = new javax.swing.ButtonGroup();
        unknownFilesButtonGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        tabbedPane.setName("tabbedPane"); // NOI18N
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        step1SplitPane.setDividerLocation(100);
        step1SplitPane.setName("step1SplitPane"); // NOI18N

        fileTypesListPanel.setName("fileTypesListPanel"); // NOI18N
        fileTypesListPanel.setLayout(new java.awt.BorderLayout());

        fileTypesListScroll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fileTypesListScroll.setName("fileTypesListScroll"); // NOI18N

        fileTypesList.setName("fileTypesList"); // NOI18N
        fileTypesListScroll.setViewportView(fileTypesList);

        fileTypesListPanel.add(fileTypesListScroll, java.awt.BorderLayout.CENTER);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(nrs.scc.SourceCodeCounter.class).getContext().getResourceMap(SourceCodeCounterView.class);
        fileTypesLabel.setBackground(resourceMap.getColor("fileTypesLabel.background")); // NOI18N
        fileTypesLabel.setDisplayedMnemonic('T');
        fileTypesLabel.setFont(fileTypesLabel.getFont().deriveFont(fileTypesLabel.getFont().getStyle() | java.awt.Font.BOLD));
        fileTypesLabel.setForeground(resourceMap.getColor("fileTypesLabel.foreground")); // NOI18N
        fileTypesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fileTypesLabel.setLabelFor(fileTypesList);
        fileTypesLabel.setText(resourceMap.getString("fileTypesLabel.text")); // NOI18N
        fileTypesLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 1, 5, 1));
        fileTypesLabel.setName("fileTypesLabel"); // NOI18N
        fileTypesLabel.setOpaque(true);
        fileTypesListPanel.add(fileTypesLabel, java.awt.BorderLayout.NORTH);

        step1SplitPane.setLeftComponent(fileTypesListPanel);

        fileSelectionPane.setName("fileSelectionPane"); // NOI18N
        fileSelectionPane.setLayout(new java.awt.BorderLayout());

        fstLabel.setBackground(resourceMap.getColor("fstLabel.background")); // NOI18N
        fstLabel.setDisplayedMnemonic('S');
        fstLabel.setFont(fstLabel.getFont().deriveFont(fstLabel.getFont().getStyle() | java.awt.Font.BOLD));
        fstLabel.setForeground(resourceMap.getColor("fstLabel.foreground")); // NOI18N
        fstLabel.setLabelFor(fileSelectionTable);
        fstLabel.setText(resourceMap.getString("fstLabel.text")); // NOI18N
        fstLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        fstLabel.setName("fstLabel"); // NOI18N
        fileSelectionPane.add(fstLabel, java.awt.BorderLayout.NORTH);

        fileSelectionActionsPane.setName("fileSelectionActionsPane"); // NOI18N
        fileSelectionActionsPane.setPreferredSize(new java.awt.Dimension(100, 40));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(nrs.scc.SourceCodeCounter.class).getContext().getActionMap(SourceCodeCounterView.class, this);
        removeAllButton.setAction(actionMap.get("removeAllFiles")); // NOI18N
        removeAllButton.setIcon(resourceMap.getIcon("removeAllButton.icon")); // NOI18N
        removeAllButton.setText(resourceMap.getString("removeAllButton.text")); // NOI18N
        removeAllButton.setToolTipText(resourceMap.getString("removeAllButton.toolTipText")); // NOI18N
        removeAllButton.setFocusable(false);
        removeAllButton.setName("removeAllButton"); // NOI18N

        removeSelectedButton.setAction(actionMap.get("removeSelectedFiles")); // NOI18N
        removeSelectedButton.setIcon(resourceMap.getIcon("removeSelectedButton.icon")); // NOI18N
        removeSelectedButton.setText(resourceMap.getString("removeSelectedButton.text")); // NOI18N
        removeSelectedButton.setToolTipText(resourceMap.getString("removeSelectedButton.toolTipText")); // NOI18N
        removeSelectedButton.setName("removeSelectedButton"); // NOI18N

        totalLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        totalLabel.setText(resourceMap.getString("totalLabel.text")); // NOI18N
        totalLabel.setName("totalLabel"); // NOI18N

        totalField.setColumns(4);
        totalField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        totalField.setText(resourceMap.getString("totalField.text")); // NOI18N
        totalField.setName("totalField"); // NOI18N

        addFilesButton.setAction(actionMap.get("addFiles")); // NOI18N
        addFilesButton.setIcon(resourceMap.getIcon("addFilesButton.icon")); // NOI18N
        addFilesButton.setText(resourceMap.getString("addFilesButton.text")); // NOI18N
        addFilesButton.setToolTipText(resourceMap.getString("addFilesButton.toolTipText")); // NOI18N
        addFilesButton.setName("addFilesButton"); // NOI18N

        selectUnknownButton.setAction(actionMap.get("selectUnknownFiles")); // NOI18N
        selectUnknownButton.setIcon(resourceMap.getIcon("selectUnknownButton.icon")); // NOI18N
        selectUnknownButton.setText(resourceMap.getString("selectUnknownButton.text")); // NOI18N
        selectUnknownButton.setToolTipText(resourceMap.getString("selectUnknownButton.toolTipText")); // NOI18N
        selectUnknownButton.setName("selectUnknownButton"); // NOI18N

        javax.swing.GroupLayout fileSelectionActionsPaneLayout = new javax.swing.GroupLayout(fileSelectionActionsPane);
        fileSelectionActionsPane.setLayout(fileSelectionActionsPaneLayout);
        fileSelectionActionsPaneLayout.setHorizontalGroup(
            fileSelectionActionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fileSelectionActionsPaneLayout.createSequentialGroup()
                .addContainerGap(479, Short.MAX_VALUE)
                .addComponent(selectUnknownButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeSelectedButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeAllButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addFilesButton)
                .addGap(18, 18, 18)
                .addComponent(totalLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        fileSelectionActionsPaneLayout.setVerticalGroup(
            fileSelectionActionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileSelectionActionsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileSelectionActionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalLabel)
                    .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeAllButton)
                    .addComponent(removeSelectedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFilesButton)
                    .addComponent(selectUnknownButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fileSelectionPane.add(fileSelectionActionsPane, java.awt.BorderLayout.SOUTH);

        fileSelectionScroll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fileSelectionScroll.setName("fileSelectionScroll"); // NOI18N

        fileSelectionTable.setAutoCreateRowSorter(true);
        fileSelectionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File name", "Type", "Size", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Long.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fileSelectionTable.setToolTipText(resourceMap.getString("fileSelectionTable.toolTipText")); // NOI18N
        fileSelectionTable.setName("fileSelectionTable"); // NOI18N
        fileSelectionTable.getTableHeader().setReorderingAllowed(false);
        fileSelectionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileSelectionTableMouseClicked(evt);
            }
        });
        fileSelectionTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fileSelectionTableKeyReleased(evt);
            }
        });
        fileSelectionScroll.setViewportView(fileSelectionTable);
        fileSelectionTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileSelectionTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        fileSelectionTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("fileSelectionTable.columnModel.title0")); // NOI18N
        fileSelectionTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        fileSelectionTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("fileSelectionTable.columnModel.title3")); // NOI18N
        fileSelectionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        fileSelectionTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("fileSelectionTable.columnModel.title1")); // NOI18N
        fileSelectionTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        fileSelectionTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("fileSelectionTable.columnModel.title2")); // NOI18N

        fileSelectionPane.add(fileSelectionScroll, java.awt.BorderLayout.CENTER);

        step1SplitPane.setRightComponent(fileSelectionPane);

        tabbedPane.addTab(resourceMap.getString("step1SplitPane.TabConstraints.tabTitle"), resourceMap.getIcon("step1SplitPane.TabConstraints.tabIcon"), step1SplitPane); // NOI18N

        step2SplitPane.setDividerLocation(200);
        step2SplitPane.setName("step2SplitPane"); // NOI18N

        fileTypesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        fileTypesPanel.setName("fileTypesPanel"); // NOI18N
        fileTypesPanel.setLayout(new java.awt.BorderLayout(0, 5));

        tab2FTLabel.setDisplayedMnemonic('T');
        tab2FTLabel.setFont(tab2FTLabel.getFont().deriveFont(tab2FTLabel.getFont().getStyle() | java.awt.Font.BOLD));
        tab2FTLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tab2FTLabel.setLabelFor(fileTypesTree);
        tab2FTLabel.setText(resourceMap.getString("tab2FTLabel.text")); // NOI18N
        tab2FTLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tab2FTLabel.setName("tab2FTLabel"); // NOI18N
        fileTypesPanel.add(tab2FTLabel, java.awt.BorderLayout.NORTH);

        fileTypesScroll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fileTypesScroll.setName("fileTypesScroll"); // NOI18N

        fileTypesTree.setName("fileTypesTree"); // NOI18N
        fileTypesTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                fileTypesTreeValueChanged(evt);
            }
        });
        fileTypesScroll.setViewportView(fileTypesTree);

        fileTypesPanel.add(fileTypesScroll, java.awt.BorderLayout.CENTER);

        ftButtonPanel.setName("ftButtonPanel"); // NOI18N
        ftButtonPanel.setLayout(new java.awt.GridLayout(1, 3, 5, 5));

        addButton.setAction(actionMap.get("addFileType")); // NOI18N
        addButton.setIcon(resourceMap.getIcon("addButton.icon")); // NOI18N
        addButton.setText(resourceMap.getString("addButton.text")); // NOI18N
        addButton.setToolTipText(resourceMap.getString("addButton.toolTipText")); // NOI18N
        addButton.setName("addButton"); // NOI18N
        ftButtonPanel.add(addButton);

        deleteButton.setAction(actionMap.get("deleteFileType")); // NOI18N
        deleteButton.setIcon(resourceMap.getIcon("deleteButton.icon")); // NOI18N
        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setToolTipText(resourceMap.getString("deleteButton.toolTipText")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N
        ftButtonPanel.add(deleteButton);

        reloadButton.setAction(actionMap.get("reloadFileTypes")); // NOI18N
        reloadButton.setIcon(resourceMap.getIcon("reloadButton.icon")); // NOI18N
        reloadButton.setText(resourceMap.getString("reloadButton.text")); // NOI18N
        reloadButton.setToolTipText(resourceMap.getString("reloadButton.toolTipText")); // NOI18N
        reloadButton.setName("reloadButton"); // NOI18N
        ftButtonPanel.add(reloadButton);

        searchButton.setAction(actionMap.get("findFileTypes")); // NOI18N
        searchButton.setIcon(resourceMap.getIcon("searchButton.icon")); // NOI18N
        searchButton.setText(resourceMap.getString("searchButton.text")); // NOI18N
        searchButton.setToolTipText(resourceMap.getString("searchButton.toolTipText")); // NOI18N
        searchButton.setName("searchButton"); // NOI18N
        ftButtonPanel.add(searchButton);

        fileTypesPanel.add(ftButtonPanel, java.awt.BorderLayout.SOUTH);

        step2SplitPane.setLeftComponent(fileTypesPanel);

        fileTypesEditPanel.setName("fileTypesEditPanel"); // NOI18N

        fileTypeNameLabel.setDisplayedMnemonic('N');
        fileTypeNameLabel.setLabelFor(fileTypeNameTextField);
        fileTypeNameLabel.setText(resourceMap.getString("fileTypeNameLabel.text")); // NOI18N
        fileTypeNameLabel.setName("fileTypeNameLabel"); // NOI18N

        fileTypeNameTextField.setColumns(20);
        fileTypeNameTextField.setText(resourceMap.getString("fileTypeNameTextField.text")); // NOI18N
        fileTypeNameTextField.setName("fileTypeNameTextField"); // NOI18N

        filePatternsLabel.setDisplayedMnemonic('P');
        filePatternsLabel.setLabelFor(filePatternsTextField);
        filePatternsLabel.setText(resourceMap.getString("filePatternsLabel.text")); // NOI18N
        filePatternsLabel.setName("filePatternsLabel"); // NOI18N

        filePatternsTextField.setColumns(20);
        filePatternsTextField.setText(resourceMap.getString("filePatternsTextField.text")); // NOI18N
        filePatternsTextField.setToolTipText(resourceMap.getString("filePatternsTextField.toolTipText")); // NOI18N
        filePatternsTextField.setName("filePatternsTextField"); // NOI18N

        regExpLabel.setDisplayedMnemonic('R');
        regExpLabel.setLabelFor(regExp);
        regExpLabel.setText(resourceMap.getString("regExpLabel.text")); // NOI18N
        regExpLabel.setName("regExpLabel"); // NOI18N

        fileTypeUpdateButton.setAction(actionMap.get("updateFileType")); // NOI18N
        fileTypeUpdateButton.setText(resourceMap.getString("fileTypeUpdateButton.text")); // NOI18N
        fileTypeUpdateButton.setName("fileTypeUpdateButton"); // NOI18N

        regExpScroll.setName("regExpScroll"); // NOI18N

        regExp.setColumns(20);
        regExp.setRows(5);
        regExp.setTabSize(4);
        regExp.setName("regExp"); // NOI18N
        regExpScroll.setViewportView(regExp);

        optionsPanel.setName("optionsPanel"); // NOI18N

        optionCI.setMnemonic('C');
        optionCI.setText(resourceMap.getString("optionCI.text")); // NOI18N
        optionCI.setToolTipText(resourceMap.getString("optionCI.toolTipText")); // NOI18N
        optionCI.setName("optionCI"); // NOI18N

        optionUnicodeCI.setMnemonic('U');
        optionUnicodeCI.setText(resourceMap.getString("optionUnicodeCI.text")); // NOI18N
        optionUnicodeCI.setToolTipText(resourceMap.getString("optionUnicodeCI.toolTipText")); // NOI18N
        optionUnicodeCI.setName("optionUnicodeCI"); // NOI18N

        optionCE.setMnemonic('E');
        optionCE.setText(resourceMap.getString("optionCE.text")); // NOI18N
        optionCE.setToolTipText(resourceMap.getString("optionCE.toolTipText")); // NOI18N
        optionCE.setName("optionCE"); // NOI18N

        optionDMA.setMnemonic('D');
        optionDMA.setText(resourceMap.getString("optionDMA.text")); // NOI18N
        optionDMA.setToolTipText(resourceMap.getString("optionDMA.toolTipText")); // NOI18N
        optionDMA.setName("optionDMA"); // NOI18N

        optionComments.setMnemonic('M');
        optionComments.setText(resourceMap.getString("optionComments.text")); // NOI18N
        optionComments.setToolTipText(resourceMap.getString("optionComments.toolTipText")); // NOI18N
        optionComments.setName("optionComments"); // NOI18N

        optionLiteral.setMnemonic('L');
        optionLiteral.setText(resourceMap.getString("optionLiteral.text")); // NOI18N
        optionLiteral.setToolTipText(resourceMap.getString("optionLiteral.toolTipText")); // NOI18N
        optionLiteral.setName("optionLiteral"); // NOI18N

        optionUL.setMnemonic('X');
        optionUL.setText(resourceMap.getString("optionUL.text")); // NOI18N
        optionUL.setToolTipText(resourceMap.getString("optionUL.toolTipText")); // NOI18N
        optionUL.setName("optionUL"); // NOI18N

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionCI)
                    .addComponent(optionUnicodeCI))
                .addGap(11, 11, 11)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(optionDMA)
                        .addGap(32, 32, 32))
                    .addComponent(optionCE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(optionComments)
                        .addGap(18, 18, 18)
                        .addComponent(optionUL))
                    .addComponent(optionLiteral)))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(optionCI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(optionUnicodeCI)
                            .addComponent(optionDMA)
                            .addComponent(optionLiteral)))
                    .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(optionCE)
                        .addComponent(optionComments)
                        .addComponent(optionUL)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout fileTypesEditPanelLayout = new javax.swing.GroupLayout(fileTypesEditPanel);
        fileTypesEditPanel.setLayout(fileTypesEditPanelLayout);
        fileTypesEditPanelLayout.setHorizontalGroup(
            fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileTypesEditPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileTypesEditPanelLayout.createSequentialGroup()
                        .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileTypeNameLabel)
                            .addComponent(filePatternsLabel))
                        .addGap(36, 36, 36)
                        .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileTypeNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
                            .addComponent(filePatternsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
                            .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fileTypeUpdateButton)))
                    .addGroup(fileTypesEditPanelLayout.createSequentialGroup()
                        .addComponent(regExpLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regExpScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)))
                .addContainerGap())
        );
        fileTypesEditPanelLayout.setVerticalGroup(
            fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileTypesEditPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTypeNameLabel)
                    .addComponent(fileTypeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filePatternsLabel)
                    .addComponent(filePatternsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(fileTypesEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileTypesEditPanelLayout.createSequentialGroup()
                        .addComponent(regExpScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(regExpLabel))
                .addGap(18, 18, 18)
                .addComponent(fileTypeUpdateButton)
                .addContainerGap(152, Short.MAX_VALUE))
        );

        step2SplitPane.setRightComponent(fileTypesEditPanel);

        tabbedPane.addTab(resourceMap.getString("step2SplitPane.TabConstraints.tabTitle"), resourceMap.getIcon("step2SplitPane.TabConstraints.tabIcon"), step2SplitPane); // NOI18N

        resultsPane.setName("resultsPane"); // NOI18N
        resultsPane.setLayout(new java.awt.BorderLayout());

        resultsSplit.setDividerLocation(300);
        resultsSplit.setDividerSize(10);
        resultsSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        resultsSplit.setName("resultsSplit"); // NOI18N
        resultsSplit.setOneTouchExpandable(true);

        resultsScroll.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("resultsScroll.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        resultsScroll.setName("resultsScroll"); // NOI18N

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File name", "Type", "Total lines", "Blank lines", "Counted lines", "Remaining lines"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsTable.setToolTipText(resourceMap.getString("resultsTable.toolTipText")); // NOI18N
        resultsTable.setName("resultsTable"); // NOI18N
        resultsTable.getTableHeader().setReorderingAllowed(false);
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultsTableMouseClicked(evt);
            }
        });
        resultsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resultsTableKeyReleased(evt);
            }
        });
        resultsScroll.setViewportView(resultsTable);
        resultsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        resultsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title0")); // NOI18N
        resultsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title5")); // NOI18N
        resultsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title1")); // NOI18N
        resultsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title2")); // NOI18N
        resultsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title3")); // NOI18N
        resultsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("resultsTable.columnModel.title4")); // NOI18N

        resultsSplit.setTopComponent(resultsScroll);

        resultSummaryScroll.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("resultSummaryScroll.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        resultSummaryScroll.setName("resultSummaryScroll"); // NOI18N

        resultsSummaryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File Type", "Counter", "Total"
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
        resultsSummaryTable.setName("resultsSummaryTable"); // NOI18N
        resultSummaryScroll.setViewportView(resultsSummaryTable);
        resultsSummaryTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("resultsSummaryTable.columnModel.title0")); // NOI18N
        resultsSummaryTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("resultsSummaryTable.columnModel.title1")); // NOI18N
        resultsSummaryTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("resultsSummaryTable.columnModel.title2")); // NOI18N

        resultsSplit.setRightComponent(resultSummaryScroll);

        resultsPane.add(resultsSplit, java.awt.BorderLayout.CENTER);

        resultsButtonPanel.setName("resultsButtonPanel"); // NOI18N

        countButton.setAction(actionMap.get("countFiles")); // NOI18N
        countButton.setFont(countButton.getFont().deriveFont(countButton.getFont().getStyle() | java.awt.Font.BOLD));
        countButton.setForeground(resourceMap.getColor("countButton.foreground")); // NOI18N
        countButton.setIcon(resourceMap.getIcon("countButton.icon")); // NOI18N
        countButton.setText(resourceMap.getString("countButton.text")); // NOI18N
        countButton.setToolTipText(resourceMap.getString("countButton.toolTipText")); // NOI18N
        countButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 20, 5, 20));
        countButton.setName("countButton"); // NOI18N

        saveResultsButton.setAction(actionMap.get("saveResults")); // NOI18N
        saveResultsButton.setIcon(resourceMap.getIcon("saveResultsButton.icon")); // NOI18N
        saveResultsButton.setText(resourceMap.getString("saveResultsButton.text")); // NOI18N
        saveResultsButton.setToolTipText(resourceMap.getString("saveResultsButton.toolTipText")); // NOI18N
        saveResultsButton.setName("saveResultsButton"); // NOI18N

        saveSummaryResultsButton.setAction(actionMap.get("saveSummaryResults")); // NOI18N
        saveSummaryResultsButton.setIcon(resourceMap.getIcon("saveSummaryResultsButton.icon")); // NOI18N
        saveSummaryResultsButton.setText(resourceMap.getString("saveSummaryResultsButton.text")); // NOI18N
        saveSummaryResultsButton.setToolTipText(resourceMap.getString("saveSummaryResultsButton.toolTipText")); // NOI18N
        saveSummaryResultsButton.setName("saveSummaryResultsButton"); // NOI18N

        countedLabel.setText(resourceMap.getString("countedLabel.text")); // NOI18N
        countedLabel.setName("countedLabel"); // NOI18N

        countedField.setColumns(5);
        countedField.setEditable(false);
        countedField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        countedField.setText(resourceMap.getString("countedField.text")); // NOI18N
        countedField.setName("countedField"); // NOI18N

        javax.swing.GroupLayout resultsButtonPanelLayout = new javax.swing.GroupLayout(resultsButtonPanel);
        resultsButtonPanel.setLayout(resultsButtonPanelLayout);
        resultsButtonPanelLayout.setHorizontalGroup(
            resultsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(countedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(countedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 681, Short.MAX_VALUE)
                .addComponent(saveSummaryResultsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveResultsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(countButton)
                .addContainerGap())
        );
        resultsButtonPanelLayout.setVerticalGroup(
            resultsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsButtonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(resultsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countButton)
                    .addComponent(saveResultsButton)
                    .addComponent(saveSummaryResultsButton)
                    .addComponent(countedLabel)
                    .addComponent(countedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        resultsPane.add(resultsButtonPanel, java.awt.BorderLayout.SOUTH);

        tabbedPane.addTab(resourceMap.getString("resultsPane.TabConstraints.tabTitle"), resourceMap.getIcon("resultsPane.TabConstraints.tabIcon"), resultsPane); // NOI18N

        mainPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setMnemonic('F');
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        saveFSMI.setAction(actionMap.get("saveFileSelection")); // NOI18N
        saveFSMI.setIcon(resourceMap.getIcon("saveFSMI.icon")); // NOI18N
        saveFSMI.setMnemonic('S');
        saveFSMI.setText(resourceMap.getString("saveFSMI.text")); // NOI18N
        saveFSMI.setToolTipText(resourceMap.getString("saveFSMI.toolTipText")); // NOI18N
        saveFSMI.setName("saveFSMI"); // NOI18N
        fileMenu.add(saveFSMI);

        loadFSMI.setAction(actionMap.get("loadFileSelection")); // NOI18N
        loadFSMI.setIcon(resourceMap.getIcon("loadFSMI.icon")); // NOI18N
        loadFSMI.setMnemonic('L');
        loadFSMI.setText(resourceMap.getString("loadFSMI.text")); // NOI18N
        loadFSMI.setName("loadFSMI"); // NOI18N
        fileMenu.add(loadFSMI);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        saveProfileMI.setAction(actionMap.get("saveProfile")); // NOI18N
        saveProfileMI.setIcon(resourceMap.getIcon("saveProfileMI.icon")); // NOI18N
        saveProfileMI.setMnemonic('A');
        saveProfileMI.setText(resourceMap.getString("saveProfileMI.text")); // NOI18N
        saveProfileMI.setToolTipText(resourceMap.getString("saveProfileMI.toolTipText")); // NOI18N
        saveProfileMI.setName("saveProfileMI"); // NOI18N
        fileMenu.add(saveProfileMI);

        loadProfileMI.setAction(actionMap.get("loadProfile")); // NOI18N
        loadProfileMI.setIcon(resourceMap.getIcon("loadProfileMI.icon")); // NOI18N
        loadProfileMI.setMnemonic('O');
        loadProfileMI.setText(resourceMap.getString("loadProfileMI.text")); // NOI18N
        loadProfileMI.setToolTipText(resourceMap.getString("loadProfileMI.toolTipText")); // NOI18N
        loadProfileMI.setName("loadProfileMI"); // NOI18N
        fileMenu.add(loadProfileMI);

        jSeparator2.setName("jSeparator2"); // NOI18N
        fileMenu.add(jSeparator2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        optionsMenu.setMnemonic('O');
        optionsMenu.setText(resourceMap.getString("optionsMenu.text")); // NOI18N
        optionsMenu.setName("optionsMenu"); // NOI18N

        countBlankLinesMI.setMnemonic('B');
        countBlankLinesMI.setSelected(true);
        countBlankLinesMI.setText(resourceMap.getString("countBlankLinesMI.text")); // NOI18N
        countBlankLinesMI.setToolTipText(resourceMap.getString("countBlankLinesMI.toolTipText")); // NOI18N
        countBlankLinesMI.setName("countBlankLinesMI"); // NOI18N
        optionsMenu.add(countBlankLinesMI);

        charSetMenu.setMnemonic('C');
        charSetMenu.setText(resourceMap.getString("charSetMenu.text")); // NOI18N
        charSetMenu.setToolTipText(resourceMap.getString("charSetMenu.toolTipText")); // NOI18N
        charSetMenu.setName("charSetMenu"); // NOI18N

        csButtonGroup.add(isoCSMI);
        isoCSMI.setMnemonic('I');
        isoCSMI.setText(resourceMap.getString("isoCSMI.text")); // NOI18N
        isoCSMI.setName("isoCSMI"); // NOI18N
        isoCSMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetChanged(evt);
            }
        });
        charSetMenu.add(isoCSMI);

        csButtonGroup.add(asciiCSMI);
        asciiCSMI.setMnemonic('A');
        asciiCSMI.setText(resourceMap.getString("asciiCSMI.text")); // NOI18N
        asciiCSMI.setName("asciiCSMI"); // NOI18N
        asciiCSMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetChanged(evt);
            }
        });
        charSetMenu.add(asciiCSMI);

        csButtonGroup.add(utf8CSMI);
        utf8CSMI.setMnemonic('U');
        utf8CSMI.setText(resourceMap.getString("utf8CSMI.text")); // NOI18N
        utf8CSMI.setName("utf8CSMI"); // NOI18N
        utf8CSMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetChanged(evt);
            }
        });
        charSetMenu.add(utf8CSMI);

        csButtonGroup.add(utf16CSMI);
        utf16CSMI.setMnemonic('T');
        utf16CSMI.setText(resourceMap.getString("utf16CSMI.text")); // NOI18N
        utf16CSMI.setName("utf16CSMI"); // NOI18N
        utf16CSMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetChanged(evt);
            }
        });
        charSetMenu.add(utf16CSMI);

        csSeparator.setName("csSeparator"); // NOI18N
        charSetMenu.add(csSeparator);

        otherCSMI.setAction(actionMap.get("selectCharSet")); // NOI18N
        otherCSMI.setText(resourceMap.getString("otherCSMI.text")); // NOI18N
        otherCSMI.setToolTipText(resourceMap.getString("otherCSMI.toolTipText")); // NOI18N
        otherCSMI.setName("otherCSMI"); // NOI18N
        charSetMenu.add(otherCSMI);

        optionsMenu.add(charSetMenu);

        fileCountersMenu.setMnemonic('F');
        fileCountersMenu.setText(resourceMap.getString("fileCountersMenu.text")); // NOI18N
        fileCountersMenu.setToolTipText(resourceMap.getString("fileCountersMenu.toolTipText")); // NOI18N
        fileCountersMenu.setName("fileCountersMenu"); // NOI18N

        threadButtonGroup.add(oneThreadMI);
        oneThreadMI.setMnemonic('1');
        oneThreadMI.setSelected(true);
        oneThreadMI.setText(resourceMap.getString("oneThreadMI.text")); // NOI18N
        oneThreadMI.setName("oneThreadMI"); // NOI18N
        oneThreadMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setThreads(evt);
            }
        });
        fileCountersMenu.add(oneThreadMI);

        threadButtonGroup.add(twoThreadMI);
        twoThreadMI.setMnemonic('2');
        twoThreadMI.setText(resourceMap.getString("twoThreadMI.text")); // NOI18N
        twoThreadMI.setName("twoThreadMI"); // NOI18N
        twoThreadMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setThreads(evt);
            }
        });
        fileCountersMenu.add(twoThreadMI);

        threadButtonGroup.add(fourThreadMI);
        fourThreadMI.setMnemonic('4');
        fourThreadMI.setText(resourceMap.getString("fourThreadMI.text")); // NOI18N
        fourThreadMI.setName("fourThreadMI"); // NOI18N
        fourThreadMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setThreads(evt);
            }
        });
        fileCountersMenu.add(fourThreadMI);

        threadButtonGroup.add(eightThreadMI);
        eightThreadMI.setMnemonic('8');
        eightThreadMI.setText(resourceMap.getString("eightThreadMI.text")); // NOI18N
        eightThreadMI.setName("eightThreadMI"); // NOI18N
        eightThreadMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setThreads(evt);
            }
        });
        fileCountersMenu.add(eightThreadMI);

        optionsMenu.add(fileCountersMenu);

        treatUnknownFilesMenu.setMnemonic('T');
        treatUnknownFilesMenu.setText(resourceMap.getString("treatUnknownFilesMenu.text")); // NOI18N
        treatUnknownFilesMenu.setName("treatUnknownFilesMenu"); // NOI18N

        ignoreCBMI.setAction(actionMap.get("setUnkownFilesFileType")); // NOI18N
        unknownFilesButtonGroup.add(ignoreCBMI);
        ignoreCBMI.setSelected(true);
        ignoreCBMI.setText(resourceMap.getString("ignoreFileType.text")); // NOI18N
        ignoreCBMI.setToolTipText(resourceMap.getString("ignoreFileType.toolTipText")); // NOI18N
        ignoreCBMI.setName("ignoreFileType"); // NOI18N
        treatUnknownFilesMenu.add(ignoreCBMI);

        unknownSEparator.setName("unknownSEparator"); // NOI18N
        treatUnknownFilesMenu.add(unknownSEparator);

        otherCBMI.setAction(actionMap.get("setUnkownFilesFileType")); // NOI18N
        unknownFilesButtonGroup.add(otherCBMI);
        otherCBMI.setText(resourceMap.getString("otherFileType.text")); // NOI18N
        otherCBMI.setToolTipText(resourceMap.getString("otherFileType.toolTipText")); // NOI18N
        otherCBMI.setName("otherFileType"); // NOI18N
        treatUnknownFilesMenu.add(otherCBMI);

        optionsMenu.add(treatUnknownFilesMenu);

        menuBar.add(optionsMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setToolTipText(resourceMap.getString("statusAnimationLabel.toolTipText")); // NOI18N
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
        statusAnimationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelFileCounter(evt);
            }
        });

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @see #loadFileSelection
	 * @param f
	 */
	void loadFileSelection(File f)
	{
		log.entering("SourceCodeCounterView", "loadFileSelection");
		try
		{
			FileSelectionIOHandler.readFileSelection(fstHandler, f);
		}
		catch (IOException ioex)
		{
			log.log(Level.WARNING, "Cannot load file selection", ioex);
			ResourceMap resourceMap = getResourceMap();
			JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("cannot.read", ioex.getMessage()),
				resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
		}
		log.exiting("SourceCodeCounterView", "loadFileSelection");
	}

	/** Loads a file selection previously saved to the hard disk */
	@Action
	public void loadFileSelection()
	{
		log.entering("SourceCodeCounterView", "loadFileSelection");
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(getResourceMap().getString("load.fs"));
		if (jfc.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			loadFileSelection(jfc.getSelectedFile());
		}
		log.exiting("SourceCodeCounterView", "loadFileSelection");
	}
	
	/** Saves any file selection that the user has made */
	@Action
	public void saveFileSelection()
	{
		if (fileSelectionTable.getModel().getRowCount() > 0)
		{
			ResourceMap rMap = getResourceMap();
			JFileChooser jfc = new JFileChooser();
			jfc.setSelectedFile(new File(SCC_FILE_SELECTION));
			jfc.setDialogTitle(rMap.getString("save.fs"));
			if (jfc.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File f = jfc.getSelectedFile();
				
				if (f.exists() &&
					JOptionPane.showConfirmDialog(getFrame(), rMap.getString("overwrite", f.getName()),
						rMap.getString("confirm.overwrite"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				{
					return;
				}

				try
				{
					FileSelectionIOHandler.saveFileSelection(fsTM, f);
					setStatusMessage(rMap.getString("saved.to", f.getAbsolutePath()));
				}
				catch (IOException ioex)
				{
					log.log(Level.WARNING, "Cannot save file selection", ioex);
					JOptionPane.showMessageDialog(getFrame(), ioex.getMessage(), rMap.getString("error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/** Count the files selected by the user while applying any counters */
	@Action
	public Task countFiles()
	{
		log.entering("SourceCodeCounterView", "countFiles");

		FileCounterManager fcManager = null;
		
		if (fsTM.getRowCount() > 0)
		{
			countedField.setText("0");
			countedField.setToolTipText(null);
			resultsTM.clear();
			summaryResultsTM.clear();
			boolean iterateOverUnknownFiles = (unknownFilesFileType != null);
			fcManager = new FileCounterManager(this, fsTM.getFilesCount(iterateOverUnknownFiles), fsTM.getFileIterator(iterateOverUnknownFiles),
				fileTypes, resultsTM, summaryResultsTM, characterSet, fileCountThreads, countBlankLinesMI.isSelected(),
				unknownFilesFileType);
			taskMonitor.setForegroundTask(fcManager);
			fcManager.setInputBlocker(new Task.InputBlocker(fcManager, Task.BlockingScope.COMPONENT, getFrame()) {
				@Override protected void block()
				{
					toggleScreenBlock(false);
				}

				@Override protected void unblock()
				{
					toggleScreenBlock(true);
				}
			});
		}
		
		log.exiting("SourceCodeCounterView", "countFiles");

		return fcManager;
	}

	/** Remove any files selected by the user */
	@Action
	public void removeSelectedFiles()
	{
		log.entering("SourceCodeCounterView", "removeSelectedFiles");

		int[] selectedRows = fileSelectionTable.getSelectedRows();
		int[] modelRows = new int[selectedRows.length];
		// convert to indexes in terms of the model
		for (int row=0; row<selectedRows.length; row++)
		{
			modelRows[row] = fileSelectionTable.convertRowIndexToModel(selectedRows[row]);
		}
		Map<String, Integer> fts = fsTM.removeRows(modelRows);
		totalField.setText(Integer.toString(fsTM.getRowCount()));
		ftLM.setFileTypes(fts);
		setExpandedFileTypes(fts);
		if (fsTM.getRowCount() > 0) {
			fileSelectionTable.getSelectionModel().setSelectionInterval(0, 0);
		} else {
			toggleFileRelatedButtons(false);
		}

		log.exiting("SourceCodeCounterView", "removeSelectedFiles");
	}

	/** Remove all the files in the file selection table */
	@Action
	public void removeAllFiles()
	{
		log.entering("SourceCodeCounterView", "removeAllFiles");

		ResourceMap resourceMap = getResourceMap();
		if (JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("remove.all"),
			resourceMap.getString("confirm.delete"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			ftLM.clear();
			fsTM.clear();
			totalField.setText("0");
			setExpandedFileTypes(null);
			toggleFileRelatedButtons(false);
		}

		log.exiting("SourceCodeCounterView", "removeAllFiles");
	}

/**
 * User selected a node in the tree
 * @param evt
 */
private void fileTypesTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_fileTypesTreeValueChanged
	Object selectedNode = fileTypesTree.getLastSelectedPathComponent();
	if (selectedNode != null)
	{
		if (selectedNode instanceof FileType)
		{
			FileType ft = (FileType)selectedNode;
			fileTypeNameTextField.setText(ft.getName());
			fileTypeNameLabel.setVisible(true);
			fileTypeNameTextField.setVisible(true);
			filePatternsLabel.setVisible(true);
			filePatternsTextField.setVisible(true);
			filePatternsTextField.setText(ft.getFilePatternsAsString());
			regExpLabel.setVisible(false);
			regExpScroll.setVisible(false);
			optionsPanel.setVisible(false);
			fileTypeUpdateButton.setVisible(true);
			fileTypeUpdateButton.setEnabled(true);
		}
		else if (selectedNode instanceof CountItem)
		{
			CountItem ci = (CountItem)selectedNode;
			fileTypeNameLabel.setVisible(true);
			fileTypeNameTextField.setVisible(true);
			fileTypeNameTextField.setText(ci.name);
			filePatternsLabel.setVisible(false);
			filePatternsTextField.setVisible(false);
			regExpLabel.setVisible(true);
			regExpScroll.setVisible(true);
			optionsPanel.setVisible(true);
			regExp.setText(ci.regExp.pattern());
			setRegExpFlags(ci);
			fileTypeUpdateButton.setVisible(true);
			fileTypeUpdateButton.setEnabled(true);
		}
		else
		{
			fileTypeNameLabel.setVisible(false);
			fileTypeNameTextField.setVisible(false);
			filePatternsLabel.setVisible(false);
			filePatternsTextField.setVisible(false);
			regExpLabel.setVisible(false);
			regExpScroll.setVisible(false);
			optionsPanel.setVisible(false);
			fileTypeUpdateButton.setVisible(false);
		}
	}
}//GEN-LAST:event_fileTypesTreeValueChanged

	/** Update the file type information */
	@Action
	public void updateFileType()
	{
		Object selectedNode = fileTypesTree.getLastSelectedPathComponent();
		assert selectedNode != null : "Cannot update tree node";

		ResourceMap resourceMap = getResourceMap();

		if (selectedNode instanceof FileType)
		{
			FileType ft = (FileType)selectedNode;
			if (fileTypeNameTextField.getText().isEmpty() || filePatternsTextField.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("enter.name.ext"),
					resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				ft.setName(fileTypeNameTextField.getText());
				String[] patterns = filePatternsTextField.getText().split(",\\s*");
				ft.setFilePatterns(Arrays.asList(patterns));
				counterTM.valueForPathChanged(fileTypesTree.getSelectionPath(), selectedNode);
				setStatusMessage(resourceMap.getString("ft.updated"));
			}
		}
		else if (selectedNode instanceof CountItem)
		{
			CountItem ci = (CountItem)selectedNode;
			if (fileTypeNameTextField.getText().isEmpty() || regExp.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("enter.name"),
					resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				ci.name = fileTypeNameTextField.getText();
				counterTM.valueForPathChanged(fileTypesTree.getSelectionPath(), selectedNode);

				try
				{
					// set up flags
					int flags = Pattern.MULTILINE;
					if (optionCE.isSelected()) {
						flags |= Pattern.CANON_EQ;
					}
					if (optionCI.isSelected()) {
						flags |= Pattern.CASE_INSENSITIVE;
					}
					if (optionDMA.isSelected()) {
						flags |= Pattern.DOTALL;
					}
					if (optionLiteral.isSelected()) {
						flags |= Pattern.LITERAL;
					}
					if (optionUL.isSelected()) {
						flags |= Pattern.UNIX_LINES;
					}
					if (optionUnicodeCI.isSelected()) {
						flags |= Pattern.UNICODE_CASE;
					}
					if (optionComments.isSelected()) {
						flags |= Pattern.COMMENTS;
					}
					// is it valid
					ci.regExp = Pattern.compile(regExp.getText(), flags);
					setStatusMessage(resourceMap.getString("ci.updated"));
				}
				catch (PatternSyntaxException psex)
				{
					JOptionPane.showMessageDialog(getFrame(), psex.getMessage(),
						resourceMap.getString("invalid.regexp"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/** Add a new file type or counter */
	@Action
	public void addFileType()
	{
		Object selectedNode = fileTypesTree.getLastSelectedPathComponent();
		if (selectedNode != null)
		{
			if (selectedNode.equals(counterTM.getRoot()))
			{
				FileType ft = counterTM.addFileType();
				if (ft == null)
				{
					ResourceMap resourceMap = getResourceMap();
					JOptionPane.showMessageDialog(getFrame(), resourceMap.getString("change.name"),
						resourceMap.getString("error"), JOptionPane.ERROR_MESSAGE);
					ft = counterTM.getNewFileType();
					assert ft != null : "Cannot get last added FileType";
				}

				// select relevant node
				Object[] path = new Object[2];
				path[0] = counterTM.getRoot();
				path[1] = ft;
				TreePath tpath = new TreePath(path);
				fileTypesTree.getSelectionModel().setSelectionPath(tpath);
				fileTypesTree.scrollPathToVisible(tpath);
			}
			else if (selectedNode instanceof FileType)
			{
				FileType ft = (FileType)selectedNode;
				addCountItem(ft);
			}
			else if (selectedNode instanceof CountItem)
			{
				CountItem ci = (CountItem)selectedNode;
				FileType ft = (FileType)ci.getParent();
				addCountItem(ft);
			}
		}
	}

	/** Delete the specified file type or counter */
	@Action
	public void deleteFileType()
	{
		log.entering("SourceCodeCounterView", "deleteFileType");

		TreePath[] paths = fileTypesTree.getSelectionPaths();
		if (paths != null)
		{
			ResourceMap resourceMap = getResourceMap();
			if (JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("sure"),
				resourceMap.getString("confirm.delete"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				TreeNode lastNode = (TreeNode)paths[0].getLastPathComponent();
				if (lastNode instanceof FileType)
				{
					counterTM.removeFileType((FileType)lastNode);
				}
				else if (lastNode instanceof CountItem)
				{
					counterTM.removeCountItem((CountItem)lastNode);
				}
			}
		}

		log.exiting("SourceCodeCounterView", "deleteFileType");
	}

	/** Save the results to a csv file */
	@Action
	public void saveResults()
	{
		saveCSVFile(resultsTM, "results.csv");
	}

	@Action
	public void saveSummaryResults()
	{
		saveCSVFile(summaryResultsTM, "sresults.csv");
	}

/** Cancel a running task */
private void cancelFileCounter(java.awt.event.MouseEvent evt)//GEN-FIRST:event_cancelFileCounter
{//GEN-HEADEREND:event_cancelFileCounter
	if (taskMonitor.getForegroundTask() != null)
	{
		taskMonitor.getForegroundTask().cancel(true);
	}
}//GEN-LAST:event_cancelFileCounter

	/** Add files to the file selection table */
	@Action
	public void addFiles()
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.setMultiSelectionEnabled(true);
		if (jfc.showOpenDialog(jfc) == JFileChooser.APPROVE_OPTION)
		{
			File[] files = jfc.getSelectedFiles();
			fstHandler.addFiles(Arrays.asList(files));
		}
	}

private void resultsTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultsTableMouseClicked
{//GEN-HEADEREND:event_resultsTableMouseClicked
	int index = resultsTable.getSelectedRow();
	if (index > -1)
	{
		int row = resultsTable.convertRowIndexToModel(index);
		String fileName = (String)resultsTM.getValueAt(row, 0);
		if (evt.getClickCount() > 1)
		{
			showFile(fileName);
		}
		else if (SwingUtilities.isRightMouseButton(evt) || SwingUtilities.isMiddleMouseButton(evt))
		{
			TablePopupMenu tpm = new TablePopupMenu(this, fileName);
			tpm.show(resultsTable, evt.getX(), evt.getY());
		}
	}
}//GEN-LAST:event_resultsTableMouseClicked

private void fileSelectionTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_fileSelectionTableMouseClicked
{//GEN-HEADEREND:event_fileSelectionTableMouseClicked
	int index = fileSelectionTable.getSelectedRow();
	if (index > -1)
	{
		int row = fileSelectionTable.convertRowIndexToModel(index);
		String fileName = (String)fsTM.getValueAt(row, 0);
		if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() > 1)
		{
			showFile(fileName);
		}
		else if (SwingUtilities.isRightMouseButton(evt) || SwingUtilities.isMiddleMouseButton(evt))
		{
			TablePopupMenu tpm = new TablePopupMenu(this, fileName);
			tpm.show(fileSelectionTable, evt.getX(), evt.getY());
		}
	}
}//GEN-LAST:event_fileSelectionTableMouseClicked

	/** Allow the user to select a character set */
	@Action
	public void selectCharSet()
	{
		SelectCharacterSetDialog selectCS = new SelectCharacterSetDialog(this, characterSet);
		selectCS.pack();
		selectCS.setLocationRelativeTo(getFrame());
		selectCS.setVisible(true);
	}

private void charsetChanged(java.awt.event.ActionEvent evt)//GEN-FIRST:event_charsetChanged
{//GEN-HEADEREND:event_charsetChanged
	JRadioButtonMenuItem csMI = (JRadioButtonMenuItem)evt.getSource();
	setSelectedCharacterSet(csMI.getText());
}//GEN-LAST:event_charsetChanged

	/**
	 * Save the state of the selected counters and files to an XML file
	 * @see #loadProfile(ActionEvent)
	 */
	@Action
	public void saveProfile()
	{
		ResourceMap rMap = getResourceMap();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(SCCP));
		fileChooser.setDialogTitle(rMap.getString("save.profile"));
		if (fileChooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File f = fileChooser.getSelectedFile();
			if (f.exists() &&
				JOptionPane.showConfirmDialog(getFrame(),
				rMap.getString("overwrite.file", f.getName()), rMap.getString("confirm.overwrite"),
				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			{
				return;
			}

			// using println rather than creating an XML DOM in case there are a lot of files to output
			PrintWriter writer = null;
			try
			{
				writer = new PrintWriter(fileChooser.getSelectedFile(), UTF8);
				writer.println(XML_PROLOG);
				writer.println(XML_ROOT);
				XMLOutputter xmlOut = new XMLOutputter();
				Format format = Format.getPrettyFormat();
				format.setOmitEncoding(true);
				xmlOut.setFormat(format);
				xmlOut.output(counterTM.getXML(), writer);
				writer.println("\n<Files>");
				Iterator<FileDetails> it = fsTM.getFileIterator(true);
				while (it.hasNext())
				{
					writer.println("<file>"+it.next().getPath()+"</file>");
				}
				writer.println("</Files>");
				writer.println("</SCCProfile>");
				setStatusMessage(rMap.getString("saved.profile", fileChooser.getSelectedFile().getAbsolutePath()));
			} catch (IOException ioex) {
				log.log(Level.WARNING, "Cannot save profile", ioex);
				JOptionPane.showMessageDialog(getFrame(), ioex.getMessage(), rMap.getString("error"), JOptionPane.ERROR_MESSAGE);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}

	/**
	 * @see #loadProfile
	 * @param f
	 */
	void loadProfile(File f)
	{
		try
		{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			FileTypesSAXParser saxParser = new FileTypesSAXParser(counterTM.getRoot());
			saxParser.setFileSelectionTableModel(fsTM);
			reader.setContentHandler(saxParser);
			reader.setErrorHandler(saxParser);
			reader.parse(new InputSource(new BufferedInputStream(new FileInputStream(f))));
			fileTypes = new FileTypes(saxParser.getFileTypes());
			counterTM.setFileTypes(saxParser.getFileTypes());
			ftLM.setFileTypes(fsTM.getFileTypesAdded());
			setExpandedFileTypes(fsTM.getFileTypesAdded());
			if (fsTM.getRowCount() > 0)
			{
				toggleFileRelatedButtons(true);
				totalField.setText(Integer.toString(fsTM.getRowCount()));
			}
			if (saxParser.getMissingFilesCount() > 0)
			{
				setStatusMessage(getResourceMap().getString("missing.files", saxParser.getMissingFilesCount()));
			}
		}
		catch (Exception ex)
		{
			log.log(Level.WARNING, "Cannot load profile", ex);
			JOptionPane.showMessageDialog(getFrame(), ex.getMessage(), getResourceMap().getString("error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Loads a profile
	 * @see #saveProfile(ActionEvent)
	 */
	@Action
	public void loadProfile()
	{
		log.entering("SourceCodeCounterView", "loadProfile");

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new SCCPFileFilter());
		fileChooser.setDialogTitle(getResourceMap().getString("load.profile"));

		if (fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			loadProfile(fileChooser.getSelectedFile());
		}

		log.exiting("SourceCodeCounterView", "loadProfile");
	}

	/**
	 * Reloads the default file types
	 * @param evt
	 */
	@Action
	public void reloadFileTypes()
	{
		log.entering("SourceCodeCounterView", "reloadFileTypes");

		ResourceMap resourceMap = getResourceMap();

		if (JOptionPane.showConfirmDialog(getFrame(), resourceMap.getString("reload.file.types"),
			resourceMap.getString("confirm.overwrite"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			InputStream is = getClass().getResourceAsStream(DEFAULT_FILE_TYPES);
			if (is == null)
			{
				log.warning("Cannot load default file types");
				JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("cannot.load.ft"), getResourceMap().getString("error"),
					JOptionPane.ERROR_MESSAGE);
			}
			else {
				loadFileTypes(new InputSource(is));
				tab2FTLabel.setForeground(Color.BLACK);
				tab2FTLabel.setToolTipText(null);
			}
		}

		log.exiting("SourceCodeCounterView", "reloadFileTypes");
	}

	/**
	 * Selects unknown and backup files in the file selection table
	 * @param evt
	 */
	@Action
	public void selectUnknownFiles()
	{
		log.entering("SourceCodeCounterView", "selectUnknownFiles");
		
		List<Integer> indices = new ArrayList<Integer>();
		String item;
		for (int i=0; i<ftLM.getSize(); i++)
		{
			item = ftLM.getFileTypeAt(i);
			if (item.endsWith("~") || !fileTypes.isKnownFileType(item)) {
				indices.add(i);
			}
		}
		
		int[] is = new int[indices.size()];
		for (int i=0; i<indices.size(); i++)
		{
			is[i] = indices.get(i);
		}
		fileTypesList.setSelectedIndices(is);

		log.exiting("SourceCodeCounterView", "selectUnknownFiles");
	}

	/** Sets how unknown files should be treated */
	@Action
	public void setUnkownFilesFileType()
	{
		ResourceMap rMap = getResourceMap();
		if (ignoreCBMI.isSelected())
		{
			unknownFilesFileType = null;
			setStatusMessage(rMap.getString("unknown.ignore"));
		}
		else
		{
			Object o = JOptionPane.showInputDialog(getFrame(), null, rMap.getString("select.file.type"), JOptionPane.QUESTION_MESSAGE,
				 null, fileTypes.getFileTypeNames(), unknownFilesFileType);
			if (o != null)
			{
				unknownFilesFileType = o.toString();
				otherCBMI.setToolTipText(unknownFilesFileType);
				setStatusMessage(rMap.getString("unknown.ft", unknownFilesFileType));
			}
			else if (unknownFilesFileType == null) // revert to ignore MI
			{
				ignoreCBMI.setSelected(true);
			}
		}
	}

	/** Find a file type in the tree */
	@Action
	public void findFileTypes()
	{
		ResourceMap rMap = getResourceMap();
		Object resp = JOptionPane.showInputDialog(getFrame(), rMap.getString("ft.search"), rMap.getString("ft.search.title"),
			JOptionPane.QUESTION_MESSAGE);
		if (resp != null)
		{
			Map<String, Integer> ftMap = new HashMap<String, Integer>();
			String[] types = resp.toString().split(",");
			for (String type : types)
			{
				ftMap.put(type.trim(), 0);
			}
			setExpandedFileTypes(ftMap);
		}
	}

	/**
	 * Clears selections on tab 1
	 */
	public void clearSelections()
	{
		fileTypesList.clearSelection();
		fileSelectionTable.clearSelection();
	}

	/**
	 * Set the number of threads used to count the files
	 * @param evt
	 */
private void setThreads(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setThreads
{//GEN-HEADEREND:event_setThreads
	JRadioButtonMenuItem miThread = (JRadioButtonMenuItem)evt.getSource();
	fileCountThreads = Integer.parseInt(miThread.getText());
	setStatusMessage(getResourceMap().getString("set.threads", fileCountThreads));
}//GEN-LAST:event_setThreads

private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_tabbedPaneStateChanged
{//GEN-HEADEREND:event_tabbedPaneStateChanged
	ResourceMap resourceMap = getResourceMap();
	switch (tabbedPane.getSelectedIndex())
	{
		case 0:
			statusMessageLabel.setText(resourceMap.getString("tab1.status"));
			getRootPane().setDefaultButton(addFilesButton);
			break;

		case 1:
			statusMessageLabel.setText(resourceMap.getString("tab2.status"));
			getRootPane().setDefaultButton(null);
			break;

		case 2:
			statusMessageLabel.setText(resourceMap.getString("tab3.status"));
			getRootPane().setDefaultButton(countButton);
			break;

		default:
			statusMessageLabel.setText("");
			break;
	}
	if (messageTimer != null) {
		messageTimer.restart();
	}
}//GEN-LAST:event_tabbedPaneStateChanged

	private void showTablePopupMenu(JTable table, KeyEvent evt)
	{
		int index = table.getSelectedRow();
		if (index > -1 && KeyEvent.VK_CONTEXT_MENU == evt.getKeyCode())
		{
			int row = table.convertRowIndexToModel(index);
			String fileName = (String)fsTM.getValueAt(row, 0);
			Point mouseP = table.getMousePosition();
			if (mouseP != null)
			{
				TablePopupMenu tpm = new TablePopupMenu(this, fileName);
				tpm.show(table, mouseP.x, mouseP.y);
			}
		}
	}

private void fileSelectionTableKeyReleased(java.awt.event.KeyEvent evt) //GEN-FIRST:event_fileSelectionTableKeyReleased
{//GEN-HEADEREND:event_fileSelectionTableKeyReleased
	showTablePopupMenu(fileSelectionTable, evt);
}//GEN-LAST:event_fileSelectionTableKeyReleased

private void resultsTableKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_resultsTableKeyReleased
{//GEN-HEADEREND:event_resultsTableKeyReleased
	showTablePopupMenu(resultsTable, evt);
}//GEN-LAST:event_resultsTableKeyReleased

/**
 * User wants to select a different charset
 * @param evt
 */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addFilesButton;
    private javax.swing.JRadioButtonMenuItem asciiCSMI;
    private javax.swing.JMenu charSetMenu;
    private javax.swing.JCheckBoxMenuItem countBlankLinesMI;
    private javax.swing.JButton countButton;
    private javax.swing.JTextField countedField;
    private javax.swing.JLabel countedLabel;
    private javax.swing.ButtonGroup csButtonGroup;
    private javax.swing.JSeparator csSeparator;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButtonMenuItem eightThreadMI;
    private javax.swing.JMenu fileCountersMenu;
    private javax.swing.JLabel filePatternsLabel;
    private javax.swing.JTextField filePatternsTextField;
    private javax.swing.JPanel fileSelectionActionsPane;
    private javax.swing.JPanel fileSelectionPane;
    private javax.swing.JScrollPane fileSelectionScroll;
    private javax.swing.JTable fileSelectionTable;
    private javax.swing.JLabel fileTypeNameLabel;
    private javax.swing.JTextField fileTypeNameTextField;
    private javax.swing.JButton fileTypeUpdateButton;
    private javax.swing.JPanel fileTypesEditPanel;
    private javax.swing.JLabel fileTypesLabel;
    private javax.swing.JList fileTypesList;
    private javax.swing.JPanel fileTypesListPanel;
    private javax.swing.JScrollPane fileTypesListScroll;
    private javax.swing.JPanel fileTypesPanel;
    private javax.swing.JScrollPane fileTypesScroll;
    private javax.swing.JTree fileTypesTree;
    private javax.swing.JRadioButtonMenuItem fourThreadMI;
    private javax.swing.JLabel fstLabel;
    private javax.swing.JPanel ftButtonPanel;
    private javax.swing.JRadioButtonMenuItem ignoreCBMI;
    private javax.swing.JRadioButtonMenuItem isoCSMI;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem loadFSMI;
    private javax.swing.JMenuItem loadProfileMI;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem oneThreadMI;
    private javax.swing.JCheckBox optionCE;
    private javax.swing.JCheckBox optionCI;
    private javax.swing.JCheckBox optionComments;
    private javax.swing.JCheckBox optionDMA;
    private javax.swing.JCheckBox optionLiteral;
    private javax.swing.JCheckBox optionUL;
    private javax.swing.JCheckBox optionUnicodeCI;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JRadioButtonMenuItem otherCBMI;
    private javax.swing.JMenuItem otherCSMI;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea regExp;
    private javax.swing.JLabel regExpLabel;
    private javax.swing.JScrollPane regExpScroll;
    private javax.swing.JButton reloadButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeSelectedButton;
    private javax.swing.JScrollPane resultSummaryScroll;
    private javax.swing.JPanel resultsButtonPanel;
    private javax.swing.JPanel resultsPane;
    private javax.swing.JScrollPane resultsScroll;
    private javax.swing.JSplitPane resultsSplit;
    private javax.swing.JTable resultsSummaryTable;
    private javax.swing.JTable resultsTable;
    private javax.swing.JMenuItem saveFSMI;
    private javax.swing.JMenuItem saveProfileMI;
    private javax.swing.JButton saveResultsButton;
    private javax.swing.JButton saveSummaryResultsButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton selectUnknownButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JSplitPane step1SplitPane;
    private javax.swing.JSplitPane step2SplitPane;
    private javax.swing.JLabel tab2FTLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.ButtonGroup threadButtonGroup;
    private javax.swing.JTextField totalField;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JMenu treatUnknownFilesMenu;
    private javax.swing.JRadioButtonMenuItem twoThreadMI;
    private javax.swing.ButtonGroup unknownFilesButtonGroup;
    private javax.swing.JSeparator unknownSEparator;
    private javax.swing.JRadioButtonMenuItem utf16CSMI;
    private javax.swing.JRadioButtonMenuItem utf8CSMI;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
