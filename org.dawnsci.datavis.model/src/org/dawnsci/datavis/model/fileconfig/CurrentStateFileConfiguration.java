package org.dawnsci.datavis.model.fileconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlottableObject;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.Slice;

public class CurrentStateFileConfiguration implements ILoadedFileConfiguration {

	private List<DataOptions> state;

	@Override
	public boolean configure(LoadedFile f) {
		if (state == null || state.isEmpty()) return false;

		Map<String, int[]> dataShapes = f.getDataShapes();

		boolean found = false;

		for (DataOptions o : state) {
			if (o.isSelected() && o.getPlottableObject() != null && dataShapes.containsKey(o.getName())) {
				PlottableObject plotObject = o.getPlottableObject();
				DataOptions dataOption = f.getDataOption(o.getName());
				NDimensions nDimensions = plotObject.getNDimensions();
				if (nDimensions == null || dataOption == null) continue;
				if (dataShapes.get(o.getName()).length == nDimensions.getRank()) {
					IPlotMode plotMode = plotObject.getPlotMode();
					NDimensions newND = dataOption.buildNDimensions();
					newND.setOptions(plotMode.getOptions());
					for (int i = 0; i < nDimensions.getRank(); i++) {
						newND.setDescription(i, nDimensions.getDescription(i));
						if (newND.getSize(i) == nDimensions.getSize(i)) {
							newND.setSlice(i, nDimensions.getSlice(i));
						} else {
							Slice s = new Slice(0,1,1);
							if (!(newND.getDescription(i) == null || newND.getDescription(i).isEmpty())) {
								s.setStop(newND.getSize(i));
							}
							newND.setSlice(i, s);
						}

						String[] axisOptions = newND.getAxisOptions(i);
						String axis = nDimensions.getAxis(i);
						Optional<String> first = Arrays.stream(axisOptions).filter(s-> axis.equals(s)).findFirst();
						if (first.isPresent()) newND.setAxis(i, first.get());
						dataOption.setSelected(true);
						PlottableObject p = new PlottableObject(plotMode, newND);
						dataOption.setPlottableObject(p);
						found = true;
					}
				}

			}
		}
		return found;
	}

	@Override
	public void setCurrentState(List<DataOptions> state) {
		this.state = state;
	}

}
