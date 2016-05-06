package org.dawnsci.isosurface.isogui;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.richbeans.api.generator.IListenableProxyFactory;
import org.eclipse.richbeans.api.generator.IListenableProxyFactory.PropertyChangeInterface;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;

public class IsoBeanTest {

	@Test
	public void testCanAddAnIsoSurface(){
		IsoBean isoBean = createIsoBean();

		isoBean.addIsosurface();
		
		checkType(isoBean, Type.ISO_SURFACE);
	}


	@Test
	public void testCanAddAVolume(){
		IsoBean isoBean = createIsoBean();

		isoBean.addVolume();
		
		checkType(isoBean, Type.VOLUME);
	}

	@Test
	public void testCanNotAddMoreThanOneVolume(){
		IsoBean isoBean = createIsoBean();

		isoBean.addVolume();
		isoBean.addVolume();
		isoBean.addVolume();
		
		List<IIsoItem> items = isoBean.getItems();
		assertThat(items.size(), is(1));
	}

	@Test
	public void testCanAddMoreThanOneSurface(){
		IsoBean isoBean = createIsoBean();

		isoBean.addIsosurface();
		isoBean.addIsosurface();
		isoBean.addIsosurface();
		
		List<IIsoItem> items = isoBean.getItems();
		assertThat(items.size(), is(3));
	}
	
	private IsoBean createIsoBean(){
		IsoBean isoBean = new IsoBean();
		isoBean.setListenableProxyFactory(new MockProxyFactory());
		return isoBean;
	}

	private void checkType(IsoBean isoBean, Type type) {
		List<IIsoItem> items = isoBean.getItems();
		assertThat(items.size(), is(1));
		
		IIsoItem item = isoBean.getItems().get(0);
		assertThat(item.getRenderType(), is(type));
		assertThat(item.getTraceKey(), is(notNullValue()));
	}
	
	private class MockProxyFactory implements IListenableProxyFactory{
		@SuppressWarnings("unchecked")
		@Override
		public <S extends T, T> T createProxyFor(S original, Class<T> interfaceImplemented) {
			return (T) new WrappedIsoItem((IsoItem)original);				
		}
	}
	
	private class WrappedIsoItem implements IIsoItem, PropertyChangeInterface{
		private IsoItem original;

		public WrappedIsoItem(IsoItem original) {
			this.original = original;
		}

				@Override
		public Type getRenderType() {
			return original.getRenderType();
		}
		@Override
		public String getTraceKey() {
			return original.getTraceKey();
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {}
		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {}
		
		@Override
		public double getValue() {
			throw new UnsupportedOperationException();
		}
		@Override
		public void setValue(double value) {
			throw new UnsupportedOperationException();
		}
		@Override
		public int getResolution() {
			throw new UnsupportedOperationException();
		}
		@Override
		public void setResolution(int resolution) {
			throw new UnsupportedOperationException();
		}
		@Override
		public int getOpacity() {
			throw new UnsupportedOperationException();
		}
		@Override
		public void setOpacity(int opacity) {
			throw new UnsupportedOperationException();
		}
		@Override
		public RGB getColour() {
			throw new UnsupportedOperationException();
		}
		@Override
		public void setColour(RGB rgb) {
			throw new UnsupportedOperationException();
		}
		
	}
}
