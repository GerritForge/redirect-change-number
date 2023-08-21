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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
public class RedirectChangeNumberFilterTest {
  @Mock private HttpServletRequest httpServletRequest;
  @Mock private HttpServletResponse httpServletResponse;
  @Mock private FilterChain filterChain;
  private final Map<Integer, String> changesProjectKeyValueStore = new HashMap<>();
  private final String sslPort = "443";
  private final String https = "https";
  private final RedirectChangeNumberFilter redirectChangeNumberFilter =
      new RedirectChangeNumberFilter(changesProjectKeyValueStore);

  @Test
  public void shouldGoNextInChainWhenNoXForwardedHeadersInRequest()
      throws ServletException, IOException {
    redirectChangeNumberFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER);
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldGoNextInChainWhenXForwardedHostHeaderIsNotEclipseGerrithubIo()
      throws ServletException, IOException {
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER))
        .thenReturn("some-subdomain.some-domain.io");
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER))
        .thenReturn(sslPort);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER))
        .thenReturn(https);
    redirectChangeNumberFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER);
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldGoNextInChainWhenWhenNoChangeNumberInURI()
      throws ServletException, IOException {
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER))
        .thenReturn(RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER))
        .thenReturn(sslPort);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER))
        .thenReturn(https);
    when(httpServletRequest.getRequestURI()).thenReturn("/ANY_STRING_VALUE");
    redirectChangeNumberFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER);
    verify(httpServletRequest).getRequestURI();
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldGoNextInChainWhenProjectIsNotFound() throws ServletException, IOException {
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER))
        .thenReturn(RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER))
        .thenReturn(sslPort);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER))
        .thenReturn(https);
    when(httpServletRequest.getRequestURI()).thenReturn("/7891");
    redirectChangeNumberFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER);
    verify(httpServletRequest).getRequestURI();
    verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
  }

  @Test
  public void shouldBe302WhenProjectIsFound() throws ServletException, IOException {
    String project = "Gerrit/project";
    Integer changeNumber = 7891;
    String redirectURL =
        https
            + "://"
            + RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST
            + ":"
            + sslPort
            + "/c/"
            + URLEncoder.encode(project, StandardCharsets.UTF_8.name())
            + "/+/"
            + changeNumber;
    changesProjectKeyValueStore.put(changeNumber, project);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER))
        .thenReturn(RedirectChangeNumberFilter.ECLIPSE_GERRITHUB_IO_HOST);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER))
        .thenReturn(sslPort);
    when(httpServletRequest.getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER))
        .thenReturn(https);
    when(httpServletRequest.getRequestURI()).thenReturn("/" + changeNumber);
    redirectChangeNumberFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_HOST_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PORT_HTTP_HEADER);
    verify(httpServletRequest).getHeader(RedirectChangeNumberFilter.X_FORWARDED_PROTO_HTTP_HEADER);
    verify(httpServletRequest).getRequestURI();
    verify(httpServletResponse).sendRedirect(redirectURL);
  }
}
