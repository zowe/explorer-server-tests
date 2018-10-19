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

import static com.ibm.atlas.webservices.tests.resources.AbstractDatasetsIntegrationTest.*;
import static com.ibm.atlas.webservices.tests.resources.AbstractDatasetsIntegrationTest.getAttributes;
import static com.ibm.atlas.webservices.tests.resources.AbstractUssFilesIntegrationTest.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.jobs.Job;

public class JobSubmitIntegrationTest extends AbstractJobsIntegrationTest {

	/**
	 * POST /api/v1/jobs
	 */
	@Test
	public void testSubmitJob() throws Exception {
		String dataSetPath = getTestJclMemberPath(JOB_IEFBR14);
		submitAndVerifySuccessfulJob("'" + dataSetPath + "'");
	}

	@Test
	public void testPostJobNotFullyQualified() throws Exception {
		String dataSetPath = getTestJclMemberPath(JOB_IEFBR14).replaceAll(HLQ + ".", "");
		submitAndVerifySuccessfulJob(dataSetPath);
	}

	@Test
	public void testPostJobInvalidJob() throws Exception {
		submitErrorJobByFileName("'ATLAS.TEST.JCL(INVALID)'", HttpStatus.SC_INTERNAL_SERVER_ERROR, "expectedResults/Jobs/Jobs_invalidDataset.txt");
	}

	@Test @Ignore("https://github.com/gizafoundation/giza-issues/issues/66")
	public void testPostJobFromUSS() throws Exception {
		String submitJobUssPath = USER_DIRECTORY + "/submitJob";
		createUssFileWithJobIfNescessary(submitJobUssPath);
		submitAndVerifySuccessfulJob(submitJobUssPath);
	}
	
	private void createUssFileWithJobIfNescessary(String submitJobUssPath) throws Exception {
		if (getAttributes(submitJobUssPath).getStatus() != HttpStatus.SC_OK) {
			createFile(submitJobUssPath, null);
			String jobContent = new String(Files.readAllBytes(Paths.get("testFiles/jobIEFBR14")));
			updateFileContent(submitJobUssPath, jobContent, null);
		}
	}

	private void submitAndVerifySuccessfulJob(String fileName) throws Exception {
		submitAndVerifyJob(fileName, "expectedResults/Jobs/JobsResponse.json");
	}

	private void submitAndVerifyJob(String fileString, String expectedResultFilePath) throws Exception {
		IntegrationTestResponse submitResponse = submitJobByFile(fileString).shouldHaveStatusCreated();
		Job actualJob = submitResponse.getEntityAs(Job.class);

		try {
			verifyJobIsAsExpected(expectedResultFilePath, actualJob);
			String expectedLocation = baseAtlasURI + JOBS_ROOT_ENDPOINT + "/" + actualJob.getJobName() + "/" + actualJob.getJobId();
			submitResponse.shouldHaveLocationHeader(expectedLocation);
		} finally {
			purgeJob(actualJob);
		}
	}

	private void submitErrorJobByFileName(String fileString, int expectedStatus, String expectedErrorFilePath) throws Exception {
		String actualError = submitJobByFile(fileString).shouldHaveStatus(expectedStatus).getEntity();
		String expectedError = new String(Files.readAllBytes(Paths.get(expectedErrorFilePath)));

		assertEquals(expectedError, actualError);
	}
}
