package com.googlesource.gerrit.plugins.redirect;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RedirectChangeRequestFilterTest {
  @Mock private HttpServletRequest httpServletRequest;
  @Mock private HttpServletResponse httpServletResponse;
  @Mock private FilterChain filterChain;
  private final Map<Integer, String> changesProjectKeyValueStore = new HashMap<>();

  private final String canonicalUrl = "https://test.com/";

  private final RedirectChangeRequestFilter redirectChangeRequestFilter =
      new RedirectChangeRequestFilter(canonicalUrl, changesProjectKeyValueStore);

  @Test
  public void shouldGoNextInChainWhenRefererHeaderIsNotPresent()
      throws ServletException, IOException {
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldGoNextInChainWhenRefererHeaderContainsNotExpectedValue()
      throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer")).thenReturn("NOT_EXPECTED_VALUE");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldBe400WhenChangeNumberIsNotCorrect() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer"))
        .thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("/ANY_STRING_VALUE");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer"))
        .thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("/7891");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldBe302WhenProjectIsFound() throws ServletException, IOException {
    String project = "SOME_PROJECT";
    Integer changeNumber = 7891;
    String redirectURL = canonicalUrl + project + "/+/" + changeNumber;
    changesProjectKeyValueStore.put(changeNumber, project);
    when(httpServletRequest.getHeader("Referer"))
        .thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("/" + changeNumber);
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendRedirect(redirectURL);
  }
}
