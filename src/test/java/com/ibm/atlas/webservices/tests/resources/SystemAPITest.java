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

public class SystemAPITest extends AbstractHTTPComparisonTest {
	
	@Test
	public void testVersionNumber() {
		System.out.println("> testGetSystemVersion()");
		String version = System.getProperty("atlas.pom");
		System.out.println("version "+version);
		String formatted = version!=null ? reformat(version) : "V1.0.0.0";
		String relativeURI				= "system/version";
		String httpMethodType			= HttpGet.METHOD_NAME;
		String expectedResultFilePath 	= "expectedResults/System/version.txt";
		int expectedReturnCode			= HttpStatus.SC_OK;
		
		HashMap<String,String> substitutionVars = new HashMap<String,String>();
		substitutionVars.put("ATLVER", formatted);
		runAndVerifyHTTPRequest(relativeURI, httpMethodType, expectedResultFilePath, expectedReturnCode, substitutionVars, false);
	}

	private String reformat(String version) {
		String ref = version.substring(0, version.indexOf("-SNAPSHOT"));
		String testref = 'V'+ref.substring(0, ref.lastIndexOf('.')+1);
		String backref = ref.substring( ref.lastIndexOf('.')+1);
		if (ref.startsWith("0.0.")) {
			return "V "+ref;
		}
		ref = ref.substring(0, ref.lastIndexOf('.'));
		System.out.println(testref);
		switch (backref.length()) {
			case 4:
				return testref+backref.substring(0,2)+(backref.charAt(2)==0?('.'+backref.charAt(4)):('.'+backref.substring(2))) ;
			case 3:
				return testref+backref.charAt(0)+(backref.charAt(1)==0?('.'+backref.charAt(3)):('.'+backref.substring(2))) ;
			default :
				return testref+"0."+backref; 
		}
	}
}
