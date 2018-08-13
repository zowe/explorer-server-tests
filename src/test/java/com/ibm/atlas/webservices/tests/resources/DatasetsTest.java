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

import static com.ibm.atlas.webservices.tests.resources.AbstractJobsIntegrationTest.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.datasets.DataSetContentResponse;

public class DatasetsTest extends AbstractDatasetsIntegrationTest {

	private final String VALID_TEST_MEMBER 		= "(IEFBR14)";
	private final String FILTER_DATASET_STRING 	= HLQ + "*";
	private final String FILTER_MIDDLE_DATASET_STRING 	= HLQ + ".*.JCL";
	private final String UNAUTHORIZED_DATASET_MEMBER 	= UNAUTHORIZED_DATASET + "(MISC)";
	
	@Test
	public void testGetAllDatasets() throws Exception {
		List<String> dataSetList = listDatasets(HLQ).shouldHaveStatusOk().getEntityAsListOfStrings();
		for (String dataSet : dataSetList) {
			assertTrue(dataSet.startsWith(HLQ));
		}
	}
	
	@Test
	public void testGetFilteredDatasets() throws Exception {
		List<String> dataSetList = listDatasets(FILTER_DATASET_STRING).shouldHaveStatusOk().getEntityAsListOfStrings();
		for (String dataSet : dataSetList) {
			assertTrue(dataSet.matches(generateRegexPattern(FILTER_DATASET_STRING)));
		}
	}
	
	@Test
	public void testGetValidDataset() throws Exception {
		listDatasetsShouldEqual(TEST_JCL_PDS, Arrays.asList(TEST_JCL_PDS));
	}
	
	@Test
	public void testGetInvalidDataset() throws Exception {
		listDatasets(INVALID_DATASET_NAME).shouldHaveStatusOk().shouldHaveEntity(Collections.<String>emptyList());
	}
	
	@Test
	public void testGetValidDatasetAttributes() throws Exception {
		String attributesRegex = "\\[\\{\\\"name\\\"\\:\\\".+?\\\",\\\"blksize\\\"\\:\\\"\\d+?\\\",\\\"lrecl\\\"\\:\\\"\\d+?\\\",\\\"recfm\\\"\\:\\\"\\w+?\\\",\\\"dsorg\\\"\\:\\\"\\w+?(-E)?\\\"\\},?\\]";
		getAttributes(TEST_JCL_PDS).shouldHaveStatusOk().shouldHaveEntityMatching(attributesRegex);
	}
	
	@Test
	public void testGetInvalidDatasetAttributes() throws Exception {
		getAttributes(INVALID_DATASET_NAME).shouldHaveStatusOk().shouldHaveEntity(Collections.<String>emptyList());
	}
	
	@Test
	public void testGetValidDatasetMembers() throws Exception {
		List<String> members = getMembers(TEST_JCL_PDS).shouldHaveStatusOk().getEntityAsListOfStrings();
		assertTrue(members.contains(JOB_IEFBR14));
		assertTrue(members.contains(JOB_WITH_STEPS));
	}
	
	@Test
	public void testGetInvalidDatasetMembers() throws Exception {
		getMembers(INVALID_DATASET_NAME).shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void testGetDatasetMemberContent() throws Exception {
		testGetIefbr14MemberContent(null,null);
	}
	
	@Test
	public void testGetDatasetMemberContentWithConvert() throws Exception {
		testGetDatasetMemberContent(TEST_JCL_PDS + VALID_TEST_MEMBER, new String(Files.readAllBytes(Paths.get("testFiles/jobIEFBR14"))), "convert=true");
	}

	@Test
	public void testGetDatasetMemberContentWithConvertChecksum() throws Exception {
		DataSetContentResponse actual = testGetDatasetMemberContent(TEST_JCL_PDS + VALID_TEST_MEMBER, new String(Files.readAllBytes(Paths.get("testFiles/jobIEFBR14"))), "convert=true&checksum=true");
		assertNotNull(actual.getChecksum());
	}
	
	private DataSetContentResponse testGetDatasetMemberContent(String path, String expectedContent, String queryParams) throws Exception {
		DataSetContentResponse actual = getContent(path, queryParams).shouldHaveStatusOk().getEntityAs(DataSetContentResponse.class);
		assertEquals(expectedContent, actual.getRecords());
		return actual;
	}
	
	@Test
	public void testGetDatasetMemberContentWithConvertStart1End1() throws Exception {
		testGetIefbr14MemberContent(1,1);
	}
	
	@Test
	public void testGetDatasetMemberContentWithConvertStart1End2() throws Exception {
		testGetIefbr14MemberContent(1,2);
	}
	
	@Test
	public void testGetDatasetMemberContentWithConvertEnd1() throws Exception {
		testGetIefbr14MemberContent(null,1);
	}
	
	@Test
	public void testGetDatasetMemberContentWithInvalidStart() throws Exception {
		testGetIefbr14MemberContent(10,1);
	}
	
	@Test
	public void testGetDatasetMemberContentWithInvalidEnd() throws Exception {
		testGetIefbr14MemberContent(null,10);
	}
	
	private void testGetIefbr14MemberContent(Integer start, Integer end) throws Exception {
		String queryParams = createQueryParams(start, end);
		StringBuffer expectedRecords = getExpectedRecords(start, end);
		DataSetContentResponse expected = DataSetContentResponse.builder().records(expectedRecords.toString()).build();

		getContent(TEST_JCL_PDS + VALID_TEST_MEMBER, queryParams).shouldHaveStatusOk().shouldHaveEntity(expected);
	}

	private String createQueryParams(Integer start, Integer end) {
		String queryParms = "convert=true";
		if (start != null) {
			queryParms += "&start=" + start;
		}
		if (end != null) {
			queryParms += "&end=" + end;
		}
		return queryParms;
	}
	
	private StringBuffer getExpectedRecords(Integer start, Integer end) throws IOException {
		List<String> expectedLines = Files.readAllLines(Paths.get("testFiles/jobIEFBR14"));
		int maxEnd = expectedLines.size();
		if (start == null) {
			start = 0;
		}
		if (end == null) {
			end = maxEnd;
		}
		
		StringBuffer expectedRecords = new StringBuffer();;
		for (int i = start; i <= end && i < maxEnd; i++) {
			expectedRecords.append(expectedLines.get(i)).append("\n");
		}
		return expectedRecords;
	}
	
	@Test
	public void testGetFilteredDatasetsUnexpectedCharacters() throws Exception {
		listDatasets("?£").shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
	}
	
	@Test
	public void testGetMiddleFilteredDatasets() throws Exception {
		String matcher = generateRegexPattern("[\"" + TEST_JCL_PDS + "\"]");
		listDatasets(FILTER_MIDDLE_DATASET_STRING).shouldHaveStatusOk().shouldHaveEntityMatching(matcher);
	}
	
	private String generateRegexPattern(String initial) {
		return initial
			.replace("[", "\\[")
			.replace("]", "\\]")
			.replace("*", ".*");
	}
	
	@Test @Ignore("Task 19604")
	public void testGetUnauthoriszedDatasetMembers() throws Exception {
		getMembers(UNAUTHORIZED_DATASET).shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test @Ignore("Task 19604")
	public void testGetUnauthoriszedDatasetContent() throws Exception {
		getContent(UNAUTHORIZED_DATASET_MEMBER, null).shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}
}
