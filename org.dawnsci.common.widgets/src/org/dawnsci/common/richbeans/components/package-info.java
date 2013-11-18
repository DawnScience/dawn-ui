/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 
 *                           Widgets
 *                           =======
 *                 
 *                 
 *  These widgets are based on SWT and implement IFieldWidget so that they 
 *  can be used in a RichBeanEditor in the rich bean framework.
 *  
 *  Widgets can also be used in RCP developer by using the palate in this
 *  plugin or manually creating your own palate.
 *  
 *  Most useful widgets:
 *  1. ScaleBox  - a widget for editing a double value with bounds and units.
 *  2. ComboWrapper - able to show choices to users. Includes a setItems(Map<String,Object>)
 *     which gives the user a visible choice and an XML choice via map.
 *  3. RangeBox - Able to edit ranges of double values.
 *  4. ScaleBoxAndExpression - edits a value like a scale box and then computes and displays another alternate value.
 *  
 */
package org.dawnsci.common.richbeans.components;
