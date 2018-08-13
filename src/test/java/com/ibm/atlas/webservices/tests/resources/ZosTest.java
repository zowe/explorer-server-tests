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
import org.junit.Test;

import com.ibm.atlas.webservice.tests.junit.AbstractHTTPComparisonTest;

public class ZosTest extends AbstractHTTPComparisonTest {


	@Test
	public void testGetUsername() {
		System.out.println("> testGetUsername()");
		String relativeURI				= "api/zos/username";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath	= "expectedResults/Zos/username.json";
		int expectedReturnCode			= HttpStatus.SC_OK;

		HashMap<String,String> substitutionVars = new HashMap<String,String>();
		substitutionVars.put("ATLAS.USERNAME", System.getProperty("atlas.username"));
		
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, substitutionVars, false);
	}
	
	@Test
	public void testGetParmlibInfo() {
		System.out.println("> testGetParmlibInfo()");

		String relativeURI				= "api/zos/parmlib";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Zos/parmlib_info_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}
	
	@Test
	public void testGetSysplexInfo() {
		System.out.println("> testGetSysplexInfo()");
		String relativeURI				= "api/zos/sysplex";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/Zos/sysplex_info_regex.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;

		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, null, true);
	}

}
