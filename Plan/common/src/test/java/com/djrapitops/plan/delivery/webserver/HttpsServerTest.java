/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.exceptions.connection.WebException;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import utilities.HTTPConnector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertTrue;

interface HttpsServerTest {

    HTTPConnector connector = new HTTPConnector();

    WebServer getWebServer();

    int testPortNumber();

    @Test
    default void webServerIsRunningHTTPS() {
        assertTrue(getWebServer().isUsingHTTPS(), "WebServer is not using https");
    }

    /**
     * Test case against "Perm level 0 required, got 0".
     */
    @Test
    default void userCanLogIn() throws Exception {
        webServerIsRunningHTTPS();

        String address = "https://localhost:" + testPortNumber();

        String cookie = login(address);
        testAccess(address, cookie);
    }

    default void testAccess(String address, String cookie) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = connector.getConnection("GET", address);
            connection.setRequestProperty("Cookie", cookie);

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                case 302:
                    return;
                case 400:
                    throw new BadRequestException("Bad Request: " + address);
                case 403:
                    throw new ForbiddenException(address + " returned 403");
                case 404:
                    throw new NotFoundException(address + " returned a 404, ensure that your server is connected to an up to date Plan server.");
                case 500:
                    throw new IllegalStateException(); // Not supported
                default:
                    throw new WebException(address + "| Wrong response code " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    default String login(String address) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection loginConnection = null;
        String cookie = "";
        try {
            loginConnection = connector.getConnection("GET", address + "/auth/login?user=test&password=testPass");
            try (InputStream in = loginConnection.getInputStream()) {
                String responseBody = new String(IOUtils.toByteArray(in));
                assertTrue(responseBody.contains("\"success\":true"), () -> "Not successful: " + responseBody);
                cookie = loginConnection.getHeaderField("Set-Cookie").split(";")[0];
                System.out.println("Got cookie: " + cookie);
            }
        } finally {
            loginConnection.disconnect();
        }
        return cookie;
    }
}