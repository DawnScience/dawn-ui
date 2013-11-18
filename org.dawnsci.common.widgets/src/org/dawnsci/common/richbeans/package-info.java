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
 * API Forked in DAWN because:
 * ===========================
 * 1. Want to deprecate large parts of DAWNs usage of it. Cannot put @ deprecated into GDA parts which are used.
 *     -> This deprecation allows DAWN developers to know that alternatives exist to the old way of doing it.
 *     
 * 2. It hard codes usage of JEP which we would like to replace with the expression service which does not use JEP.
 * 
 * 3. Licensing issues - if GPL, means the Science WG will not work out http://wiki.eclipse.org/Science_IWG. Plugins
 * for the group must be a permissive license like Apache or EPL.
 * 
 * 4. There have been changes to the widgets inside rich beans which mean that they cannot be reused with no GDA or 
 * third party unwanted dependencies (like JEP and castor).
 */

/**
                                                RICH BEAN FRAMEWORK - v1.0.5

Introduction
------------
The rich bean framework is a design for separating widgets from functionality. It uses reflection to synchronise
beans with editors which then can be saved to XML (or other format) and run on the server by transmitting the beans
via serialisation, file or web service (et. al.) RCP developer can be used to create the composites used to edit 
beans. Widgets for a wide variety of bean fields exist in the components package. Widgets can be added and must implement
IFieldWidget.


Procedure for Creating a new Rich Bean Editor
---------------------------------------------

Create a Bean
=============
1. Create a bean to hold your data values.
   - Store only experimental data values in a known unit set.
   - Not units
   - Not formatting, limits/bounds, label values etc.
   - Not UI data
2. Follow the rules documented in the file package-info.ajav in the beans sub-package on creating beans.
3. Create a separate unit test which reads and writes the bean to file 
   and tests equality and validation (for instance see how @ExafsValidator
   is called from unit tests.
4. Ensure that the bean and all its children (recursively) implement Serializable
   so that they can be sent to the server.


Using the DLS Rich Bean Tool
============================
1. Right click on your bean and go to 'New -> Other... -> DLS Wizards -> New Rich Bean Editor from Wizard'
   If this is not available then the wizard is not installed - put the latest version in your eclipse
   from /dls_sw/dasc/tools_versions/developer_tools. TODO Create update site for tool.
   
2. Follow the instructions, <b>NOTE</b> it is a good idea to add the editor to a different plugin to the bean because
   the bean will be atomic and used in places where the eclipse API is not present. There
   are choices to do this when using the wizard.

3. Extensions will be created in your plugin automatically. Including a descriptor which will determine if a particular XML
   file should be edited with your editor - <b>NOTE</b> by default this does not use castor but does assume a format where the
   bean type appears on the second line of the file in an xml tag - see source code to understand this.
   
4. The tool creates a composite which can be edited using RCP Developer. By default all fields in the bean are exposed
   in the composite and Collections are not exposed but they are supported (see 5.).
  
5. To support fields which are lists of beans you, need an @ListEditor, a common concrete class of which is @VerticalListEditor.
   Search for uses of @VerticalListEditor and read its javadocs for clues on how to use it.
   
6. To support fields which are beans simply create a class extending @FieldBeanComposite and create getters on it for its fields.
   For example @CryostatComposite.  
   
7. Create a unit test for your editor using a plugin test. Examples of which are in <code>uk.ac.gda.exafs.ui</code> for instance @XasParametersUIEditorPluginTest
   <b>NOTE</b> In order for Plugin Test's to work in the GDA framework they should end in the string 'PluginTest'.


Using Project Builders
======================
At this point you have a bean with a test and an editor with a test. The bean test is checked with a validator and this validator
can be reused with a project builder in eclipse to check the bean files whenever they are saved. Thus avoiding invalid beans being
saved and submitted to run. The class @ExafsXMLValidationBuilder is an example of this. Follow the standard eclipse examples for adding
an @IncrementalProjectBuilder to a project to activate this feature.


Implement Server Side Code
==========================
Server side code which reads the bean and runs the experiment should be implemented. This is beamline specific. The server side code
should also validate any beans transmitted to it.


Undo/Redo
=========
Rich bean editors support undo/redo automatically.


Design of Experiments
=====================
Any field in your bean can be used in design of experiments studies. This means varying the field and doing an experiment with a range of
values for it. The system supports multiple fields and fields inside lists of beans (using a recursive algorithm). 

This is done as follows:
1. Change the field in the bean to be a string, note that if using castor the mapping files should be changed.
2. Apply the annotation @DOEField to the field in the bean - you must provide a weighting at this point e.g. @DOEField(DOEField.DEFAULT_LEVEL)
3. Change the IFieldWidget used in the composite being used to edit the bean to be an IRangeWidget for instance swap ScaleBox for RangeBox
4. On the server use the method <code>DOEUtils.expand(bean)</code> to generate all the single experiment beans and feed these to your server side code
   which uses the bean (simply expand out the beans and loop over them).

There is a view which displays the experiments in the order that they will be run. See also the package-info.java for the <code>uk.ac.gda.doe</code> package.

Expressions
===========
Expressions can be entered into most NumberBox widgets when they are used in the Rich Beans framework. The expression is a function of other fields.
Mathematical functions are allows as are numerical expressions. 

Currently the expression is not saved but the computed value is. However in future an expression resolution method (similar to or part of DOEUtils) should
be created to evaluate expressions as the final data beans are created for use on the server.


Storage of Beans in Nexus files
===============================
The nexus file format is not part of the Richbean framework but there are utilities to help with opening editors when their xml is stored in nexus.
The xml can be extract from the nexus file as a string and opened in a richbean editor using XMLEditorManager.openXmlFromStrings(...)
An example of this is done in OpenXmlAction in the scisoft rcp plugin. This opens any richbean editor whose xml is stored in the 'xml' NXdata node.



Extensions
----------

Multiple Run
============
The exafs beamlines have a perspective for queueing multiple sets of runs including repositioning, time estimation and a queue view.
This is almost generic and could be added to the rich bean framework.


**/
package org.dawnsci.common.richbeans;
