package org.dawnsci.isosurface.isogui;

import java.util.Arrays;

import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.binding.BeanService;

public class IsoHandler extends ValueAdapter {

	final private IsosurfaceJob job;

	private IBeanController controller;
	private IsoComposite isoComp; // not sure if this is ok
	private ILazyDataset lazyDataset;

	private IPlottingSystem<?> system;

	private IsoBean bean;

	public IsoHandler(IsoComposite ui, IsoBean bean, IsosurfaceJob newJob, ILazyDataset lazyDataset,
			IPlottingSystem<?> system) {
		super("IsoValueListner");

		this.bean = bean;
		this.isoComp = ui;
		this.job = newJob;
		this.lazyDataset = lazyDataset;
		this.system = system;

		try {
			controller = BeanService.getInstance().createController(ui, bean);
			controller.addValueListener(this);
			controller.beanToUI();
			controller.switchState(true);
		} catch (Exception e1) {

			System.err.println("\nController not set - Default value is NULL");
			e1.printStackTrace();
		}
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {
		// update view
		try {
			controller.uiToBean();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		job.destroyOthers(bean.getItems().stream().map(item -> item.getTraceKey()));

		if (isoComp.getItems().getSelectedIndex() < isoComp.getItems().getListSize()) {
			IsoItem current = (IsoItem) isoComp.getItems().getBean();

			boolean justRerender = Arrays.asList("colour", "opacity", "name").contains(e.getFieldName());
			
			// run alg
			if (justRerender) {
				IIsosurfaceTrace trace = (IIsosurfaceTrace) system.getTrace(current.getTraceKey());
				trace.setMaterial(
						current.getColour().red, current.getColour().green, current.getColour().blue, 
						current.getOpacity()
					);
				trace.setData(null, null, null, null);

			} else {
				job.compute(
					new MarchingCubesModel(
						lazyDataset, 
						current.getValue(),
						new int[] { current.getX(), current.getY(), current.getZ() },
						new int[] { current.getColour().red, current.getColour().green, current.getColour().blue },
						current.getOpacity(), 
						current.getTraceKey(), 
						current.getName()
					)
				);
			}
		}

	}
}
