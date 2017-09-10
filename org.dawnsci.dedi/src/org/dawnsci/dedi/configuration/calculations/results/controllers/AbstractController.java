package org.dawnsci.dedi.configuration.calculations.results.controllers;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.dedi.configuration.calculations.results.models.IModel;


public abstract class AbstractController<T extends IModel> implements PropertyChangeListener {
	    protected List<PropertyChangeListener> registeredViews;
	    protected List<T> registeredModels;

	    public AbstractController() {
	        registeredViews = new ArrayList<>();
	        registeredModels = new ArrayList<>();
	    }

	    
	    public void addView(PropertyChangeListener view) {
	        registeredViews.add(view);
	    }

	    public void removeView(PropertyChangeListener view) {
	        registeredViews.remove(view);
	    }


	    public void addModel(T model){
			registeredModels.add(model);
			model.addPropertyChangeListener(this);
		}
		
		
		public void removeModel(T model){
			registeredModels.remove(model);
			model.removePropertyChangeListener(this);
		}
		
	    
	    //  This method is used to observe property changes from registered models
	    //  and propagate them on to all the views
		@Override
	    public void propertyChange(PropertyChangeEvent evt) {
	        for (PropertyChangeListener view: registeredViews) {
	            view.propertyChange(evt);
	        }
	    }
	    
        
		
	    /* 
	     * Convenience methods that can be used to make controllers as independent of their models as possible.
	     * However, concrete controller classes are free to define their own ways of manipulating the models as well.
	     */
		
		
		protected Object getModelProperty(String propertyName){
	   	 	for (T model: registeredModels) {
		            try {
		            	Method method = model.getClass().getDeclaredMethod("get" + propertyName);
		                return method.invoke(model);
		            } catch (Exception ex) {
		                //  Do nothing.
		            }
		     }
	   	 	 return null;
	    }
		

	    @SuppressWarnings("rawtypes")
		protected void setModelProperty(String propertyName, Object newValue, Class clazz) {
	        for (T model: registeredModels) {
	            try {
	            	Method method = model.getClass().
	                    getMethod("set"+propertyName, new Class[] {clazz});
	                method.invoke(model, newValue);
	            } catch (Exception ex) {
	                //  Do nothing.
	            }
	        }
	    }
}

