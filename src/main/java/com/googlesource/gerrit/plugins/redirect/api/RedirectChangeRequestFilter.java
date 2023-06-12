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
  private final Pattern pattern = Pattern.compile("^/(\\d+)$");

  @Inject
  RedirectChangeRequestFilter(
      @CanonicalWebUrl String canonicalUrl, Map<Integer, String> changesProjectKeyValueStore) {
    this.canonicalUrl = canonicalUrl;
    this.changesProjectKeyValueStore = changesProjectKeyValueStore;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    boolean isEclipseGerritHubRequest =
        Optional.ofNullable(httpRequest.getHeader(X_FORWARDED_HOST_HTTP_HEADER))
            .filter(header -> header.equalsIgnoreCase(ECLIPSE_GERRITHUB_IO_HOST))
            .isPresent();

    if (isEclipseGerritHubRequest) {
      Optional<Integer> maybeChangeNumber = extractChangeNumberFromURI(httpRequest.getRequestURI());
      if (maybeChangeNumber.isPresent()) {
        Optional<String> maybeProject = findProjectNameByChangeNumber(maybeChangeNumber.get());
        if (maybeProject.isPresent()) {
          httpResponse.sendRedirect(buildRedirectURL(maybeProject.get(), maybeChangeNumber.get()));
        } else {
          httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
      } else {
        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  private Optional<Integer> extractChangeNumberFromURI(String uri) {
    Matcher matcher = pattern.matcher(uri);
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
