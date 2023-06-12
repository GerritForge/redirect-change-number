package com.googlesource.gerrit.plugins.redirect;

import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.SkipProjectClone;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.acceptance.UseLocalDisk;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SkipProjectClone
@UseLocalDisk
@TestPlugin(
    name = "redirect-change-request",
    httpModule = "com.googlesource.gerrit.plugins.redirect.HttpModule")
public class RedirectChangeRequestFilterIT extends LightweightPluginDaemonTest {

  private final HttpClient httpClient = HttpClients.createMinimal();

  @Test
  public void shouldBe400WhenChangeNumberIsNotCorrect() throws ServletException, IOException {
    HttpUriRequest request =  new HttpGet(canonicalWebUrl.get() + "12343/324234");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws ServletException, IOException {
    HttpUriRequest request =  new HttpGet(canonicalWebUrl.get() + "99999");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldBe301WhenProjectIsFound() throws ServletException, IOException {
    HttpUriRequest request =  new HttpGet(canonicalWebUrl.get() + "1234");
    request.addHeader("Referer", RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    HttpResponse response = httpClient.execute(request);
    Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_FOUND);
    Assert.assertEquals(response.getFirstHeader("Location").getValue(), canonicalWebUrl.get() + "example1/example2/+/1234");
  }
}
