package com.googlesource.gerrit.plugins.redirect;

import com.google.gerrit.common.Nullable;
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class RedirectChangeRequestFilter extends AllRequestFilter {
  static final String ECLIPSE_PROJECT_HOST = "git.eclipse.org";
  private static final String GERRIT_HUB_IO_URL_FALLBACK = "https://review.gerrithub.io";
  private final Map<Integer, String> changesProjectKeyValueStore;
  private final String canonicalUrl;

  private Pattern pattern = Pattern.compile("^/(\\d+)$");

  @Inject
  RedirectChangeRequestFilter(@Nullable @CanonicalWebUrl String canonicalUrl, Map<Integer, String> changesProjectKeyValueStore) {
    this.canonicalUrl = Optional.ofNullable(canonicalUrl).orElse(GERRIT_HUB_IO_URL_FALLBACK);
    this.changesProjectKeyValueStore = changesProjectKeyValueStore;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    Optional<String> maybeEclipseProjectAsReferer =
        Optional
            .ofNullable(httpRequest.getHeader("Referer"))
            .filter(referer -> referer.equals(ECLIPSE_PROJECT_HOST));
    if (maybeEclipseProjectAsReferer.isPresent()) {
      Optional<Integer> maybeChangeNumber = extractChangeNumberFromURI(httpRequest.getRequestURI());
      if (maybeChangeNumber.isPresent()) {
        Optional<String> maybeProject = findProjectByChangeNumber(maybeChangeNumber.get());
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

  private Optional<Integer> extractChangeNumberFromURI(String uri) { // TODO MAYBE USE REGULAR EXPRESSION
    try {

      return Optional.of(Integer.parseInt(uri.substring(1)));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  private Optional<String> findProjectByChangeNumber(int changeNumber) {
    return Optional.ofNullable(changesProjectKeyValueStore.get(changeNumber));
  }

  private String buildRedirectURL(String project, int change) {
    return canonicalUrl + project + "/+/" + change;
  }
}

