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

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.jobs.Job;
import com.ibm.atlas.model.jobs.JobStatus;

public class JobsTest extends AbstractJobsIntegrationTest {
	
	private static Job job;

	@BeforeClass
	public static void submitJob() throws Exception {
		job = submitJobAndPoll(JOB_IEFBR14, JobStatus.OUTPUT);
	}

	@AfterClass
	public static void purgeJob() throws Exception {
		purgeJob(job);
	}
	
	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobs() {
		System.out.println("> testGetJobs()");
				
		String relativeURI				= "jobs";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}
	
	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobsWithUnlikelyPrefix() {
		System.out.println("> testGetJobsWithUnlikelyPrefix()");
				
		String relativeURI				= "jobs?prefix=12345678";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_unlikelyPrefix.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobsWithInvalidPrefix() {
		System.out.println("> testGetJobsWithInvalidPrefix()");
				
		String relativeURI				= "jobs?prefix=123456789";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_invalidPrefix.txt";
		int expectedReturnCode			= HttpStatus.SC_BAD_REQUEST;
		
		HashMap<String,String> substitutionVars = new HashMap<String,String>();
		substitutionVars.put("ATLAS.USERNAME", System.getProperty("atlas.username").toUpperCase());

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, substitutionVars, false);
	}
	
	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobsWithUnlikelyOwner() {
		System.out.println("> testGetJobsWithUnlikelyOwner()");
				
		String relativeURI				= "jobs?owner=12345678"; 
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_unlikelyPrefix.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobsWithInvalidOwner() {
		System.out.println("> testGetJobsWithInvalidOwner()");
				
		String relativeURI				= "jobs?owner=123456789";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_invalidOwner.txt";
		int expectedReturnCode			= HttpStatus.SC_BAD_REQUEST;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, false);
	}

	/**
	 * GET /Atlas/jobs
	 */
	@Test
	public void testGetJobsWithOwnerAndPrefix() {
		System.out.println("> testGetJobsWithOwnerAndPrefix()");
				
		String relativeURI				= "jobs?owner="+System.getProperty("atlas.username")+"&prefix=*";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/Jobs_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs 
	 */
	@Test
	public void testGetJobsWithCurrentUserAsOwnerAndSpecificPrefix() throws Exception {
		System.out.println("> testGetJobsWithSpecificOwnerAndPrefix()");
		
		String relativeURI				= "jobs?prefix=" + job.getJobName();
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/Jobs_specificPrefix_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, getSubstitutionVars(job), true);
	}

