package com.googlesource.gerrit.plugins.redirect;

import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.SkipProjectClone;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.acceptance.UseLocalDisk;
import java.io.IOException;
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
    name = "redirect-change-request",
    httpModule = "com.googlesource.gerrit.plugins.redirect.HttpModule")
public class RedirectChangeRequestFilterIT extends LightweightPluginDaemonTest {

  private final HttpClient httpClient = HttpClients.createMinimal();

  @Test
  public void shouldBe400WhenChangeNumberIsNotCorrect() throws IOException {
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + "12343/324234");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(
        response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws IOException {
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + "99999");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldBe301WhenProjectIsFound() throws IOException {
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + "789096");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FOUND);
    Assert.assertEquals(
        response.getFirstHeader("Location").getValue(),
        canonicalWebUrl.get() + "test-namespace/project1/+/789096");
  }
}
