package org.dawnsci.fileviewer.table;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dawnsci.fileviewer.Utils;
import org.dawnsci.fileviewer.Utils.SortType;
import org.dawnsci.fileviewer.Utils.SortDirection;

public class FileTableUtils {
	
	private FileTableUtils() {
		
	}

	public static int compareFiles(FileTableContent a, FileTableContent b, SortType sortType, SortDirection sortDirection) {
		// sort case-sensitive files in a case-insensitive manner
		int compare = 0;
		switch (sortType) {
		case NAME:
			compare = a.getFileName().compareToIgnoreCase(b.getFileName());
			if (compare == 0)
				compare = a.getFileName().compareTo(b.getFileName());
			break;
		case SIZE:
			if (a.getFile().isDirectory()) {
				compare = -1;
				break;
			} else if (b.getFile().isDirectory()) {
				compare = 1;
				break;
			}
			long sizea = a.getFile().isDirectory() ? 0 : a.getFile().length();
			long sizeb = b.getFile().isDirectory() ? 0 : b.getFile().length();
			compare = sizea < sizeb ? -1 : 1;
			break;
		case TYPE:
			String typea = a.getFileType();
			String typeb = b.getFileType();
			compare = typea.compareToIgnoreCase(typeb);
			if (compare == 0)
				compare = typea.compareTo(typeb);
			break;
		case DATE:
			Date date1 = new Date(a.getFile().lastModified());
			Date date2 = new Date(b.getFile().lastModified());
			compare = date1.compareTo(date2);
			break;
		default:
			return 0;
		}
		if (Utils.SortDirection.DESC == sortDirection)
			return -1 * compare;
		return compare;
	}

	/**
	 * Gets a directory listing
	 * 
	 * @param file
	 *            the directory to be listed
	 * @param sort
	 *            the sorting type
	 * @return an array of files this directory contains, may be empty but not
	 *         null
	 */
	public static FileTableContent[] getDirectoryList(File file, SortType sortType, SortDirection sortDirection, String filter, boolean useRegex ) {
		File[] list = null;
		if (filter == null || "*".equals(filter) || Pattern.matches("^\\s*$", filter)) {
			list = file.listFiles();
		}
		else if (useRegex) {
			try {
				list = file.listFiles((FileFilter) new RegexFileFilter(filter));
			} catch (PatternSyntaxException e) {
				list = null;
			}
		}
		else {
			list = file.listFiles((FileFilter) new WildcardFileFilter(filter));
		}
		if (list == null || list.length == 0)
			return new FileTableContent[0];
		FileTableContent[] contentList = createFileTableContentListFromFiles(list);
		Arrays.sort(contentList, (a, b) -> compareFiles(a, b, sortType, sortDirection));
		return contentList;
	}

	public static int getDirectoryListCount(File file, String filter, boolean useRegex) {
		File[] list = null;
		if (filter == null || "*".equals(filter) || Pattern.matches("^\\s*$", filter)) {
			list = file.listFiles();
		}
		else if (useRegex) {
			try {
				list = file.listFiles((FileFilter) new RegexFileFilter(filter));
			} catch (PatternSyntaxException e) {
				list = null;
			}
		}
		else {
			list = file.listFiles((FileFilter) new WildcardFileFilter(filter));
		}
		if (list == null)
			return 0; 
		return list.length;
	}

	private static FileTableContent[] createFileTableContentListFromFiles(File[] fileList) {
		if (fileList == null || fileList.length == 0)
			return new FileTableContent[0];
		
		FileTableContent[] rv = new FileTableContent[fileList.length];
		for (int i = 0 ; i < fileList.length ; i++)
			rv[i] = new FileTableContent(fileList[i]);
		
		
		return rv;
	}
	
}
