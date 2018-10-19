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
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.utilities.Utils;

public class UssDeleteFilesTest extends AbstractUssFilesIntegrationTest {

	/**
	 * DELETE /api/v1/uss/files/
	 */
	
	@Test
	public void testDeleteDirectoryWorks() throws Exception {
		String tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.directory, tempPath, null).shouldHaveStatusCreated();
			
		deleteResource(tempPath)
			.shouldHaveStatusNoContent();
	}

	@Test
	public void testDeleteNonEmptyDirectoryWorks() throws Exception {
		String tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.directory, tempPath, null).shouldHaveStatusCreated();
		createResource(UssFileType.file, tempPath + "/child", null).shouldHaveStatusCreated();
			
		deleteResource(tempPath)
			.shouldHaveStatusNoContent();
	}
	
	@Test
	public void testDeleteFileWorks() throws Exception {
		String tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.file, tempPath, null).shouldHaveStatusCreated();
			
		deleteResource(tempPath)
			.shouldHaveStatusNoContent();
	}
	
	@Test @Ignore("Task 19610")
	public void testDeleteFileWithoutAccess() throws Exception {
		deleteResource(UNAUTHORISED_DIRECTORY)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void testDeleteFileWhichDoesntExist() throws Exception {
		String notExistantFile = USER_DIRECTORY + "dummy" + Utils.generateRandomString();
		deleteResource(notExistantFile)
			.shouldHaveStatus(HttpStatus.SC_NOT_FOUND)
			.shouldHaveEntityContaining(String.format("No file or directory at path %s could not be found", notExistantFile));
	}
}