	/**
	 * GET /Atlas/jobs
	 * @throws Exception 
	 */
	@Test
	public void testGetJobsWithCurrentUserAsOwnerSpecificPrefixAndStatus() throws Exception {
		System.out.println("> testGetJobsWithSpecificOwnerAndPrefix()");
				
		String relativeURI				= "jobs?status=OUTPUT&prefix=" + job.getJobName();
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/Jobs_specificPrefix_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, getSubstitutionVars(job), true);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids
	 */
	@Test
	public void testGetJobIds() throws Exception {
		System.out.println("> testGetJobIds()");

				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/ids_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, getSubstitutionVars(job), true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids
	 */
	@Test
	public void testGetJobIdsForInvalidJobName() {
		System.out.println("> testGetJobIdsForInvalidJobName()");
				
		String relativeURI				= "jobs/FAKEJOBNAME/ids";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/ids_invalidJobName.txt";
		int expectedReturnCode			= HttpStatus.SC_BAD_REQUEST;
		
		HashMap<String,String> substitutionVars = new HashMap<>();
		substitutionVars.put("ATLAS.USERNAME", System.getProperty("atlas.username").toUpperCase());
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, substitutionVars, false);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids
	 */
	@Test
	public void testGetJobIdsOwner() throws Exception {
		System.out.println("> testGetJobIdsOwner()");
			
		String relativeURI				= "jobs/" + job.getJobName() + "/ids?owner=" + System.getProperty("atlas.username");
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/Jobs_advance_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids
	 */
	@Test
	public void testGetJobIdsInvalidOwner() throws Exception {
		System.out.println("> testGetJobIdsInvalidOwner()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids?owner=12345678";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/Jobs_unlikelyOwner.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps
	 */
	@Test
	public void testGetJobSteps() throws Exception {
		System.out.println("> testGetJobSteps()");
		
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/steps";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/steps.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps
	 */
	@Test @Ignore("temporarily disabled - this test relies on a specific job execution that is not run as part of the automated tests, which may be purged")
	public void testGetJobStepsNoStepsAvailable() {
		System.out.println("> testGetJobStepsNoStepsAvailable()");
				
		String relativeURI				= "jobs/DBJ1IRLM/ids/STC12204/steps";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/steps_noStepsAvailable.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
		
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps
	 */
	@Test
	public void testGetJobStepsNoStepsForThisJobType() {
		System.out.println("> testGetJobStepsNoStepsForThisJobType()");
				
		String relativeURI				= "jobs/$MASCOMM/ids/STC00001/steps";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/steps_noStepsAvailableForThisJobType.txt";
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps
	 */
	@Test
	public void testGetJobStepsInvalidJob() {
		System.out.println("> testGetJobStepsInvalidJob()");
				
		String relativeURI				= "jobs/FAKEJOB/ids/FAKEID/steps";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/steps_invalidJob.txt";
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps/{stepNumber}/dds
	 */
	@Test
	public void testGetJobStepDDs() throws Exception {
		System.out.println("> testGetJobStepDDs()");
		
		Job jobWithDD = submitJobAndPoll(JOB_WITH_STEPS, JobStatus.OUTPUT);
		
		String relativeURI				= "jobs/" + jobWithDD.getJobName() + "/ids/" + jobWithDD.getJobId() + "/steps/1/dds";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/dds/dds.json";
		int expectedReturnCode			= HttpStatus.SC_OK;
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
		purgeJob(jobWithDD);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps/{stepNumber}/dds
	 */
	@Test
	public void testGetJobStepDDsInvalidStep() throws Exception {
		System.out.println("> testGetJobStepDDsInvalidStep()");
		
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/steps/99/dds";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/dds/dds_invalidStep.txt";
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, getSubstitutionVars(job), false);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps/{stepNumber}/dds
	 */
	@Test
	public void testGetJobStepDDsInvalidJob() {
		System.out.println("> testGetJobStepDDsInvalidJob()");
		
		String relativeURI				= "jobs/FAKEJOB/ids/FAKEID/steps/1/dds";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/dds/dds_invalidJob.txt";
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/steps/{stepNumber}/dds
	 */
	@Test
	public void testGetJobStepDDsNotAvailableForThisJobType() {
		System.out.println("> testGetJobStepDDsNotAvailableForThisJobType()");
		
		String relativeURI				= "jobs/$MASCOMM/ids/STC00001/steps/1/dds";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath = "expectedResults/Jobs/ids/steps/dds/dds_noDDsAvailableForThisJobType.txt";
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files
	 */
	@Test
	public void testGetJobOutputFiles() throws Exception {
		System.out.println("> testGetJobOutputFiles()");
		
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath	= "expectedResults/Jobs/ids/files/files_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files
	 */
	@Test
	public void testGetJobOutputFilesInvalidJobId() throws Exception {
		System.out.println("> testGetJobOutputFilesInvalidJobId()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/z/files";
		String httpMethodType			= HttpGet.METHOD_NAME;
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, null, expectedReturnCode);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files
	 */
	@Test
	public void testGetJobOutputFilesInvalidJobNameAndId() {
		System.out.println("> testGetJobOutputFilesInvalidJobNameAndId()");
				
		String relativeURI				= "jobs/z/ids/z/files";
		String httpMethodType			= HttpGet.METHOD_NAME;
		int expectedReturnCode			= HttpStatus.SC_NOT_FOUND;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, null, expectedReturnCode);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files{fieldId}
	 */
	@Test
	public void testGetJobOutputFileFieldId() throws Exception {
		System.out.println("> testGetJobOutputFileFieldId()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files{fieldId}
	 */
	@Test
	public void testGetJobOutputFileFieldIdStartParam() throws Exception {
		System.out.println("> testGetJobOutputFileFieldIdStartParam()");

		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2?start=2";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files{fieldId}
	 */
	@Test
	public void testGetJobOutputFileFieldIdEndParam() throws Exception {
		System.out.println("> testGetJobOutputFileFieldIdEndParam()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2?end=0";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files{fieldId}
	 */
	@Test
	public void testGetJobOutputFileFieldIdStartEndParam() throws Exception {
		System.out.println("> testGetJobOutputFileFieldIdStartEndParam()");
					
			String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2?start=0&end=1";
			String httpMethodType			= HttpGet.METHOD_NAME;
			String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_line1.json";
			int expectedReturnCode			= HttpStatus.SC_OK;
			
			runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode);
	}
	
	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files/{fileId}/tail
	 */
	@Test
	public void testGetJobOutputFileFieldIdTail() throws Exception {
		System.out.println("> testGetJobOutputFileFieldIdTail()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2/tail";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

	/**
	 * GET /Atlas/jobs/{jobName}/ids/{jobId}/files/{fileId}/tail
	 */
	@Test
	public void testGetJobOutputFileFieldIdTailRecords() throws Exception {
		System.out.println("> testGetJobOutputFileFieldIdTailRecords()");
				
		String relativeURI				= "jobs/" + job.getJobName() + "/ids/" + job.getJobId() + "/files/2/tail?records=1";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Jobs/ids/files/JESMSGLG_tail1.json";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

}