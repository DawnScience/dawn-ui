package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.dawnsci.mapping.ui.MappedDataViewState;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

public class MappedDataViewStateTest {

	private static final String FILE1_PATH = "dir1/file1.nxs";
	private static final String FILE2_PATH = "dir1/file2.nxs";
	private static final String FILE3_PATH = "dir2/file3.nxs";

	private static MappedDataViewState stateNullList;
	private static MappedDataViewState stateEmptyList;
	private static MappedDataViewState stateOneFile;
	private static MappedDataViewState stateMultipleFiles;

	@BeforeClass
	public static void setupClass() {
		stateNullList = new MappedDataViewState();

		stateEmptyList = new MappedDataViewState();
		stateEmptyList.setFilesInView(new ArrayList<>());

		stateOneFile = new MappedDataViewState();
		stateOneFile.setFilesInView(Arrays.asList(FILE1_PATH));

		stateMultipleFiles = new MappedDataViewState();
		stateMultipleFiles.setFilesInView(Arrays.asList(FILE1_PATH, FILE2_PATH, FILE3_PATH));
	}

	//-----------------------------------------
	// Test toString()
	//-----------------------------------------
	@Test
	public void testToStringNullList() {
		assertEquals("MappedDataViewState [filesInView=null]", stateNullList.toString());
	}

	@Test
	public void testToStringEmptyList() {
		assertEquals("MappedDataViewState [filesInView=[]]", stateEmptyList.toString());
	}

	@Test
	public void testToStringOneFile() {
		assertEquals("MappedDataViewState [filesInView=[dir1/file1.nxs]]", stateOneFile.toString());
	}

	@Test
	public void testToStringMultipleFiles() {
		assertEquals("MappedDataViewState [filesInView=[dir1/file1.nxs, dir1/file2.nxs, dir2/file3.nxs]]", stateMultipleFiles.toString());
	}

	//-----------------------------------------
	// Test equals()
	//-----------------------------------------
	@Test
	public void testEquals() {
		assertEquals(stateNullList, stateNullList);
		assertEquals(stateEmptyList, stateEmptyList);
		assertEquals(stateOneFile, stateOneFile);
		assertEquals(stateMultipleFiles, stateMultipleFiles);
	}

	@Test
	public void testNotEqualsDifferentNumbersOfFiles() {
		assertNotEquals(stateNullList, stateEmptyList);
		assertNotEquals(stateNullList, stateOneFile);
		assertNotEquals(stateNullList, stateMultipleFiles);
		assertNotEquals(stateEmptyList, stateOneFile);
		assertNotEquals(stateEmptyList, stateMultipleFiles);
		assertNotEquals(stateOneFile, stateMultipleFiles);
	}

	@Test
	public void testNotEqualDifferentContents() {
		final MappedDataViewState stateOneFile2 = new MappedDataViewState();
		stateOneFile2.setFilesInView(Arrays.asList(FILE2_PATH));
		assertNotEquals(stateOneFile, stateOneFile2);

		final MappedDataViewState stateMultipleFiles2 = new MappedDataViewState();
		stateMultipleFiles2.setFilesInView(Arrays.asList(FILE2_PATH, FILE3_PATH));
		assertNotEquals(stateMultipleFiles, stateMultipleFiles2);
	}

	@Test
	public void testNotEqualDifferentOrder() {
		final MappedDataViewState stateMultipleFiles2 = new MappedDataViewState();
		stateMultipleFiles2.setFilesInView(Arrays.asList(FILE2_PATH, FILE3_PATH, FILE1_PATH));
		assertNotEquals(stateMultipleFiles, stateMultipleFiles2);
	}

	//-----------------------------------------
	// Test marshalling
	//-----------------------------------------
	@Test
	public void testMarshallingNullList() throws Exception {
		final IMarshallerService service = getService();
		final String json = service.marshal(stateNullList);
		assertEquals("{}", json);
		assertEquals(stateNullList, service.unmarshal(json, MappedDataViewState.class));
	}

	@Test
	public void testMarshallingEmptyList() throws Exception {
		final IMarshallerService service = getService();
		final String json = service.marshal(stateEmptyList);
		assertEquals("{\"filesInView\":[]}", json);
		assertEquals(stateEmptyList, service.unmarshal(json, MappedDataViewState.class));
	}

	@Test
	public void testMarshallingOneFile() throws Exception {
		final IMarshallerService service = getService();
		final String json = service.marshal(stateOneFile);
		assertEquals("{\"filesInView\":[\"dir1/file1.nxs\"]}", json);
		assertEquals(stateOneFile, service.unmarshal(json, MappedDataViewState.class));
	}

	@Test
	public void testMarshallingMultipleFiles() throws Exception {
		final IMarshallerService service = getService();
		final String json = service.marshal(stateMultipleFiles);
		assertEquals("{\"filesInView\":[\"dir1/file1.nxs\",\"dir1/file2.nxs\",\"dir2/file3.nxs\"]}", json);
		assertEquals(stateMultipleFiles, service.unmarshal(json, MappedDataViewState.class));
	}

	private IMarshallerService getService() {
		// Non-OSGi for test - do not copy!
		return new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller()));
	}
}
