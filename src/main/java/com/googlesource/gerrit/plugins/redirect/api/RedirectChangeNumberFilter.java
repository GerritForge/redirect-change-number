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
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectChangeNumberFilter extends AllRequestFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedirectChangeNumberFilter.class);

  static final String ECLIPSE_GERRITHUB_IO_HOST = "eclipse.gerrithub.io";
  private final Map<Integer, String> changesProjectKeyValueStore;
  private final Pattern changeNumberUrlPattern = Pattern.compile("^/(\\d+)$");

  @Inject
  RedirectChangeNumberFilter(Map<Integer, String> changeProjectKeyValueStore) {
    this.changesProjectKeyValueStore = changeProjectKeyValueStore;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String requestURI = httpRequest.getRequestURI();

    try {
      Optional<String> maybeForwardedHost =
          Optional.ofNullable(httpRequest.getHeader(HttpHeaders.X_FORWARDED_HOST));
      Optional<Integer> maybeForwardedPort =
          Optional.ofNullable(httpRequest.getHeader(HttpHeaders.X_FORWARDED_PORT))
              .map(Integer::parseInt);
      Optional<String> maybeForwardedProto =
          Optional.ofNullable(httpRequest.getHeader(HttpHeaders.X_FORWARDED_PROTO));

      boolean areForwardedHeadersInRequest =
          maybeForwardedHost.isPresent()
              && maybeForwardedPort.isPresent()
              && maybeForwardedProto.isPresent();

      Supplier<Optional<Integer>> maybeChangeNumber = () -> extractChangeNumberFromURI(requestURI);

      if (areForwardedHeadersInRequest
          && maybeForwardedHost.get().equalsIgnoreCase(ECLIPSE_GERRITHUB_IO_HOST)) {
        Optional<String> maybeRedirectURL =
            maybeChangeNumber
                .get()
                .flatMap(changeNumber -> findProjectNameByChangeNumber(changeNumber))
                .map(
                    projectName ->
                        buildRedirectURL(
                            maybeForwardedProto.get(),
                            maybeForwardedHost.get(),
                            maybeForwardedPort.get(),
                            projectName,
                            maybeChangeNumber.get().get()));

        if (maybeRedirectURL.isPresent()) {
          httpResponse.sendRedirect(maybeRedirectURL.get());
          return;
        }
      }

      if (!areForwardedHeadersInRequest && maybeChangeNumber.get().isPresent()) {
        LOGGER.warn(
            "Some X-Forwarded Headers are not present in the request:"
                + httpRequest.getRequestURL());
      }

      chain.doFilter(request, response);

    } catch (IllegalStateException ex) {
      LOGGER.error("Error processing url: " + httpRequest.getRequestURL(), ex);
      httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private Optional<Integer> extractChangeNumberFromURI(String uri) {
    Matcher matcher = changeNumberUrlPattern.matcher(uri);
    if (matcher.find()) {
      return Optional.of(Integer.parseInt(matcher.group(1)));
    } else {
      return Optional.empty();
    }
  }

  private Optional<String> findProjectNameByChangeNumber(int changeNumber) {
    return Optional.ofNullable(changesProjectKeyValueStore.get(changeNumber));
  }

  private String buildRedirectURL(
      String protocol, String host, int port, String projectName, int changeNumber) {
    try {
      return new URL(
              protocol,
              host,
              port,
              String.format(
                  "/c/%s/+/%s",
                  URLEncoder.encode(projectName, StandardCharsets.UTF_8.name()), changeNumber))
          .toString();
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("No UTF-8 support", e);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Error building redirection url", e);
    }
  }
}
