package org.dawnsci.common.widgets.gda.function;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ModelTest.class, FunctionTreeViewerJexlPluginTest.class,
		FunctionTreeViewerPluginTest.class,
		FunctionTreeViewerHandlersIsHandledPluginTest.class,
		FunctionTreeViewerHandlersExecutePluginTest.class,
		FunctionExtensionFactoryPluginTest.class })
public class AllTests {

}
