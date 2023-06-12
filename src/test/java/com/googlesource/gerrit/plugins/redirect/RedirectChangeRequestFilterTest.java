package com.googlesource.gerrit.plugins.redirect;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedirectChangeRequestFilterTest {
  @Mock private HttpServletRequest httpServletRequest;
  @Mock private HttpServletResponse httpServletResponse;
  @Mock private FilterChain filterChain;
  private final Map<Integer, String > changesProjectKeyValueStore = new HashMap<>();
  private final RedirectChangeRequestFilter redirectChangeRequestFilter = new RedirectChangeRequestFilter(changesProjectKeyValueStore);

  @Test
  public void shouldGoNextInChainWhenRefererHeaderIsNotPresent() throws ServletException, IOException {
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldGoNextInChainWhenRefererHeaderContainsNotExpectedValue() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer")).thenReturn("NOT_EXPECTED_VALUE");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldBe400WhenChangeNumberIsNotCorrect() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer")).thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("ANY_STRING_VALUE");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer")).thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("7891");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldBe301WhenProjectIsFound() throws ServletException, IOException {
    String project = "SOME_PROJECT";
    Integer changeNumber = 7891;
    String redirectURL = RedirectChangeRequestFilter.GERRIT_HUB_URL + "/" + project + "+" + changeNumber;
    changesProjectKeyValueStore.put(changeNumber, project);
    when(httpServletRequest.getHeader("Referer")).thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn(changeNumber.toString());
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendRedirect(redirectURL);
  }
}
