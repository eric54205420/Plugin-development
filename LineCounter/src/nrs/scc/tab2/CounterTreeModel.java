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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdom.Element;

/**
 * File types and counters to apply in the form of a tree model
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class CounterTreeModel implements TreeModel
{
	private String NEW_FILE_TYPE = "New File Type";
	private String NEW_COUNT_ITEM = "New Count Item";
	private String FILE_TYPES = "File Types";
	private final static String NEW_PATTERN = ".";

	// look up I18N labels
	{
		ResourceMap rMap = Application.getInstance(nrs.scc.SourceCodeCounter.class).getContext().getResourceMap(CounterTreeModel.class);
		NEW_FILE_TYPE = rMap.getString("new.file.type");
		NEW_COUNT_ITEM = rMap.getString("new.count.item");
		FILE_TYPES = rMap.getString("file.types");
	}

	private List<FileType> fileTypesList = new ArrayList<FileType>();
	private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode(FILE_TYPES);

	public CounterTreeModel()
	{
	}

	/**
	 * Set the file types
	 * @param fileTypes
	 */
	public void setFileTypes(Map<String, FileType> fileTypes)
	{
		fileTypesList.clear();
		fileTypesList.addAll(fileTypes.values());
		Collections.sort(fileTypesList);
		TreeModelEvent e = new TreeModelEvent(this, new TreePath(root));
		for (TreeModelListener listener : listeners)
		{
			listener.treeStructureChanged(e);
		}
	}

	/**
	 * Notify listeners of an insert or delete
	 * @param insert
	 * @param childIndices
	 * @param childObjects
	 * @param path
	 */
	private void notifyListeners(boolean insert, int[] childIndices, Object[] childObjects, TreeNode... path)
	{
		TreeModelEvent e = new TreeModelEvent(this, new TreePath(path), childIndices, childObjects);
		for (TreeModelListener listener : listeners)
		{
			if (insert) {
				listener.treeNodesInserted(e);
			} else {
				listener.treeNodesRemoved(e);
			}
		}
	}

	/**
	 * Add a new FileType
	 * @return the FileType that was created
	 */
	public FileType addFileType()
	{
		FileType rft = null;

		FileType ft = new FileType(root, NEW_FILE_TYPE);
		if (!fileTypesList.contains(ft))
		{
			CountItem ci = new CountItem(ft, NEW_COUNT_ITEM, Pattern.compile(NEW_PATTERN));
			ft.addCountItem(ci);
			fileTypesList.add(ft);
			Collections.sort(fileTypesList);

			int[] childIndex = { fileTypesList.indexOf(ft) };
			Object[] childObject = { ft };
			notifyListeners(true, childIndex, childObject, root);

			rft = ft;
		}
		
		return rft;
	}
	
	/**
	 * Get the last manually added FileType
	 * @return FileType or null if not found
	 */
	public FileType getNewFileType()
	{
		FileType rft = null;

		FileType ft = new FileType(root, NEW_FILE_TYPE);
		int index = fileTypesList.indexOf(ft);
		if (index > -1) {
			rft = fileTypesList.get(index);
		}

		return rft;
	}

	/**
	 * Add a new counter
	 * @param ft parent FileType
	 * @return the created CountItem
	 */
	public CountItem addCountItem(FileType ft)
	{
		CountItem rci = null;

		CountItem ci = new CountItem(ft, NEW_COUNT_ITEM, Pattern.compile(NEW_PATTERN));
		if (ft.addCountItem(ci))
		{
			int[] childIndex = { ft.getIndex(ci) };
			Object[] childObject = { ci };
			notifyListeners(true, childIndex, childObject, root, ft);

			rci = ci;
		}
		
		return rci;
	}

	/**
	 * Remove the specified FileType
	 * @param ft
	 */
	public void removeFileType(FileType ft)
	{
		int[] childIndex = { fileTypesList.indexOf(ft) };
		Object[] childObject = { ft };
		
		fileTypesList.remove(ft);
		
		notifyListeners(false, childIndex, childObject, root);
	}

	/**
	 * Remove the specified CountItem
	 * @param ci
	 */
	public void removeCountItem(CountItem ci)
	{
		FileType ft = (FileType)ci.getParent();
		int[] childIndex = { ft.getIndex(ci) };
		Object[] childObject = { ci };
		ft.removeCountItem(ci);
		
		notifyListeners(false, childIndex, childObject, root, ft);
	}

	/**
	 * Add a listener
	 * @param l
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		listeners.add(l);
	}

	/**
	 * Get the child object of the specified parent
	 * @param parent
	 * @param index
	 * @return
	 */
	@Override
	public Object getChild(Object parent, int index)
	{
		Object child = null;
		if (parent.equals(root))
		{
			child = fileTypesList.get(index);
		}
		else if (parent instanceof FileType)
		{
			FileType ft = (FileType)parent;
			child = ft.getChildAt(index);
		}
		
		return child;
	}

	/**
	 * Get the number of children for the specified parent
	 * @param parent
	 * @return
	 */
	@Override
	public int getChildCount(Object parent)
	{
		int count = 0;

		if (parent.equals(root))
		{
			count = fileTypesList.size();
		}
		else if (parent instanceof FileType)
		{
			FileType ft = (FileType)parent;
			count = ft.getChildCount();
		}

		return count;
	}

	/**
	 * Get the index of the child
	 * @param parent
	 * @param child
	 * @return
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		int index = 0;

		if (parent.equals(root))
		{
			index = fileTypesList.indexOf(child);
		}
		else if (parent instanceof FileType)
		{
			FileType ft = (FileType)parent;
			index = ft.getIndex((TreeNode)child);
		}
		
		return index;
	}

	@Override
	public TreeNode getRoot()
	{
		return root;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		return (node instanceof CountItem);
	}

	/**
	 * Remove a listener
	 * @param l
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		listeners.remove(l);
	}

	/**
	 *
	 * @param path
	 * @param newValue
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		TreeModelEvent e;
		for (TreeModelListener listener : listeners)
		{
			e = new TreeModelEvent(this, path);
			listener.treeNodesChanged(e);
		}
	}

	/**
	 * Get a List of nodes that are selected
	 * @param fileTypeExts
	 * @return
	 */
	public List<TreePath> getSelectedTreePaths(List<FileType> fileTypeExts)
	{
		List<TreePath> paths = new ArrayList<TreePath>();
		TreePath tpath;
		Object[] path = { root, "" };
		for (FileType ft : fileTypeExts)
		{
			path[1] = ft;
			tpath = new TreePath(path);
			paths.add(tpath);
		}
		return paths;
	}

	/**
	 * Get an XML representation of the FileTypes
	 * @return a JDom Element
	 */
	public Element getXML()
	{
		Element rootEl = new Element(FileTypesSAXParser.FILE_TYPES);
		Element ftEl, fpEl, extEl, cisEl;

		for (FileType ft : fileTypesList)
		{
			ftEl = new Element(FileTypesSAXParser.FILE_TYPE);
			ftEl.setAttribute(FileTypesSAXParser.ATTR_TYPE, ft.getName());
			rootEl.addContent(ftEl);

			fpEl = new Element(FileTypesSAXParser.FILE_PATTERNS);
			ftEl.addContent(fpEl);
			for (String pattern : ft.getFilePatterns())
			{
				extEl = new Element(FileTypesSAXParser.PATTERN);
				fpEl.addContent(extEl);
				extEl.setText(pattern);
			}

			cisEl = new Element(FileTypesSAXParser.COUNT_ITEMS);
			ftEl.addContent(cisEl);
			for (CountItem ci : ft.getCountItems())
			{
				cisEl.addContent(ci.getXML());
			}
		}
		return rootEl;
	}
}
