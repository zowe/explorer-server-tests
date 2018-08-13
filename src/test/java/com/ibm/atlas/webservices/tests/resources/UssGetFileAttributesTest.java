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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.atlas.model.files.GetFileAttributesResponse;
import com.ibm.atlas.model.files.GetFileChildAttributesResponse;
import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.utilities.Utils;

public class UssGetFileAttributesTest extends AbstractUssFilesIntegrationTest {

	/**
	 * GET /Atlas/api/uss/files/{path}
	 */

	private static String tempPath;

	@BeforeClass
	public static void setUpFile() throws Exception {
		// Setup a directory we can use
		tempPath = USER_DIRECTORY + Utils.generateRandomString();
		createResource(UssFileType.directory, tempPath, "755").shouldHaveStatusCreated();
		createResource(UssFileType.directory, tempPath + "/dir1", "555").shouldHaveStatusCreated();
		createResource(UssFileType.directory, tempPath + "/dir2", "750").shouldHaveStatusCreated();
		createResource(UssFileType.file, tempPath + "/file", "754").shouldHaveStatusCreated();
		updateFileContent(tempPath + "/file", "test content", null);
	}

	@AfterClass
	public static void cleanUpFile() throws Exception {
		deleteResource(tempPath).shouldHaveStatusNoContent();
	}

	@Test @Ignore("Defect 19608")
	public void testGetAttributesOfDirectoryWorks() throws Exception {
		List<GetFileChildAttributesResponse> children = new ArrayList<>();
		children.add(createChildObject(UssFileType.directory, "dir1"));
		children.add(createChildObject(UssFileType.directory, "dir2"));
		children.add(createChildObject(UssFileType.file, "file"));

		GetFileAttributesResponse expected = GetFileAttributesResponse.builder()
			.type(UssFileType.directory)
			.fileOwner(userName)
			.group("SYS1")
			.permissionsSymbolic("rwxr-xr-x")
			.permissionsNumeric("755")
			.size(8192l)
			.children(children)
			.build();

		IntegrationTestResponse response = getAttributes(tempPath).shouldHaveStatusOk();
		GetFileAttributesResponse actual = response.getEntityAs(GetFileAttributesResponse.class);
		assertEquals(expected, actual);
		assertDateIsRecent(actual.getLastModifiedDate());
	}

	@Test @Ignore("Defect 19608")
	public void testGetAttributesOfFileWorks() throws Exception {
		String filePath = tempPath + "/file";
		GetFileAttributesResponse expected = GetFileAttributesResponse.builder()
			.type(UssFileType.file)
			.fileOwner(userName)
			.group("SYS1")
			.permissionsSymbolic("rwxr-xr--")
			.permissionsNumeric("754")
			.size(12l)
			// TODO LATER - codepage: "codepage" : "IBM-1047"?
			// TODO  LATER - codepage: "contentType": "text"/"binary"/"mixed",?
			.content(new URI(baseAtlasURI + USS_ROOT_ENDPOINT + "/" + URLEncoder.encode(filePath, "UTF-8") + "/content"))
			.build();

		IntegrationTestResponse response = getAttributes(filePath).shouldHaveStatusOk();
		GetFileAttributesResponse actual = response.getEntityAs(GetFileAttributesResponse.class);
		assertEquals(expected, actual);
		assertDateIsRecent(actual.getLastModifiedDate());
	}

	private GetFileChildAttributesResponse createChildObject(UssFileType type, String name) throws URISyntaxException, UnsupportedEncodingException {
		return GetFileChildAttributesResponse.builder().name(name).type(type)
			.link(new URI(baseAtlasURI + USS_ROOT_ENDPOINT + "/" + URLEncoder.encode(tempPath + "/" + name, "UTF-8"))).build();
	}

	@Test  @Ignore("Defect 19610")
	public void testGetAttributesWithoutAccess() throws Exception {
		String unauthorisedFile = UNAUTHORISED_DIRECTORY + "/file";
		String expectedErrorMessage = String.format("Operation on resource %s failed: Permission is denied", unauthorisedFile);

		getAttributes(unauthorisedFile)
			.shouldHaveStatus(HttpStatus.SC_FORBIDDEN)
			.shouldHaveEntityContaining(expectedErrorMessage);
	}

	@Test
	public void testUpdateFileWhichDoesntExist() throws Exception {
		String notExistantFile = USER_DIRECTORY + "dummy" + Utils.generateRandomString();
		getAttributes(notExistantFile)
			.shouldHaveStatus(HttpStatus.SC_NOT_FOUND)
			.shouldHaveEntityContaining(String.format("No file or directory at path %s could not be found", notExistantFile));
	}
}
