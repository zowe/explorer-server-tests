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

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.atlas.model.jobs.Job;

public class JobGetByIdTest extends AbstractJobsIntegrationTest {

	private static Job job;

	@BeforeClass
	public static void submitJob() throws Exception {
		job = submitJobAndPoll(JOB_IEFBR14);
	}

	@AfterClass
	public static void purgeJob() throws Exception {
		purgeJob(job);
	}

	/**
	 * GET /Atlas/api/jobs/<jobname>/<jobid>
	 */

	@Test
	public void testGetJobByNameAndId() throws Exception {
		Job actualJob = new IntegrationTestResponse(sendGetRequest(getJobUri(job))).shouldHaveStatusOk().getEntityAs(Job.class);

		verifyJobIsAsExpected("expectedResults/Jobs/JobsResponse.json", actualJob);
	}

	@Test
	public void testGetJobByNameAndNonexistingId() throws Exception {
		String uri = JOBS_ROOT_ENDPOINT + "/" + job.getJobName() + "/z000000";

		new IntegrationTestResponse(sendGetRequest(uri)).shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
	}
}