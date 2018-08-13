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

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.files.GetFileContentResponse;
import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.utilities.Utils;

public class UssUpdateFileContentTest extends AbstractUssFilesIntegrationTest {

	static String tempPath;

	@BeforeClass
	public static void setUpFile() throws Exception {
		tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.file, tempPath, null).shouldHaveStatusCreated();
	}

	@AfterClass
	public static void cleanUpFile() throws Exception {
		deleteResource(tempPath).shouldHaveStatusNoContent();
	}

	/**
	 * PUT /Atlas/api/uss/files/{path}/content
	 */

	@Test
	public void testUpdateWithoutChecksumWorks() throws Exception {
		testUpdate("testfile content");
	}

	@Test
	public void testUpdateWithSpecialCharsWorks() throws Exception {
		testUpdate("testfile content w!th $ # £ |\\~");
	}

	private void testUpdate(String content) throws UnsupportedEncodingException, Exception {
		testUpdate(content, null);
	}

	private void testUpdate(String content, String checksum) throws UnsupportedEncodingException, Exception {
		updateFileContent(tempPath, content, checksum).shouldHaveStatusNoContent();
		GetFileContentResponse actual = getContent(tempPath).shouldHaveStatusOk().getEntityAs(GetFileContentResponse.class);
		assertEquals(content, actual.getContent());
	}

	@Test
	public void testUpdateWithChecksumWorks() throws Exception {
		String currentChecksum = getContent(tempPath).getEntityAs(GetFileContentResponse.class).getChecksum();
		testUpdate("testfile content", currentChecksum);
	}

	@Test
	public void testUpdateWithIncorrectChecksum() throws Exception {
		String expectedErrorMessage = "The checksum supplied did not match the checksum returned by the previous read, so it is deemed a concurrent update has occurred";

		updateFileContent(tempPath, "test Content", "junk")
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining(expectedErrorMessage);
	}

	@Test @Ignore("Defect 19610")
	public void testUpdateFileWithoutAccess() throws Exception {
		String expectedErrorMessage = String.format("Operation on resource %s failed: Permission is denied", UNAUTHORISED_DIRECTORY);

		updateFileContent(UNAUTHORISED_DIRECTORY, "test Content", null)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN)
			.shouldHaveEntityContaining(expectedErrorMessage);
	}

	@Test
	@Ignore("blocked until we decide the desired behaviour")
	public void testUpdateFileWhichDoesntExist() throws Exception {
		String notExistantFile = USER_DIRECTORY + "dummy" + Utils.generateRandomString();
		updateFileContent(notExistantFile, "test Content", null)
			.shouldHaveStatus(HttpStatus.SC_NOT_FOUND)
			.shouldHaveEntityContaining(String.format("No file or directory at path %s could not be found", notExistantFile));
	}
}
