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
import java.net.URLEncoder;

import org.junit.BeforeClass;

import com.ibm.atlas.model.files.GetFileAttributesResponse;
import com.ibm.atlas.model.files.GetFileChildAttributesResponse;
import com.ibm.atlas.model.files.UssFileType;
import com.ibm.atlas.webservice.tests.junit.AbstractHTTPComparisonTest;
import com.ibm.json.java.JSONObject;

public class AbstractUssFilesIntegrationTest extends AbstractHTTPComparisonTest {

	

	static final String USS_ROOT_ENDPOINT = "api/uss/files";
	
	final static String U_DIRECTORY = System.getProperty("baseDirectory") == null ? "/u/" : System.getProperty("baseDirectory");
	final static String USER_DIRECTORY = U_DIRECTORY + System.getProperty("atlas.username").toLowerCase() + "/";
	final String ATLAS_TEST_FILE = USER_DIRECTORY + "ATLAS_TEST.txt";
	static final String NO_WRITE_PERMISSION = "noWritePermission";
	static final String UNAUTHORISED_DIRECTORY = USER_DIRECTORY + NO_WRITE_PERMISSION;
	
	@BeforeClass
	public static void setUpInaccessibleFileIfRequired() throws Exception {
		GetFileAttributesResponse parentAttributes = getAttributes(USER_DIRECTORY).getEntityAs(GetFileAttributesResponse.class);
		for (GetFileChildAttributesResponse child : parentAttributes.getChildren()) {
			if (child.getName().equals(NO_WRITE_PERMISSION)) {
				return;
			}
		}
		createResource(UssFileType.directory, UNAUTHORISED_DIRECTORY, "440").shouldHaveStatusCreated();
	}
	
	static IntegrationTestResponse createFile(String path, String permissions) throws Exception {
		return createResource(UssFileType.file, path, permissions);
	}
	
	static IntegrationTestResponse createDirectory(String path, String permissions) throws Exception {
		return createResource(UssFileType.directory, path, permissions);
	}
	
	static IntegrationTestResponse createResource(UssFileType type, String path, String permissions) throws Exception {
		JSONObject body = new JSONObject();
		if (permissions != null) {
			body.put("permissions", permissions);
		}
		body.put("path", path);
		body.put("type", type.toString());

		return sendPostRequest(USS_ROOT_ENDPOINT, body);
	}
	
	static IntegrationTestResponse deleteResource(String path) throws Exception {
		return sendDeleteRequest2(USS_ROOT_ENDPOINT + "/" + URLEncoder.encode(path, "UTF-8"));
	}
	
	static IntegrationTestResponse getAttributes(String path) throws Exception {
		return sendGetRequest2(USS_ROOT_ENDPOINT + "/" + URLEncoder.encode(path, "UTF-8"));
	}
	
	static IntegrationTestResponse getContent(String path) throws Exception {
		return sendGetRequest2(getUriForContent(path));
	}
	
	void verifyPermissions(String path, String expectedPermissions) throws Exception {
		//TODO Remove once - 'Defect 18264: Creating new directory with 777 permissions doesn't work' fixed
		if (expectedPermissions.equals("777")) {
			expectedPermissions = "755";
		}
		assertEquals(expectedPermissions, getPermissions(path));
	}
	
	void verifyPermissionsInherited(String path) throws Exception {
		String parentPath = path.substring(0, path.lastIndexOf("/"));
		String parentPermissions = getPermissions(parentPath);
		verifyPermissions(path, parentPermissions);
	}
	
	private String getPermissions(String path) throws Exception {
		GetFileAttributesResponse response = getAttributes(path).getEntityAs(GetFileAttributesResponse.class);
		return response.getPermissionsNumeric();
		
	}
	
	static IntegrationTestResponse updateFileContent(String filePath, String content, String checksum) throws UnsupportedEncodingException, Exception {
		JSONObject body = new JSONObject();
		body.put("content", content);
		if (checksum != null) {
			body.put("checksum", checksum);
		}

		String uri = getUriForContent(filePath);
		return sendPutRequest(uri, body);
	}
	
	static String getUriForContent(String path) throws UnsupportedEncodingException {
		return USS_ROOT_ENDPOINT + "/" + URLEncoder.encode(path, "UTF-8") + "/content";
	}
}
