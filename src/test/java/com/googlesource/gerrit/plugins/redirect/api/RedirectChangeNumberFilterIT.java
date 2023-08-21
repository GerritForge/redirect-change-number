// Copyright (C) 2023 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.redirect.api;

import com.google.common.net.HttpHeaders;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.SkipProjectClone;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.acceptance.UseLocalDisk;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;

@SkipProjectClone
@UseLocalDisk
@TestPlugin(
    name = "redirect-change-number",
    httpModule = "com.googlesource.gerrit.plugins.redirect.api.HttpModule")
public class RedirectChangeNumberFilterIT extends LightweightPluginDaemonTest {
  private final HttpClient httpClient = HttpClients.createMinimal();
  private final String sslPort = "443";
  private final String https = "https";

  @Test
  public void shouldBe302WhenProjectIsFound() throws IOException {
    String changeNumber = "789096";
    String projectName = "test-namespace/project1";
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + changeNumber);
    request.addHeader(
        HttpHeaders.X_FORWARDED_HOST, RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST);
    request.addHeader(HttpHeaders.X_FORWARDED_PROTO, "https");
    request.addHeader(HttpHeaders.X_FORWARDED_PORT, "443");
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FOUND);
    String locationHeaderExpected =
        https
            + "://"
            + RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST
            + ":"
            + sslPort
            + "/c/"
            + URLEncoder.encode(projectName, StandardCharsets.UTF_8.name())
            + "/+/"
            + changeNumber;
    Assert.assertEquals(response.getFirstHeader("Location").getValue(), locationHeaderExpected);
  }
}
