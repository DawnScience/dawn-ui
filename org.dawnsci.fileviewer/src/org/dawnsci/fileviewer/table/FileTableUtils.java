package org.dawnsci.fileviewer.table;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dawnsci.fileviewer.Utils;
import org.dawnsci.fileviewer.Utils.SortDirection;
import org.dawnsci.fileviewer.Utils.SortType;

public class FileTableUtils {
	public static enum FilterType {
		WILDCARD("Wildcard", "Can use * and ? characters.\nE.g., 'i12-*nxs' for all NeXus files starting with 'i12-' and ending with 'nxs'"),
		SCAN_NUMBER("Scan range", "Optional prefix and Python slice.\nE.g., 'i12- 1234::2' for all files starting with 'i12-' and containing even scan numbers from 1234"),
		REG_EXP("Regular expression", "Java regular expression"),
		;

		private String label;
		private String tooltip;

		private FilterType(String label, String tooltip) {
			this.label = label;
			this.tooltip = tooltip;
		}

		public String getTooltip() {
			return tooltip;
		}

		static String[] getLabels() {
			FilterType[] values = values();
			String[] labels = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				labels[i] = values[i].label;
			}
			return labels;
		}
	}

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

	private static FileFilter getFileFilter(String filter, FilterType type) {
		try {
			switch (type) {
			case REG_EXP:
				return new RegexFileFilter(filter);
			case SCAN_NUMBER:
				String[] parts = filter.split("\\s+");
				if (parts.length == 1) {
					return new ScanNumberFileFilter(null, filter);
				}
				return new ScanNumberFileFilter(parts[0], parts[1]);
			case WILDCARD:
				return new WildcardFileFilter(filter);
			default:
				break;
			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
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
	public static FileTableContent[] getDirectoryList(File file, SortType sortType, SortDirection sortDirection, String filter, FilterType filterType) {
		File[] list = null;
		if (filter == null || "*".equals(filter) || Pattern.matches("^\\s*$", filter)) {
			list = file.listFiles();
		} else {
			FileFilter fileFilter = getFileFilter(filter, filterType);
			if (fileFilter != null) {
				list = file.listFiles(fileFilter);
			}
		}
		if (list == null || list.length == 0)
			return new FileTableContent[0];
		FileTableContent[] contentList = createFileTableContentListFromFiles(list);
		Arrays.sort(contentList, (a, b) -> compareFiles(a, b, sortType, sortDirection));
		return contentList;
	}

	public static int getDirectoryListCount(File file, String filter, FilterType filterType) {
		File[] list = null;
		if (filter == null || "*".equals(filter) || Pattern.matches("^\\s*$", filter)) {
			list = file.listFiles();
		} else {
			FileFilter fileFilter = getFileFilter(filter, filterType);
			if (fileFilter != null) {
				list = file.listFiles(fileFilter);
			}
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
