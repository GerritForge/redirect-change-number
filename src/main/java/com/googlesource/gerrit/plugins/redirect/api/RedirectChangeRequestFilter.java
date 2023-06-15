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

import com.google.gerrit.httpd.AllRequestFilter;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectChangeRequestFilter extends AllRequestFilter {
  static final String ECLIPSE_GERRITHUB_IO_HOST = "eclipse.gerrithub.io";
  static final String X_FORWARDED_HOST_HTTP_HEADER = "X-Forwarded-Host";
  private final Map<Integer, String> changesProjectKeyValueStore;
  private final String canonicalUrl;
  private final Pattern changeNumberUrlPattern = Pattern.compile("^/(\\d+)$");

  @Inject
  RedirectChangeRequestFilter(
      @CanonicalWebUrl String canonicalUrl, Map<Integer, String> changeProjectKeyValueStore) {
    this.canonicalUrl = canonicalUrl;
    this.changesProjectKeyValueStore = changeProjectKeyValueStore;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    Optional<String> maybeRedirectURL =
        Optional.ofNullable(httpRequest.getHeader(X_FORWARDED_HOST_HTTP_HEADER))
            .filter(header -> header.equalsIgnoreCase(ECLIPSE_GERRITHUB_IO_HOST))
            .flatMap(
                header ->
                    extractChangeNumberFromURI(httpRequest.getRequestURI())
                        .flatMap(
                            changeNumber ->
                                findProjectNameByChangeNumber(changeNumber)
                                    .map(
                                        projectName ->
                                            buildRedirectURL(projectName, changeNumber))));

    if (maybeRedirectURL.isPresent()) httpResponse.sendRedirect(maybeRedirectURL.get());
    else chain.doFilter(request, response);
  }

  private Optional<Integer> extractChangeNumberFromURI(String uri) {
    Matcher matcher = changeNumberUrlPattern.matcher(uri);
    if (matcher.find()) return Optional.of(Integer.parseInt(matcher.group(1)));
    else return Optional.empty();
  }

  private Optional<String> findProjectNameByChangeNumber(int changeNumber) {
    return Optional.ofNullable(changesProjectKeyValueStore.get(changeNumber));
  }

  private String buildRedirectURL(String projectName, int changeNumber) {
    return String.format("%sc/%s/+/%s", canonicalUrl, projectName, changeNumber);
  }
}
