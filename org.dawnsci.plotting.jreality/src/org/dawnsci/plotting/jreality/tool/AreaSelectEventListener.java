/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.EventListener;

/**
 * AreaSelectEventListener allows to listen to any successful rectangle area selection that have been
 * done inside the 3D framework, the AreaSelectEvent contains the rectangle coordinates in DataSet space
 */

public interface AreaSelectEventListener extends EventListener {

	/**
	 * An area has been selected
	 * @param event AreaSelectEvent object
	 */
	public void areaSelected(AreaSelectEvent event);
	
}
