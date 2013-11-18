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

The beans plugin contains only POJO bean files. 

TODO Currently there are some beans in the beans plugin that reference XMLHelper from the common plugin - these are wrong and should
not import XMLHelper. Once this change is made, this plugin will depend on nothing but <code>org.apache.commons.beanutils.BeanUtils</code>
and the JDK.

No further dependencies should be made between this plugin and other projects. All classes should be POJOs - plain old
java classes - and fit the bean pattern (no argument constructors and private/protected fields with getters and setters).

Here are some rules for using the package:

1. Inheritance should not be used and interfaces avoided. Aggregation is ok.

2. No third party dependencies should be introduced.

3. Beans should not reference how they are saved. They may save to XML with castor or XMLEncoder and
   should have no reference to how this is done.
   
4. *All* beans should have hashCode() and equals(...) implemented.

5. The toString() method should be implemented, normally using org.apache.commons.beanutils.BeanUtils.

6. A method signed 'clear()' must be added to beans containing collections and it should clear them. If there are no
   collections, then the method should do nothing.

7. Concrete classes for collections (e.g. ArrayList) should not be visible in getter and setter methods, use List<?> instead.
   This allows swapping of concrete class as requirements dictate.
   
8. It is better to use Objects as fields rather than primitive types as these can be null. So use Double and Integer rather than
   double and int respectively.
   
9. All beans must be serializable.

**/
package org.dawnsci.common.richbeans.beans;