/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */

package com.ibm.atlas.webservices.tests.resources;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.datasets.CreateDataSetRequest;
import com.ibm.atlas.model.datasets.DataSetContentResponse;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class DatasetsPostTest extends AbstractDatasetsIntegrationTest {

	
	private static final String VALID_DATASET_NAME 	= HLQ + ".TEST.DELETE";
	private static final String PUT_DATASET_NAME 		= HLQ + ".TEST.PUT";
	
	//TODO - once with have junit 5 nest the cleanup
	private String cleanUp = null;
	@After
	public void cleanUp() throws Exception {
		if (cleanUp != null) {
			deleteDataset(cleanUp);
		}
	}
	
	@Test
	public void testPostDataset() throws Exception {
		CreateDataSetRequest pdsRequest = createPdsRequest();
		
		createDataset(VALID_DATASET_NAME, pdsRequest).shouldHaveStatusCreated();
		cleanUp = VALID_DATASET_NAME;
		
		listDatasetsShouldEqual(VALID_DATASET_NAME, Arrays.asList(VALID_DATASET_NAME));

		JSONArray expected = getExpectedAttributes(VALID_DATASET_NAME, pdsRequest);
		
		JSONArray actual = getAttributes(VALID_DATASET_NAME).shouldHaveStatusOk().getEntityAsJsonArray();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testPostDatasetWithInvalidRequestFails() throws Exception {
		CreateDataSetRequest sdsRequestWithDirBlk = createSdsRequest();
		sdsRequestWithDirBlk.setDirblk(10);
		createDataset(VALID_DATASET_NAME, sdsRequestWithDirBlk).shouldHaveStatus(HttpStatus.SC_BAD_REQUEST);
	}
	
	@Test
	public void testPostDatasetAlreadyExists() throws Exception {
		createPds(TEST_JCL_PDS).shouldHaveStatus(HttpStatus.SC_CONFLICT);
	}
	
	@Test
	public void testUpdateWithChecksumWorks() throws Exception {
		createSds(PUT_DATASET_NAME);
		cleanUp = PUT_DATASET_NAME;
		String checksum = getContent(PUT_DATASET_NAME, "checksum=true").getEntityAs(DataSetContentResponse.class).getChecksum();
		updateDatasetContent(PUT_DATASET_NAME, "Some test file", checksum).shouldHaveStatusOk();
	}
	
	@Test
	public void testUpdateWithIncorrectChecksum() throws Exception {
		createSds(PUT_DATASET_NAME);
		cleanUp = PUT_DATASET_NAME;
		
		updateDatasetContent(PUT_DATASET_NAME, "Some test file", "junk")
			.shouldHaveStatus(HttpStatus.SC_PRECONDITION_FAILED);
			//TODO - create proper error message?
	}
	
	@Test @Ignore("z/os mf error")
	public void testUpdateDatasetWhichDoesntExist() throws Exception {
		String notExistantFile = HLQ + ".DUMMY";
		updateDatasetContent(notExistantFile, "test Content", null)
			.shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
			//TODO - create proper error message?
	}
		
	//TODO LATER actually test the basedns

	// Includes create, attributes and content testing
	@Test
	public void testPostDatasetCreateWithRecords() throws Exception{
		
		String expectedContent = "This is my test report\n";
		CreateDataSetRequest request = createSdsRequest();
		request.setRecfm("VB");
		request.setRecords(expectedContent);
		
		createDataset(VALID_DATASET_NAME, request);
		cleanUp = VALID_DATASET_NAME;

		JSONArray expected = getExpectedAttributes(VALID_DATASET_NAME, request);
		
		//Check attributes
		JSONArray actual = getAttributes(VALID_DATASET_NAME).shouldHaveStatusOk().getEntityAsJsonArray();
		assertEquals(expected, actual);

		//Check content
		DataSetContentResponse actualContent = getContent(VALID_DATASET_NAME, "convert=true").shouldHaveStatusOk().getEntityAs(DataSetContentResponse.class);
		assertEquals(expectedContent, actualContent.getRecords());
	}
	

	private JSONArray getExpectedAttributes(String dataSetName, CreateDataSetRequest request) {
		//TODO LATER - switch response to bring back ints, not strings
		JSONObject expectedAttributes = new JSONObject();
		expectedAttributes.put("name", dataSetName);
		expectedAttributes.put("blksize", request.getBlksize().toString());
		expectedAttributes.put("lrecl", request.getLrecl().toString());
		expectedAttributes.put("recfm", request.getRecfm());
		expectedAttributes.put("dsorg", request.getDsorg().name());
		JSONArray expected = new JSONArray();
		expected.add(0, expectedAttributes);
		return expected;
	}
}
