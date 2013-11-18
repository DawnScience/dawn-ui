/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.xml.string;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringInput implements IStorageEditorInput {

    private IStorage storage;
    
    public StringInput(IStorage storage) {this.storage = storage;}
    
    @Override
    public boolean exists() {return true;}
    @Override
    public ImageDescriptor getImageDescriptor() {return null;}
    @Override
    public String getName() {
       return storage.getName();
    }
    @Override
    public IPersistableElement getPersistable() {return null;}
    @Override
    public IStorage getStorage() {
       return storage;
    }
    @Override
    public String getToolTipText() {
       return "String-based file: " + storage.getName();
    }
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
      return null;
    }

}
