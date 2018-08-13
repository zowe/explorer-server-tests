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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.atlas.model.jobs.Job;
import com.ibm.atlas.model.jobs.JobStatus;
import com.ibm.atlas.utilities.JsonUtils;
import com.ibm.atlas.webservice.tests.junit.AbstractHTTPComparisonTest;
import com.ibm.json.java.JSONObject;

public class AbstractJobsIntegrationTest extends AbstractHTTPComparisonTest {

	static final String JOBS_ROOT_ENDPOINT = "api/jobs";
	
	static final String JOB_IEFBR14 = "IEFBR14";
	static final String JOB_WITH_STEPS = "JOB1DD";

	@BeforeClass
	public static void setUpJobDatasetsIfRequired() throws Exception {
		AbstractDatasetsIntegrationTest.initialiseDatasetsIfNescessary();
	}

	static Job submitJobAndPoll(String testJob) throws Exception {
		return submitJobAndPoll(testJob, null);
	}

	static Job submitJobAndPoll(String testJob, JobStatus waitForState) throws Exception {
		Job job = submitJob(testJob);
		String jobName = job.getJobName();
		String jobId = job.getJobId();
		assertPoll(jobName, jobId, waitForState);
		return job;
	}

	public static Job submitJob(String jobFile) throws Exception {
		String jobFileString = "'" + AbstractDatasetsIntegrationTest.getTestJclMemberPath(jobFile) + "'";
		return submitJobByFile(jobFileString).shouldHaveStatusCreated().getEntityAs(Job.class);
	}
	
	public static IntegrationTestResponse purgeJob(Job job) throws Exception {
		return sendDeleteRequest2(getJobUri(job));
	}

	protected static String getJobUri(Job job) {
		return JOBS_ROOT_ENDPOINT + "/" + job.getJobName() + "/" + job.getJobId();
	}
	
	private static void assertPoll(String jobName, String jobId, JobStatus waitForState) throws Exception {
		Assert.assertTrue("Failed to verify job submit, jobname:" + jobName + ", jobid:" + jobId, pollJob(jobName, jobId, waitForState));
	}

	public static boolean pollJob(String jobName, String jobId, JobStatus waitForState) throws Exception {
		String uri = JOBS_ROOT_ENDPOINT + "/" + jobName + "/" + jobId;

		for (int i = 0; i < 20; i++) {
			IntegrationTestResponse response = new IntegrationTestResponse(sendGetRequest(uri));
			System.out.println("Response status is: " + response.getStatus());
			if (response.getStatus() == HttpStatus.SC_OK) {
				if (waitForState != null) {
					Job jobResponse = response.getEntityAs(Job.class);
					System.out.println("Job status is: " + jobResponse.getStatus());
					if (waitForState == jobResponse.getStatus()) {
						return true;
					}
				} else {
					return true;
				}
			}
			Thread.sleep(1200);
		}
		return false;
	}
	
	static HashMap<String, String> getSubstitutionVars(Job job) {
		HashMap<String, String> substitutionVars = new HashMap<>();
		substitutionVars.put("JOBNAME", job.getJobName());
		substitutionVars.put("JOBID", job.getJobId());
		substitutionVars.put("ATLAS.USERNAME", System.getProperty("atlas.username"));
		return substitutionVars;
	}

	Job getSubstitutionVars(Job toUpdate, Job updateJob) {
		if ("${JOBID}".equals(toUpdate.getJobId())) {
			toUpdate.setJobId(updateJob.getJobId());
		}
		if ("${JOBNAME}".equals(toUpdate.getJobName())) {
			toUpdate.setJobName(updateJob.getJobName());
		}
		if (JobStatus.ALL == toUpdate.getStatus()) {
			toUpdate.setStatus(updateJob.getStatus());
		}
		if ("${ANY}".equals(toUpdate.getPhaseName())) {
			toUpdate.setPhaseName(updateJob.getPhaseName());
		}
		if ("${ANY}".equals(toUpdate.getReturnCode())) {
			toUpdate.setReturnCode(updateJob.getReturnCode());
		}
		if ("${ANY}".equals(toUpdate.getExecutionClass())) {
			toUpdate.setExecutionClass(updateJob.getExecutionClass());
		}
		if ("${ATLAS.USERNAME}".equals(toUpdate.getOwner())) {
			toUpdate.setOwner(System.getProperty("atlas.username").toUpperCase());
		}
		return toUpdate;
	}

	void verifyJobIsAsExpected(String expectedResultFilePath, Job actualJob) throws JsonParseException, JsonMappingException, IOException {
		Job expectedJob = JsonUtils.convertFilePath(Paths.get(expectedResultFilePath), Job.class);
		expectedJob = getSubstitutionVars(expectedJob, actualJob);

		assertEquals(expectedJob, actualJob);
	}

	static IntegrationTestResponse submitJobByFile(String fileString) throws Exception {
		JSONObject body = new JSONObject();
		body.put("file", fileString);

		return sendPostRequest(JOBS_ROOT_ENDPOINT, body);
	}

}