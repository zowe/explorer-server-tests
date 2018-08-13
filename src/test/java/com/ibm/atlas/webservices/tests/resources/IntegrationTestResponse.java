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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import com.ibm.atlas.utilities.JsonUtils;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class IntegrationTestResponse {

	private final HttpResponse response;

	public IntegrationTestResponse(HttpResponse response) {
		this.response = response;
	}

	public int getStatus() {
		return response.getStatusLine().getStatusCode();
	}

	public String getEntity() throws IOException {
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}
	
	public List<String> getEntityAsListOfStrings() throws IOException {
		List<String> stringList = new ArrayList<String>();
		return getEntityAs(stringList.getClass());
	}

	public JSONObject getEntityAsJson() throws IOException {
		return JSONObject.parse(getEntity());
	}
	
	public JSONArray getEntityAsJsonArray() throws IOException {
		return JSONArray.parse(getEntity());
	}

	public <T> T getEntityAs(Class<T> entityType) throws IOException {
		return JsonUtils.convertString(getEntity(), entityType);
	}

	public IntegrationTestResponse shouldHaveStatusOk() {
		return shouldHaveStatus(HttpStatus.SC_OK);
	}

	public IntegrationTestResponse shouldHaveStatusCreated() {
		return shouldHaveStatus(HttpStatus.SC_CREATED);
	}
	
	public IntegrationTestResponse shouldHaveStatusNoContent() {
		return shouldHaveStatus(HttpStatus.SC_NO_CONTENT);
	}

	public IntegrationTestResponse shouldHaveStatus(int expectedStatus) {
		assertEquals(expectedStatus, getStatus());
		return this;
	}

	public void shouldHaveLocationHeader(String expectedLocation) {
		assertEquals(expectedLocation, response.getLastHeader("Location").getValue());
	}
	
	public IntegrationTestResponse shouldHaveEntityContaining(String expectedEntity) throws IOException {
		String entity = getEntity();
		assertTrue(String.format("%s contains %s", entity, expectedEntity), entity.contains(expectedEntity));
		return this;
	}
	
	public IntegrationTestResponse shouldHaveEntityMatching(String pattern) throws IOException {
		String entity = getEntity();
		assertTrue(String.format("%s matches %s", entity, pattern), entity.matches(pattern));
		return this;
	}
	
	public IntegrationTestResponse shouldHaveEntity(Object expected) throws Exception {
		Object entity = getEntityAs(expected.getClass());
		assertEquals(expected, entity);
		return this;
	}
	
	
	
}
