package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.junit.Test;

public class MappedDataAreaTest {

	@Test
	public void test() {
		MappedDataArea area = new MappedDataArea();
		area.setXandYAxesName(MapNexusFileBuilderUtils.STAGE_X, MapNexusFileBuilderUtils.STAGE_Y);
		
	}

}
