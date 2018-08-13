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
import org.junit.Test;

import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.utilities.Utils;

public class UssCreateFilesTest extends AbstractUssFilesIntegrationTest {

	private final String ATLAS_TEST_FILE = USER_DIRECTORY + "ATLAS_TEST.txt";

	/**
	 * POST /Atlas/api/uss/files/
	 */

	@Test
	public void testCreateDirectoryWithDefaultPermissions() throws Exception {
		testCreateDirectory(null);
	}

	@Test
	public void testCreateDirectoryWithCustomPermissions() throws Exception {
		testCreateDirectory("540");
	}

	@Test
	public void testCreateDirectoryWithoutAccess() throws Exception {
		String unauthorisedDirectory = UNAUTHORISED_DIRECTORY + "/" + Utils.generateRandomString();
		createDirectory(unauthorisedDirectory, null)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}

	@Test
	public void testCreateDirectoryWhichAlreadyExists() throws Exception {
		createDirectory(U_DIRECTORY, null)
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining(String.format("Attempt to create new directory at path %s failed as file already exists", U_DIRECTORY));
	}

	@Test
	public void testCreateDirectoryWithInvalidPermissions() throws Exception {
		String tempDirectoryPath = USER_DIRECTORY + Utils.generateRandomString();
		createDirectory(tempDirectoryPath, "999")
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining("Invalid permissions string specified, please use numeric notation eg \"777\"");
	}

	@Test
	public void testCreateFileWithDefaultPermissions() throws Exception {
		testCreateFile(null);
	}

	@Test
	public void testCreateFileWithCustomPermissions() throws Exception {
		testCreateFile("540");
	}

	@Test
	public void testCreateFileWithoutAccess() throws Exception {
		String unauthorisedDirectory = UNAUTHORISED_DIRECTORY + "/" + Utils.generateRandomString();
		createFile(unauthorisedDirectory, null)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void testCreateFileWhichAlreadyExists() throws Exception {
		createFile(ATLAS_TEST_FILE, null);
		
		createFile(ATLAS_TEST_FILE, null)
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining(String.format("Attempt to create new file at path %s failed as file already exists", ATLAS_TEST_FILE));
		
		deleteResource(ATLAS_TEST_FILE);
	}

	@Test
	public void testCreateFileWithInvalidPermissions() throws Exception {
		String tempDirectoryPath = USER_DIRECTORY + Utils.generateRandomString();
		createFile(tempDirectoryPath, "999")
			.shouldHaveStatus(HttpStatus.SC_BAD_REQUEST)
			.shouldHaveEntityContaining("Invalid permissions string specified, please use numeric notation eg \"777\"");
	}

	private void testCreateFile(String permissions) throws Exception {
		testCreateResource(UssFileType.file, permissions);
	}
	
	private void testCreateDirectory(String permissions) throws Exception {
		testCreateResource(UssFileType.directory, permissions);
	}
	
	private void testCreateResource(UssFileType type, String permissions) throws Exception {
		String tempDirectoryPath = USER_DIRECTORY + Utils.generateRandomString();
		String expectedLocation = baseAtlasURI + USS_ROOT_ENDPOINT + tempDirectoryPath;

		createResource(type, tempDirectoryPath, permissions)
			.shouldHaveStatusCreated()
			.shouldHaveLocationHeader(expectedLocation);

		if (permissions != null) {
			verifyPermissions(tempDirectoryPath, permissions);
		} else {
			verifyPermissionsInherited(tempDirectoryPath);
		}
		deleteResource(tempDirectoryPath);
	}
}
