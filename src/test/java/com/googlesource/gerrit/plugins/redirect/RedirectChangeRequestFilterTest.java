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
  private final Map<String, List<Integer>> changesPerProject = new HashMap<>();
  private final RedirectChangeRequestFilter redirectChangeRequestFilter = new RedirectChangeRequestFilter(changesPerProject);

  /*
  Request `https://review.gerrithub.io/7940` is received by the filter:

if the header `Referer` is `ECLIPSE FOUNDATION`
      then
         if the url format is correct and change number  is extracted i.e `7940`
         then
           If the change `7940` is linked to a project i.e PROJECT_X
           then redirect to https://review.gerrithub.io/c/GerritForge/PROJECT_X/+/7940
           else send back NOT_FOUND
         else send back NOT_FOUND
      else go to the next in the chain

   */

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

  @Ignore
  public void shouldBe404WhenChangeNumberIsNotCorrect() { // TODO first I need to know how the format of the URL
  }

  @Test
  public void shouldBe404WhenProjectIsNotFound() throws ServletException, IOException {
    when(httpServletRequest.getHeader("Referer")).thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn("7891");
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
  }

  @Test
  public void shouldBe301WhenProjectIsFound() throws ServletException, IOException {
    String project = "SOME_PROJECT";
    Integer changeNumber = 7891;
    List<Integer> list1 = new ArrayList<>();
    list1.add(changeNumber);
    String redirectURL = RedirectChangeRequestFilter.GERRIT_HUB_URL + "/" + project + "+" + changeNumber;
    changesPerProject.put(project, list1);
    when(httpServletRequest.getHeader("Referer")).thenReturn(RedirectChangeRequestFilter.ECLIPSE_PROJECT_HOST);
    when(httpServletRequest.getRequestURI()).thenReturn(changeNumber.toString());
    redirectChangeRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader("Referer");
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendRedirect(redirectURL);
  }
}
