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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

public class FirewallAuthentication {

	
	//if required, authenticate through a firewall before running tests
	// these are set by -D defines from the Jenkins job or on the VM Arguments of the Eclipse launch configuration 
	protected static String firewallURI				= System.getProperty("firewall.baseURI");
	protected static String firewallUserName		= System.getProperty("firewall.username");
	protected static String firewallUserPassword	= System.getProperty("firewall.userpassword");
	
	protected static boolean authenticationAttempted	= false;
	protected static boolean authenticationFailed		= false;
	
	protected static boolean logging					= false;
	
	public static void authenticate() {

		try {
			if (firewallURI == null || firewallURI.isEmpty())
				return;		//do not attempt firewall authentication if none is specified
			
			if (authenticationFailed)
				Assert.fail("Firewall authentication has previously failed, aborting attempting test run");
			
			if (authenticationAttempted)
				return;
			
			//We don't need to authenticate if we're building in hursley and running against hursley
			if (System.getProperty("atlas.baseURI").contains("hursley"))
				return;
			
			System.out.println("Attempting authentication through ADL firewall");
			
			//allow self-signed certificates and allow server hostnames that do not match the hostname in the certificate
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
			HttpClientContext localContext = new HttpClientContext();

			//weirdly enough, a "connection refused" exception means that you are already authenticated, or that the authentication process was successful
			try {
				
				//send a GET request to the gateway URL
				HttpGet requestGET = new HttpGet();
				requestGET.setURI(new URI(firewallURI));
				if (logging) {System.out.println("Sending GET request to " + firewallURI);}
				CloseableHttpResponse response = (CloseableHttpResponse)httpClient.execute(requestGET, localContext);
				
				try {
					int rc = response.getStatusLine().getStatusCode();
					if (logging) {System.out.println("response code: " + rc);}
					Assert.assertTrue("Firewall response was " + rc + ", expected 200", rc == 200);
					EntityUtils.consume(response.getEntity());
				} finally {
					response.close();
				}

				//the gateway redirects to another URL, which contains a dynamically generated SID and a new URL base to be used for the subsequent login POSTs  
				String redirectBaseURL = null;
				String sid = null;
				URI redirectUrl = requestGET.getURI();
				RedirectLocations locations = (RedirectLocations) localContext.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
				if (locations != null && !locations.isEmpty()) {
					redirectUrl = locations.getAll().get(locations.getAll().size() - 1);

					//the redirect URL should be something like https://9.190.127.200:443/netaccess/redirect.html?sid=2160602980
					String redirectUrlString = redirectUrl.toString();
					if (logging) {System.out.println("redirected URL: " + redirectUrlString);}
					Assert.assertTrue("Firewall URL redirection was " + redirectUrlString + ", expected redirect?sid=", redirectUrlString.contains("redirect.html?sid="));

					//split the redirected URL to get the base redirected URL to be used for subsequent POSTs, and the dynamically generated SID
					String redirectParts[] = redirectUrlString.split("redirect.html\\?sid=");
					if (redirectParts != null && redirectParts.length == 2) {
						redirectBaseURL = redirectParts[0];
						sid =  redirectParts[1];
						if (logging) {System.out.println("redirectBase: " + redirectBaseURL);}
						if (logging) {System.out.println("sid: " + sid);}
					} else {
						Assert.fail("Could not obtain the redirection or SID");
					}
				}




				//clicking on the redirect.html's login button takes you to the connstatus.html page, which defines the form data (username, password, sid) for the login POST
				//this step is not optional; you cannot skip directly to the loginuser.html POST request
				HttpPost requestPOST1 = new HttpPost();
				requestPOST1.setURI(new URI(redirectBaseURL+"connstatus.html"));
				List<NameValuePair> formData1 = new ArrayList <NameValuePair>();
				formData1.add(new BasicNameValuePair("login", "Log In Now"));
				formData1.add(new BasicNameValuePair("sid", sid));
				requestPOST1.setEntity(new UrlEncodedFormEntity(formData1, "UTF-8"));
				if (logging) {System.out.println("Sending POST request to " + redirectBaseURL+"connstatus.html");}
				response = (CloseableHttpResponse)httpClient.execute(requestPOST1, localContext);
				
				try {
					int rc = response.getStatusLine().getStatusCode();
					if (logging) {System.out.println("response code: " + rc);}
					if (logging) {System.out.println(EntityUtils.toString( response.getEntity(), "UTF-8" ));}
					EntityUtils.consume(response.getEntity());
				} finally {
					response.close();
				}
				
				
				//now POST to the login.html page using the login credentials
				if (logging) {System.out.println("Sending POST request to " + redirectBaseURL+"loginuser.html");}
				HttpPost requestPOST2 = new HttpPost();
				requestPOST2.setURI(new URI(redirectBaseURL+"loginuser.html"));
				requestPOST2.addHeader("Referer", redirectBaseURL+"connstatus.html");
				List<NameValuePair> formData2 = new ArrayList <NameValuePair>();
				formData2.add(new BasicNameValuePair("Login", "Continue"));	//required, otherwise no login occurs
				formData2.add(new BasicNameValuePair("username", firewallUserName));
				formData2.add(new BasicNameValuePair("password", firewallUserPassword));
				formData2.add(new BasicNameValuePair("sid", sid));
				requestPOST2.setEntity(new UrlEncodedFormEntity(formData2, "UTF-8"));
				response = (CloseableHttpResponse)httpClient.execute(requestPOST2, localContext);

				try {
					int rc = response.getStatusLine().getStatusCode();
					if (logging) {System.out.println("response code: " + rc);}
					if (logging) {System.out.println(EntityUtils.toString( response.getEntity(), "UTF-8" ));}
					EntityUtils.consume(response.getEntity());
				} finally {
					response.close();
				}



				//if successful, the login page redirects back to the gateway URL and issues a ConnectionException.  Failure to login will redirect back to connstatus.html?
				redirectUrl = requestPOST2.getURI();
				locations = (RedirectLocations) localContext.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
				if (locations != null && !locations.isEmpty()) {
					redirectUrl = locations.getAll().get(locations.getAll().size() - 1);
					String redirectUrlString = redirectUrl.toString();
					if (logging) {System.out.println("redirected URL: " + redirectUrlString);}
					Assert.assertTrue("Firewall URL redirection was " + redirectUrlString + ", expected redirect?sid=", redirectUrlString.contains("redirect.html?sid="));
				} else {
					if (logging) {System.out.println("URL not redirected: " + redirectUrl);}
				}

			} catch (HttpHostConnectException hhce) {
				authenticationAttempted = true;
				System.out.println("ADL firewall authentication success!");
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		authenticationFailed = true;
		Assert.fail("ERROR AUTHENTICATING THROUGH ADL FIREWALL");
	}

}
