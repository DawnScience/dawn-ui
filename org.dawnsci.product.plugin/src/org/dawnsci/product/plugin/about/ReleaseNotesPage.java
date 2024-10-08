package org.dawnsci.product.plugin.about;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.about.InstallationPage;

/**
 * Release notes page accessible through the About dialog Release Notes tab
 * @author wqk87977
 *
 */
public class ReleaseNotesPage extends InstallationPage {

	private Text text;

	private String releaseNotesFileName = "release-notes.txt";

	public ReleaseNotesPage() {
		// do nothing
	}

	@Override
	public void createControl(Composite parent) {

		text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.NO_FOCUS | SWT.WRAP);
		text.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(300);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		text.setLayoutData(gridData);
		text.setFont(JFaceResources.getTextFont());

		String releaseText;
		try {
			File path = new File(Platform.getInstallLocation().getURL().getPath()+releaseNotesFileName);
			String fullPath = null;
			if (path.exists()) {
				fullPath = path.getAbsolutePath();
			} else {
				Optional<File> loc = FileLocator.getBundleFileLocation(Platform.getBundle("org.dawnsci.product.plugin"));
				if (loc.isPresent()) {
					fullPath = Paths.get(loc.get().getParentFile().getAbsolutePath(), "org.dawnsci.base.product.feature", "release", releaseNotesFileName).toString();
				}
			}
			releaseText = fullPath != null ? readFile(fullPath) : "Release notes file could not be found";
		} catch (IOException e) {
			releaseText = "The following path could not be loaded: " + e.getMessage();
		}
		text.setText(releaseText);
	}

	private String readFile(String path) throws IOException {
		try (FileInputStream stream = new FileInputStream(new File(path))) {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
	}
}
