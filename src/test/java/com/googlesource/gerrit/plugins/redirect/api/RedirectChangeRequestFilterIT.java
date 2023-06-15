package com.googlesource.gerrit.plugins.redirect.api;

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
    httpModule = "com.googlesource.gerrit.plugins.redirect.api.HttpModule")
public class RedirectChangeRequestFilterIT extends LightweightPluginDaemonTest {

  private final HttpClient httpClient = HttpClients.createMinimal();

  @Test
  public void shouldBe400WhenChangeNumberIsNotCorrect() throws IOException {
    String changeWithBadFormat = "12343/324234";
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + changeWithBadFormat);
    request.addHeader(
        RedirectChangeRequestFilter.X_FORWARDED_HOST_HTTP_HEADER,
        RedirectChangeRequestFilter.ECLIPSE_GERRITHUB_IO_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(
        response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws IOException {
    String changeWithNoProject = "99999";
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + changeWithNoProject);
    request.addHeader(
        RedirectChangeRequestFilter.X_FORWARDED_HOST_HTTP_HEADER,
        RedirectChangeRequestFilter.ECLIPSE_GERRITHUB_IO_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldBe302WhenProjectIsFound() throws IOException {
    String changeWithProject = "789096";
    HttpUriRequest request = new HttpGet(canonicalWebUrl.get() + changeWithProject);
    request.addHeader(
        RedirectChangeRequestFilter.X_FORWARDED_HOST_HTTP_HEADER,
        RedirectChangeRequestFilter.ECLIPSE_GERRITHUB_IO_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FOUND);
    Assert.assertEquals(
        response.getFirstHeader("Location").getValue(),
        canonicalWebUrl.get() + "c/test-namespace/project1/+/789096");
  }
}
