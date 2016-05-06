package org.dawnsci.isosurface.isogui;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.isosurface.tool.RenderingPropertyChangeListener;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.richbeans.api.generator.IListenableProxyFactory;
import org.eclipse.richbeans.api.generator.IListenableProxyFactory.PropertyChangeInterface;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.RowDeleteAction;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiAction;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiHidden;

public class IsoBean{
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private IListenableProxyFactory listenableProxyFactory;
	private List<IIsoItem> items = new ArrayList<>();
	private int ISO_COUNT = 0;

	private RenderingPropertyChangeListener renderingHandler;
	
	/**
	 * Get the list of items
	 * @return ItemList
	 */
	@RowDeleteAction("deleteItem")
	public List<IIsoItem> getItems()	
	{
		return this.items;
	}
	
	/**
	 * Set the list of items
	 * @param newItems - The new list of items
	 */
	public void setItems(List<IIsoItem> newItems)
	{
		List<IIsoItem> oldItems = items;
		oldItems.forEach(item -> ((PropertyChangeInterface)item).removePropertyChangeListener(renderingHandler));
		newItems.forEach(item -> ((PropertyChangeInterface)item).addPropertyChangeListener(renderingHandler));
		this.items = newItems;
		pcs.firePropertyChange("items", oldItems, items);
	}
	
	@UiAction
	public void addIsosurface()
	{
		addProxyItemFor(Type.ISO_SURFACE);
	}
	
	@UiAction
	public void addVolume()
	{
		if (!isThereAlreadyAVolume()){
			addProxyItemFor(Type.VOLUME);
		}
	}

	public void deleteItem(IIsoItem isoItem){
		List<IIsoItem> newItems = new ArrayList<>(getItems());
		newItems.remove(isoItem);
		setItems(newItems);		
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener){
		pcs.removePropertyChangeListener(listener);
	}

	@UiHidden
	public void setListenableProxyFactory(IListenableProxyFactory listenableProxyFactory) {
		this.listenableProxyFactory = listenableProxyFactory;
	}
	
	private Color generateNextColour() {
		return ColorUtility.GRAPH_DEFAULT_COLORS[ISO_COUNT ++ % ColorUtility.GRAPH_DEFAULT_COLORS.length];
	}
	
	private void addProxyItemFor(Type type){
		IIsoItem newItem = new IsoItem(
				type,
				5,
				20,
				50,
				generateNextColour()
			);
		
		IIsoItem listenableItem = listenableProxyFactory.createProxyFor(newItem, IIsoItem.class);
		
		List<IIsoItem> newItems = new ArrayList<>(getItems());
		newItems.add(listenableItem);
		setItems(newItems);
	}

	private boolean isThereAlreadyAVolume() {
		return items.stream().anyMatch(item -> Type.VOLUME == item.getRenderType());
	}

	@UiHidden
	public void setRenderingHandler(RenderingPropertyChangeListener renderingHandler) {
		addPropertyChangeListener(renderingHandler);
		this.renderingHandler = renderingHandler;
	}
}
