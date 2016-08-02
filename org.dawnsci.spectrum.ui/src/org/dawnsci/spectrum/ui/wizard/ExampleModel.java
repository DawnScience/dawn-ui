package org.dawnsci.spectrum.ui.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.widgets.Slider;

public class ExampleModel {
	
	private int imageNumber = 0;
//	private IROI region
//	
//	
//	private static Slider slider;
//	private static IRegion region;
//	private static AggregateDataset data;
//	private static IDataset image;
	
	
	
//	public static void setSlider(Slider slider1){
//		slider = slider1;
//		firePropertyChange("slider", this.slider, this.slider= slider);
//	}
//	
//	public int getSliceNo(Slider slider){
//		
//		int sliceNo = slider.getSelection();
//		
//		return sliceNo;
//	}
//
//	
//	private static void setRegion(IRegion region1){
//		region = region1;
//	}
//	
//	
//	public int[][] getROIboxlenpt(IRegion region){
//		
//		IROI box = region.getROI();
//		IRectangularROI bounds = box.getBounds();
//		int[] len = bounds.getIntLengths();
//		int[] pt = bounds.getIntPoint();
//		
//		int[][] lenpt = new int[2][];
//		
//		lenpt[0]= len;
//		lenpt[1]= pt;
//				
//		return lenpt;
//	}
//	
//	
//	public IRegion getRegion(){
//	
//		return region;
//	}
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
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public int getImageNumber() {
		return imageNumber;
	}

	public void setImageNumber(int imageNumber) {
		firePropertyChange("imageNumber", this.imageNumber, this.imageNumber= imageNumber);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
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
