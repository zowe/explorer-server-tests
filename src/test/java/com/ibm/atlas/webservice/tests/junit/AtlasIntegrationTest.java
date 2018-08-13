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

import static org.junit.Assert.*;

import java.util.Date;

public class AtlasIntegrationTest {

	public static void assertDateIsRecent(Date date) {
		assertDateWithinRange(date, 90);
	}

	protected static void assertDateWithinRange(Date date, int pastDateInSeconds) {
		Date now = new Date();
		// Add 10 seconds to "now" because Date comparisons are strict < instead of <=
		Date nowPlus10seconds = new Date(now.getTime() + (15 * 1000));
		Date pastDate = new Date(now.getTime() - (pastDateInSeconds * 1000));
		String errorMessage = String.join("", "Date was ", date.toString(), " should be between ", pastDate.toString(), " and ", nowPlus10seconds.toString());
		boolean isRecentDate = date.after(pastDate) && date.before(nowPlus10seconds);
		assertTrue(errorMessage, isRecentDate);
	}

}
