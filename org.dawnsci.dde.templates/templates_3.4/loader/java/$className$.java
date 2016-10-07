package $packageName$;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyDataset;

import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.Utils;

public class $className$ extends AbstractFileLoader {

	// Map to hold values
	protected Map<String, List<Double>> vals = new LinkedHashMap<String, List<Double>>();;

	@Override
	protected void clearMetadata() {
		metadata = null;
		if (vals != null)
			vals.clear();
	}

	@Override
	public DataHolder loadFile() throws ScanFileHolderException {
		return loadFile((IMonitor) null);
	}

	/**
	 * Function that loads a custom csv file
	 * 
	 * @return The package which contains the data that has been loaded
	 * @throws ScanFileHolderException
	 */
	@Override
	public DataHolder loadFile(final IMonitor mon) throws ScanFileHolderException {

		// first instantiate the return object.
		final DataHolder result = new DataHolder();

		// then try to read the file given
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

			parseHeaders(in, mon);
			String line = null;
			// Read data
			while ((line = in.readLine()) != null) {
				if (!monitorIncrement(mon)) {
					throw new ScanFileHolderException("Loader cancelled during reading!");
				}
				final String[] values = line.split(getDelimiter());
				final Iterator<String> it = vals.keySet().iterator();
				for (String value : values) {
					// add read value to Map
					vals.get(it.next()).add(Utils.parseDouble(value.trim()));
				}
			}
			// Add Map to dataholder
			for (final String n : vals.keySet()) {
				ILazyDataset data;
				data = DatasetFactory.createFromList(vals.get(n));
				data.setName(n);
				result.addDataset(n, data);
			}
			return result;

		} catch (Exception e) {
			throw new ScanFileHolderException("DawnTestLoader.loadFile exception loading " + fileName, e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				throw new ScanFileHolderException("Cannot close stream from file  " + fileName, e);
			}
		}
	}

	/**
	 * May override to support different file formats.
	 * 
	 * @return the delimiter
	 */
	private String getDelimiter() {
		return "\\s*";
	}

	@Override
	public void loadMetadata(final IMonitor mon) throws IOException {
		// TODO
	}

	/**
	 * Reads the first line
	 * 
	 * @param in
	 * @param mon
	 * @return last line
	 * @throws Exception
	 */
	private void parseHeaders(final BufferedReader in, IMonitor mon) throws Exception {
		String line = in.readLine();
		if (line == null)
			new ScanFileHolderException("Read line is null!");
		if (!line.trim().startsWith("&dawnscience"))
			throw new ScanFileHolderException("Not a custom diamond csv file");
		if (!monitorIncrement(mon))
			throw new ScanFileHolderException("Loader cancelled during reading!");
		vals.clear();

		// read next line (columns headers)
		line = in.readLine();
		String[] headers = line.split(getDelimiter());
		// populate Map with headers found
		for (int i = 0; i < headers.length; i++) {
			vals.put(headers[i], new ArrayList<Double>());
		}
	}
}