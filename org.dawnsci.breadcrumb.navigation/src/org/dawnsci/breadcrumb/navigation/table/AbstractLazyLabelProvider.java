/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.table;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A label provider that can get fast and slow text in order to allow
 * data to be retrieved slowly if required.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractLazyLabelProvider extends CellLabelProvider {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractLazyLabelProvider.class);
	
	private BlockingQueue<ViewCellBean> viewerCellTextQueue;
    private boolean 		    disposed = false;
	private IIndexProvider      indexProvider;
    private int                 columnOffset = 0; // Bit of a bodge
	private boolean             allowLazyText;

	public AbstractLazyLabelProvider(IIndexProvider indexProvider) {
		this(indexProvider, true);
	}

	public AbstractLazyLabelProvider(IIndexProvider indexProvider, boolean allowLazyText) {
		
		this.indexProvider = indexProvider;
		this.allowLazyText = allowLazyText;
		
		this.viewerCellTextQueue = allowLazyText
				                 ? new LinkedBlockingQueue<AbstractLazyLabelProvider.ViewCellBean>()
				                 : null; // Don't need it
		
		if (allowLazyText) {
			final Thread processor = new Thread() {
				public void run() {
					while(!disposed) {
						try {
							final ViewCellBean vcb = viewerCellTextQueue.take();
							final String      text = getSlowText(vcb.getElement(), vcb.getColumnIndex()-columnOffset);
							if (text==null) continue;
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									if (vcb.getCell().getItem().isDisposed()) return;
									vcb.getCell().setText(text);
								}
							});
						} catch (Throwable ne) {
							ne.printStackTrace();
						}
					}
				}
			};
			processor.setDaemon(true);
			processor.start();
		}
		
	}
		
	private Color lightBlue;
	private Color lightGrey;

	public Color getBackground(Object element) {
				
		boolean even = indexProvider.getIndex(element)%2 == 0;
		if( even ) {
			return null;
		} else {
			if (lightBlue==null) lightBlue = new Color(Display.getDefault(), 250, 250, 254);
			return lightBlue;
		}
	}
		
	/**
	 * Clears the queue
	 */
	public void clear() {
		viewerCellTextQueue.clear();
	}
	
	@Override
	public void dispose() {
		try {
			disposed = true;
			if (lightBlue!=null) lightBlue.dispose();
			lightBlue = null;
			
			if (lightGrey!=null) lightGrey.dispose();
			lightGrey = null;
			
		} catch (Throwable ne) {
			logger.debug("Error disposing of provider!", ne);
		}
		
		super.dispose();
		
	}


	public void update(ViewerCell cell) {
		
		if (disposed) return;
		if (cell.getItem().isDisposed()) return;
		
		Object element = cell.getElement();
		
		try {
			final int    index    = cell.getColumnIndex();
			final String fastText = getFastText(element, index-columnOffset);
			if (fastText!=null) {
				cell.setText(fastText);
			} else if (allowLazyText){
				viewerCellTextQueue.add(new ViewCellBean(cell, element, index));
			} else {
				cell.setText(getSlowText(element, index-columnOffset));
			}
			
			Image image = getImage(element, index-columnOffset);
			cell.setImage(image);
			cell.setBackground(getBackground(element));
			//cell.setForeground(getForeground(element));
			//cell.setFont(getFont(element));

		} catch (Exception ne) {
			logger.error("Error in table ", ne);
		}

	}
	
	protected abstract Image getImage(Object element, int index);

	public int getToolTipDisplayDelayTime(Object object) {
		return 200;
	}
	public int getToolTipTimeDisplayed(Object object) {
		return 5000;
	}
	public Color getToolTipBackgroundColor(Object object) {
		return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	}
	

	/**
	 * Text to be used to compare rows.
	 * @param o1
	 * @param index
	 * @return
	 */
	public String getCompareText(Object o1, int index) {
		String text = getFastText(o1, index-columnOffset);
		if (text!=null) return text;
		return getSlowText(o1, index-columnOffset);
	}

	protected abstract String getFastText(Object element, int columnIndex);
	
	protected abstract String getSlowText(Object element, int columnIndex);
	
	private class ViewCellBean {
		private ViewerCell cell;
		private Object     element;
		private int        columnIndex;
		public ViewCellBean(ViewerCell cell, Object element, int columnIndex) {
			super();
			this.cell = cell;
			this.element = element;
			this.columnIndex = columnIndex;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((cell == null) ? 0 : cell.hashCode());
			result = prime * result + columnIndex;
			result = prime * result
					+ ((element == null) ? 0 : element.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ViewCellBean other = (ViewCellBean) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (cell == null) {
				if (other.cell != null)
					return false;
			} else if (!cell.equals(other.cell))
				return false;
			if (columnIndex != other.columnIndex)
				return false;
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element))
				return false;
			return true;
		}
		public ViewerCell getCell() {
			return cell;
		}
		public void setCell(ViewerCell cell) {
			this.cell = cell;
		}
		public Object getElement() {
			return element;
		}
		public void setElement(Object element) {
			this.element = element;
		}
		public int getColumnIndex() {
			return columnIndex;
		}
		public void setColumnIndex(int columnIndex) {
			this.columnIndex = columnIndex;
		}
		private AbstractLazyLabelProvider getOuterType() {
			return AbstractLazyLabelProvider.this;
		}
	}

	private NumberFormat numberFormat;
	protected String getDoubleFormatted(String strDouble) {
		if (strDouble==null) return "";
		try {
			double dbl = Double.parseDouble(strDouble);
			if (numberFormat == null) numberFormat = DecimalFormat.getNumberInstance();
			return numberFormat.format(dbl);
		} catch (Exception ne) {
			return strDouble;
		}
	}
	
	private NumberFormat tdpFormat;
	protected String getDoubleFormatted2dp(String strDouble) {
		if (strDouble==null) return "";
		try {
			double dbl = Double.parseDouble(strDouble);
			return getDoubleFormatted2dp(dbl);
		} catch (Exception ne) {
			return strDouble;
		}
	}
	protected String getDoubleFormatted2dp(double dbl) {
		if (tdpFormat == null) tdpFormat = new DecimalFormat("##########0.00");
		return tdpFormat.format(dbl);
	}

	public int getColumnOffset() {
		return columnOffset;
	}

	public void setColumnOffset(int columnOffset) {
		this.columnOffset = columnOffset;
	}

}
