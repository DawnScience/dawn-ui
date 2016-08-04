package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.widgets.Slider;

public class ReflectivityUIModel {
	
	private int imageNumber = 0;
	private IROI ROI;
	private int[][] lenpt;
	private IDataset currentImage;
	private ArrayList<IROI> ROIList = new ArrayList<IROI>();
	private IROI ROIListElement;
	private int ROIevent=0;
	
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


	public IROI getROI(){
		return ROI;
	}

	public void setROI(IROI ROI) {
		//this.setLenPt(ROI);
		firePropertyChange("ROI", this.ROI, this.ROI= ROI);
		IRectangularROI bounds = ROI.getBounds();
		int[] len = bounds.getIntLengths();
		int[] pt = bounds.getIntPoint();
		int[][] lenpt = new int[2][];
		lenpt[0]=len;
		lenpt[1]=pt;
		firePropertyChange("ROI", this.ROI, this.ROI= ROI);
		firePropertyChange("lenpt", this.lenpt, this.lenpt= lenpt);
	}
	
	public void setROIList(ArrayList<IROI> ROIList) {
		firePropertyChange("ROIList", this.ROIList, this.ROIList= ROIList);	
	}
	
	public void setROIListElement(IROI ROI, int k) {
		
		if (ROIList.size() < k){
			ROIList.add(ROI);
			firePropertyChange("ROIevent", this.ROIevent, this.ROIevent= (ROIevent+1));
			
			
			//ROIList.set(k, ROI);
		}
		else {
			//ROIList.add(ROI);
			//firePropertyChange("ROIevent", this.ROIevent, this.ROIevent= (ROIevent+1));
			ROIList.set(k, ROI);
		}
		
		firePropertyChange("ROIevent", this.ROIevent, this.ROIevent= (ROIevent+1));
	}

	
	public int[][] getLenPt(){
		return lenpt;
	}

	public IDataset getCurrentImage(){
		return currentImage;
	}
	
	public void setCurrentImage(IDataset currentImage){
		firePropertyChange("currentImage", this.currentImage, this.currentImage= currentImage);
		}
	
	//	
//	public static void setData(AggregateDataset data1){
//		data=data1;
//	}
//	
//	public static ILazyDataset getData(){
//		return data;
//		
//	}
//	
//	public static void setImage (IDataset image1){
//		image=image1;
//	}
//	
//	public static IDataset getImage(){
//		return image;
//	}
//	

	public int getImageNumber() {
		return imageNumber;
	}

	public void setImageNumber(int imageNumber) {
		firePropertyChange("imageNumber", this.imageNumber, this.imageNumber= imageNumber);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}
	
}
