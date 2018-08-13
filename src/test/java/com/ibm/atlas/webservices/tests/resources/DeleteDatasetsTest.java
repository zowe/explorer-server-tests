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

public class DeleteDatasetsTest extends AbstractDatasetsIntegrationTest {
	
	@Test
	public void testDeleteSdsWorks() throws Exception {
		String tempPath = HLQ + ".TEMP";
		createSds(tempPath).shouldHaveStatusCreated();
		
		deleteDataset(tempPath).shouldHaveStatusNoContent();
	}
	
	@Test
	public void testDeletePdsWorks() throws Exception {
		String tempPath = HLQ + ".TEMP";
		createPds(tempPath).shouldHaveStatusCreated();
			
		deleteDataset(tempPath).shouldHaveStatusNoContent();
	}
	
	@Test
	public void testDeletePdsMemberWorks() throws Exception {
		String memberPath = getTestJclMemberPath("TEMP");
		createPdsMember(memberPath,"").shouldHaveStatusOk();
			
		deleteDataset(memberPath).shouldHaveStatusNoContent();
	}
	
	@Test @Ignore("Task 19604")
	public void testDeleteFileWithoutAccess() throws Exception {
		deleteDataset(UNAUTHORIZED_DATASET).shouldHaveStatus(HttpStatus.SC_FORBIDDEN);
	}
	
	@Test
	public void testDeleteDatasetsInvalidDataset() throws Exception {
		deleteDataset(INVALID_DATASET_NAME).shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
	}
}
