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

public interface IDataPositionEvent {

	/**
	 * Describes the various states a data position event can be in:
	 * <li/> START - position event has been initiated
	 * <li/> DRAG - position is changing
	 * <li/> END - position event is complete
	 */
	public enum Mode {START, DRAG, END}
	
	/**
	 * Get the current mode
	 * @return current mode
	 */
	public abstract Mode getMode();

	/**
	 * Get the position in texture coordinates
	 * @return texture coordinates
	 */
	public abstract double[] getPosition();

}
