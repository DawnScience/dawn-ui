package org.dawnsci.spectrum.ui.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.IDataset;

public class ExampleModel {
	
	private int imageNumber = 0;
	private IROI ROI;
	private int[][] lenpt;
	private IDataset currentImage;
	
	
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
		
//	public void setLenPt(IROI ROI){
//		
//		IRectangularROI bounds = ROI.getBounds();
//		int[] len = bounds.getIntLengths();
//		int[] pt = bounds.getIntPoint();
//		lenpt = new int[2][];
//		lenpt[0]=len;
//		lenpt[1]=pt;
//	
//		firePropertyChange("lenpt", this.lenpt, this.lenpt = lenpt);
//	}
	
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
