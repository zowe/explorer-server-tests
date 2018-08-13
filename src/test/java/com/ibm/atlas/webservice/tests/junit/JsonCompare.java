/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */

package com.ibm.atlas.webservice.tests.junit;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONArtifact;
import com.ibm.json.java.JSONObject;

public class JsonCompare {
	 
	public static final boolean STRICT_ORDER = true;
	public static final boolean LAX_ORDER = false;
	
	public static final boolean SUBSET_EQUAL = true;
	public static final boolean STRICT_EQUAL = false;
	
	
	public static String compare(JSONArtifact expectedResult, JSONArtifact actualResult, Map<String,String> substitutionVars, boolean treatExpectedValueAsRegex, boolean useStrictArrayOrderCompare) {
	  String result = null;
	  result = compareJsonArtifacts(expectedResult, actualResult);
	  if (null!=result) return result;
	  if (expectedResult instanceof JSONArray) result = compareJsonArrays((JSONArray)expectedResult, (JSONArray)actualResult, substitutionVars, treatExpectedValueAsRegex, useStrictArrayOrderCompare);
	  else result = compareJsonObjects((JSONObject)expectedResult, (JSONObject)actualResult, substitutionVars, treatExpectedValueAsRegex, useStrictArrayOrderCompare);
	  
	  return result;
	}
	
	private static String compareValues (String baseLocation, Object expectedResultValue, Object actualResultValue, Map<String,String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
		if(expectedResultValue instanceof JSONObject ) {
			if (!(actualResultValue instanceof JSONObject)) {
				return "JSON types don't match: " + baseLocation + ". Value: \"" + expectedResultValue + "\". Expected Result object is JSONObject but Actual Result is \"" + actualResultValue.getClass().getSimpleName() + "\"";
			} else {
				return compareJsonObjects((JSONObject)expectedResultValue, (JSONObject)actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
			}
		} else if (expectedResultValue instanceof JSONArray ) {
			if (!(actualResultValue instanceof JSONArray)) {
				return "JSON types don't match: " + baseLocation +". Value: \"" + expectedResultValue + "\". Expected Result object is JSONArray but Actual Result is \"" + actualResultValue.getClass().getSimpleName() +"\"";
			} else {
				return compareJsonArrays((JSONArray)expectedResultValue, (JSONArray)actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
			}
		} else if (expectedResultValue instanceof Object ) {
			if (!(actualResultValue instanceof Object)) {
				return "JSON types don't match: " + baseLocation + ". Value: \"" + expectedResultValue + "\". Expected Result object is Object but Actual Result is \"" + actualResultValue.getClass().getSimpleName() + "\"" ;
				
			} else if (treatExpectedValueAsRegex && expectedResultValue instanceof String && actualResultValue instanceof String) {
				if(expectedResultValue.equals(actualResultValue)){
					return null;
				}
				//treat the expectedResultValue as a regex and see if the actual result matches or not 
				Pattern pattern = Pattern.compile((String)expectedResultValue, Pattern.MULTILINE | Pattern.DOTALL);
				Matcher matcher = pattern.matcher((String)actualResultValue);
				
				return matcher.matches()  ? null : "JSON objects not equal: " + baseLocation + ". Expected Result value is: \"" + expectedResultValue + "\" but Actual Result is: \"" + actualResultValue + "\"" ;

			} else {
				return (expectedResultValue).equals(actualResultValue) ? null : "JSON objects not equal: " + baseLocation + ". Expected Result value is: \"" + expectedResultValue + "\" but Actual Result is: \"" + actualResultValue + "\"" ;
			}
		} else if (expectedResultValue ==null ) {
			return actualResultValue==null ? null : "JSON objects not equal: " + baseLocation + ". Expected Result value is null but Actual Result is: \"" + actualResultValue + "\"" ;
		} else {
			return "Expected Result file JSONArtifact contains invalid object for key: " + baseLocation + ". \nObject class is: " + expectedResultValue.getClass().getSimpleName() + "\"" ;
		}
	}
	
	
	public static String compareJsonArrays (JSONArray expectedResult, JSONArray actualResult, Map<String,String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
		int i = 0;
		

		//if JSON arrays are allowed to be unordered, just sort them and let the regular iterator compare handle it 
		if (allowUnorderedArrays) {
			Comparator<Object> c = new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					if (o1 == o2) {return 0;}
					if (o1 == null) {return -1;}
					if (o2 == null) {return 1;}
					return o1.toString().compareTo(o2.toString());
				}
			};

			expectedResult.sort(c);
			actualResult.sort(c);
		}


		for (Object expectedResultValue : expectedResult) {
			if (actualResult.size() != expectedResult.size() || i >= actualResult.size()) {
				try {
					return "JSON Arrays are unequal size.  \nExpected Result array: " + expectedResult.serialize() + "\n  Actual Result array: " + actualResult.serialize();
				} catch (IOException e) {
					e.printStackTrace();
					return "Error in array processing";
				}
			}
			Object actualResultValue = actualResult.get(i);
			String result = null;
			result = compareValues(" JSONArray index=\"" + String.valueOf(i) +"\"", expectedResultValue, actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
			if (result!=null) return result;
			i++;
		}
		return null;		
	}
	
	
	@SuppressWarnings("unchecked")
	public static String compareJsonObjects(JSONObject expectedResult, JSONObject actualResult, Map<String,String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
		int bSize = expectedResult.size();
		int tSize = actualResult.size();
		boolean subset = false;
		if (!subset) {
			if (bSize != tSize) {
				String msg = "JSON objects are not the same size. Expected Result File is \"" + bSize + "\" but Actual Result is \"" + tSize + "\"";
				return msg;
			}
		}

		for (Map.Entry<String, Object> j : (Set<Map.Entry<String, Object>>) expectedResult.entrySet()) {
			String expectedResultKey = j.getKey();
			Object expectedResultValue = j.getValue();

			if (!actualResult.containsKey(expectedResultKey)) {
				return "JSON objects not equal. Expected Result File contains key/value: \""
						+ expectedResultKey + "\" : \"" + expectedResultValue + "\"." 
						+ " This key is missing from the Actual Result";
			}

			// Replace test-specific substitution variables in the expected result file with supplied values
			if (substitutionVars != null && expectedResultValue instanceof String) {
				for (Map.Entry<String, String> entry : substitutionVars.entrySet()) {
					expectedResultValue = String.valueOf(expectedResultValue).replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
				}
			}

			Object actualResultValue = actualResult.get(expectedResultKey);
			String result = compareValues(" JSONObject key= \"" + expectedResultKey + "\"", expectedResultValue, actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
			return result;
		}
		return null;
	}
	
	
	public static String compareJsonArtifacts(JSONArtifact expectedResult, JSONArtifact actualResult) {
	  if (expectedResult instanceof JSONArray && actualResult instanceof JSONArray) return null;
	  else if (expectedResult instanceof JSONObject && actualResult instanceof JSONObject) return null;
	  else return "JSONArtifacts are not of the same type.  " + 
	  "Expected Result is " + (expectedResult == null ? "null" : expectedResult.getClass().getSimpleName()) + 
	  ". Actual Result is " + (actualResult == null ? "null" : actualResult.getClass().getSimpleName());
	}
}
