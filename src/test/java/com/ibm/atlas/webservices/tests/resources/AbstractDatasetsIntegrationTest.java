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

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;

import com.ibm.atlas.model.datasets.AllocationUnitType;
import com.ibm.atlas.model.datasets.CreateDataSetRequest;
import com.ibm.atlas.model.datasets.DataSetOrganisationType;
import com.ibm.atlas.utilities.JsonUtils;
import com.ibm.atlas.webservice.tests.junit.AbstractHTTPComparisonTest;
import com.ibm.json.java.JSONObject;

public class AbstractDatasetsIntegrationTest extends AbstractHTTPComparisonTest {

	static final String DATASETS_ROOT_ENDPOINT = "datasets";
	static final String HLQ = System.getProperty("atlas.username").toUpperCase();
	static final String TEST_JCL_PDS = HLQ + ".TEST.JCL";
	static final String INVALID_DATASET_NAME = HLQ + ".TEST.INVALID";
	static final String UNAUTHORIZED_DATASET = "IBMUSER.NOWRITE.CNTL";
	
	@BeforeClass
	public static void initialiseDatasetsIfNescessary() throws Exception {
		if (getContent(getTestJclMemberPath(JOB_IEFBR14),"").getStatus() != HttpStatus.SC_OK ) {
			createPds(TEST_JCL_PDS);
			createPdsMember(getTestJclMemberPath(JOB_IEFBR14), new String(Files.readAllBytes(Paths.get("testFiles/jobIEFBR14"))));
			createPdsMember(getTestJclMemberPath(JOB_WITH_STEPS), new String(Files.readAllBytes(Paths.get("testFiles/jobWithSteps"))));
		}
	}
	
	static String getDataSetMemberPath(String pds, String member) {
		return pds + "(" + member + ")";
	}
	
	static String getTestJclMemberPath(String member) {
		return getDataSetMemberPath(TEST_JCL_PDS, member);
	}

	static IntegrationTestResponse createDataset(String path, CreateDataSetRequest dataSetRequest)
		throws Exception {
		JSONObject request = JSONObject.parse(JsonUtils.convertToJsonString(dataSetRequest));
		return sendPostRequest(DATASETS_ROOT_ENDPOINT + "/" + path, request);
	}
	
	static IntegrationTestResponse createPds(String path) throws Exception {
		CreateDataSetRequest defaultJclPdsRequest = createPdsRequest();
		return createDataset(path, defaultJclPdsRequest);
	}

	static CreateDataSetRequest createPdsRequest() {
		CreateDataSetRequest defaultJclPdsRequest = CreateDataSetRequest.builder()
			.blksize(400)
			.primary(10)
			.lrecl(80)
			.secondary(5)
			.dirblk(21)
			.dsorg(DataSetOrganisationType.PO)
			.recfm("FB")
			.alcunit(AllocationUnitType.TRK)
			.build();
		return defaultJclPdsRequest;
	}
	
	static IntegrationTestResponse createSds(String path) throws Exception {
		CreateDataSetRequest sdsRequest = createSdsRequest();

		String jsonString = JsonUtils.convertToJsonString(sdsRequest);
		JSONObject request = JSONObject.parse(jsonString);
		return sendPostRequest(DATASETS_ROOT_ENDPOINT + "/" + path, request);
	}

	protected static CreateDataSetRequest createSdsRequest() {
		CreateDataSetRequest defaultJclPdsRequest = CreateDataSetRequest.builder()
			.blksize(400)
			.primary(10)
			.lrecl(80)
			.secondary(5)
			.dsorg(DataSetOrganisationType.PS)
			.recfm("FB")
			.alcunit(AllocationUnitType.TRK)
			.build();
		return defaultJclPdsRequest;
	}
	
	static IntegrationTestResponse createPdsMember(String memberPath, String content) throws Exception {
		return createPdsMember(memberPath, content, null);
	}
	
	static IntegrationTestResponse createPdsMember(String memberPath, String content, String checksum) throws Exception {
		JSONObject body = new JSONObject();
		body.put("records", content);
		if (checksum != null) {
			body.put("checksum", checksum);
		}
		return sendPutRequest(DATASETS_ROOT_ENDPOINT + "/" + memberPath + "/content", body);
	}
	
	static IntegrationTestResponse listDatasets(String filter) throws Exception {
		return sendGetRequest2(DATASETS_ROOT_ENDPOINT + "/" + filter);
	}
	
	static void listDatasetsShouldEqual(String filter, List<String> expected) throws Exception {
		List<String> expectedList = new ArrayList<String>();
		expectedList.addAll(expected);
		listDatasets(filter).shouldHaveStatusOk().shouldHaveEntity(expectedList);
	}
	
	static IntegrationTestResponse deleteDataset(String path) throws Exception {
		return sendDeleteRequest2(DATASETS_ROOT_ENDPOINT + "/" + path);
	}
	
	static IntegrationTestResponse getAttributes(String dataSetName) throws Exception {
		return sendGetRequest2(DATASETS_ROOT_ENDPOINT + "/" + dataSetName + "/attributes");
	}
	
	static IntegrationTestResponse getMembers(String dataSetName) throws Exception {
		return sendGetRequest2(DATASETS_ROOT_ENDPOINT + "/" + dataSetName + "/members");
	}
	
	static IntegrationTestResponse getContent(String path, String queryParams) throws Exception {
		String url = DATASETS_ROOT_ENDPOINT + "/" + path + "/content";
		if (queryParams != null && !queryParams.isEmpty()) {
			if (!queryParams.startsWith("?")) {
				url += "?";
			}
			url += queryParams;
		}
		return sendGetRequest2(url);
	}
	
	static IntegrationTestResponse updateDatasetContent(String path, String content, String checksum) throws UnsupportedEncodingException, Exception {
		String url = DATASETS_ROOT_ENDPOINT + "/" + path + "/content";
		JSONObject body = new JSONObject();
		body.put("records", content);
		if (checksum != null) {
			body.put("checksum", checksum);
		}
		return sendPutRequest(url, body);
	}
}