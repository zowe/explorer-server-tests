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

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.atlas.model.files.GetFileContentResponse;
import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.utilities.Utils;

public class UssGetFileContentTest extends AbstractUssFilesIntegrationTest {

	/**
	 * GET /api/v1/uss/files/{path}/content
	 */

	private static String tempPath;
	
	@BeforeClass
	public static void setUpFile() throws Exception {
		tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.directory, tempPath, "755").shouldHaveStatusCreated();
		createResource(UssFileType.file, tempPath + "/file1", "755").shouldHaveStatusCreated();
		createResource(UssFileType.file, tempPath + "/file2", "755").shouldHaveStatusCreated();
		updateFileContent(tempPath + "/file1", "test content", null);
		updateFileContent(tempPath + "/file2", "test content w!th funny chars # £ $ @", null);
	}

	@AfterClass
	public static void cleanUpFile() throws Exception {
		deleteResource(tempPath).shouldHaveStatusNoContent();
	}

	@Test
	public void testGetContentOfSimpleFileWorks() throws Exception {
		testGetContent("file1", "test content");
	}
	
	@Test
	public void testGetContentOfFileWithFunnyCharsWorks() throws Exception {
		testGetContent("file2", "test content w!th funny chars # £ $ @");
	}

	private void testGetContent(String filename, String expectedContent) throws JsonParseException, JsonMappingException, IOException, Exception {
		GetFileContentResponse actual = getContent(tempPath + "/" + filename).shouldHaveStatusOk().getEntityAs(GetFileContentResponse.class);
		assertEquals(expectedContent, actual.getContent());
		assertNotNull(actual.getChecksum());
	}

	@Test
	public void testGetContentOfDirectory() throws Exception {
		String expectedErrorMessage = String.format("The resource at path %s was not a file, so get content is not valid", tempPath);

		getContent(tempPath)
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining(expectedErrorMessage);
	}
	
	@Test @Ignore("Defect 19610")
	public void testGetContentWithoutAccess() throws Exception {
		String unauthorisedFile = UNAUTHORISED_DIRECTORY + "/file";
		String expectedErrorMessage = String.format("Operation on resource %s failed: Permission is denied", unauthorisedFile);

		getContent(unauthorisedFile)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN)
			.shouldHaveEntityContaining(expectedErrorMessage);
	}

	@Test
	public void testGetContentFileWhichDoesntExist() throws Exception {
		String notExistantFile = USER_DIRECTORY + "dummy" + Utils.generateRandomString();
		getAttributes(notExistantFile)
			.shouldHaveStatus(HttpStatus.SC_NOT_FOUND)
			.shouldHaveEntityContaining(String.format("No file or directory at path %s could not be found", notExistantFile));
	}
}
