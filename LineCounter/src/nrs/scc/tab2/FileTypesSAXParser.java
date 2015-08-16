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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.tree.TreeNode;
import nrs.scc.tab1.FileSelectionTableModel;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses the file types XML configuration primarily. Will also parse profile configuration files
 * including a list of files.
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class FileTypesSAXParser extends DefaultHandler
{
	private Locator locator;
	private TreeNode parentNode;
	private StringBuilder sb = new StringBuilder();
	private String type, countName, countID, countRef;
	private int regExpFlags;
	private boolean selected;
	private FileType ft;
	private Map<String, FileType> fileTypes = new HashMap<String, FileType>();
	private List<String> filePatterns;
	private List<CountItem> countItems;
	private Map<String, CountItem> commonCountItems = new HashMap<String, CountItem>();
	private FileSelectionTableModel fsTM;
	private List<File> fileSelection;
	private int missingFilesCount; // number of files in a profile that no longer exist

	static final String FILE_TYPES = "FileTypes";
	static final String FILE_TYPE = "FileType";
	static final String FILE_PATTERNS = "FilePatterns";
	static final String PATTERN = "pattern";
	static final String COUNT_ITEM = "CountItem";
	static final String COUNT_ITEMS = "CountItems";
	static final String FILE = "file";
	static final String FILES = "Files";
	static final String ATTR_NAME = "name";
	static final String ATTR_ID = "id";
	static final String ATTR_REF = "ref";
	static final String ATTR_SELECTED = "selected";
	static final String ATTR_TYPE = "type";
	static final String ATTR_VERSION = "version";

	// options
	static final String ATTR_CASE_INSENSITIVE = "ci";
	static final String ATTR_UNICODE_CASE = "uc";
	static final String ATTR_LITERAL = "literal";
	static final String ATTR_COMMENTS = "comments";
	static final String ATTR_UNIX_LINES = "ul";
	static final String ATTR_DOT_ALL = "dotall";
	static final String ATTR_CANON_EQUIV = "ce";

	private static final Map<String, Integer> attrMapping = new HashMap<String, Integer>(7);

	{
		attrMapping.put(ATTR_CANON_EQUIV, Pattern.CANON_EQ);
		attrMapping.put(ATTR_CASE_INSENSITIVE, Pattern.CASE_INSENSITIVE);
		attrMapping.put(ATTR_COMMENTS, Pattern.COMMENTS);
		attrMapping.put(ATTR_DOT_ALL, Pattern.DOTALL);
		attrMapping.put(ATTR_LITERAL, Pattern.LITERAL);
		attrMapping.put(ATTR_UNICODE_CASE, Pattern.UNICODE_CASE);
		attrMapping.put(ATTR_UNIX_LINES, Pattern.UNIX_LINES);
	}

	private static final Logger log = Logger.getLogger(FileTypesSAXParser.class.getName());
	
	public FileTypesSAXParser(TreeNode parentNode)
	{
		this.parentNode = parentNode;
	}

	/**
	 * Gets the file types as a map between a defined type (e.g. Java) and a FileType object
	 * @return Map<String, FileType>
	 */
	public Map<String, FileType> getFileTypes()
	{
		return fileTypes;
	}

	@Override
	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}
	
	/**
	 * Only required if parsing a profile config file
	 * @param fsTM
	 */
	public void setFileSelectionTableModel(FileSelectionTableModel fsTM)
	{
		this.fsTM = fsTM;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		sb.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals(PATTERN))
		{
			filePatterns.add(sb.toString().toLowerCase());
		}
		else if (qName.equals(COUNT_ITEM))
		{
			CountItem ci;
			if (countRef != null)
			{
				ci = commonCountItems.get(countRef);
				if (ci == null) {
					throw new SAXException("Cannot find CountItem for "+ft.getName()+"-"+countRef);
				}
				try
				{
					ci = (CountItem)ci.clone();
					ci.setFileType(ft);
				} catch (CloneNotSupportedException cnsex) {
					throw new SAXException("Cannot clone CountItem", cnsex);
				}
			}
			else
			{
				try {
					ci = new CountItem(ft, countName, Pattern.compile(sb.toString(), regExpFlags));
				} catch (PatternSyntaxException psex) {
					throw new SAXParseException("Invalid regular expession for CountItem "+countName, locator, psex);
				}
			}

			ci.setSelected(selected);
			
			if (countID != null) {
				commonCountItems.put(countID, ci);
			} else {
				countItems.add(ci);
			}
		}
		else if (qName.equals(FILE_TYPE))
		{
			ft.setCountItems(countItems);
			ft.setFilePatterns(filePatterns);
			fileTypes.put(type, ft);
		}
		else if (qName.equals(FILE))
		{
			File f = new File(sb.toString());
			if (f.exists()) {
				fileSelection.add(f);
			} else {
				missingFilesCount++;
			}
		}
		else if (qName.equals(FILES))
		{
			if (fsTM != null) {
				fsTM.addFiles(fileSelection);
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		sb.setLength(0);
		
		if (qName.equals(FILE_TYPE))
		{
			type = attributes.getValue(ATTR_TYPE);
			ft = new FileType(parentNode, type);
			filePatterns = new ArrayList<String>();
			countItems = new ArrayList<CountItem>();
		}
		else if (qName.equals(COUNT_ITEM))
		{
			countName = attributes.getValue(ATTR_NAME);
			countID = attributes.getValue(ATTR_ID);
			countRef = attributes.getValue(ATTR_REF);
			
			// see if any regex flags are set
			regExpFlags = Pattern.MULTILINE;
			for (String attr : attrMapping.keySet())
			{
				if (attributes.getValue(attr) != null) {
					regExpFlags |= attrMapping.get(attr);
				}
			}

			selected = Boolean.valueOf(attributes.getValue(ATTR_SELECTED));
		}
		else if (qName.equals(FILES))
		{
			fileSelection = new ArrayList<File>();
		}
	}

	public int getMissingFilesCount()
	{
		return missingFilesCount;
	}

	private String getErrorLocation(SAXParseException e)
	{
		return e.getLineNumber()+","+e.getColumnNumber();
	}

	@Override public void error(SAXParseException e) throws SAXException
	{
		log.log(Level.SEVERE, "Error parsing file types at "+getErrorLocation(e), e);
	}

	@Override public void warning(SAXParseException e) throws SAXException
	{
		log.log(Level.WARNING, "Error parsing file types at "+getErrorLocation(e), e);
	}

	@Override public void fatalError(SAXParseException e) throws SAXException
	{
		log.log(Level.SEVERE, "Error parsing file types at "+getErrorLocation(e), e);
	}
}
