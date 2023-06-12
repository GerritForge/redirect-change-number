package com.googlesource.gerrit.plugins.redirect;

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
  static final String ECLIPSE_PROJECT_HOST = "git.eclipse.org";

  private static final String REFERER_HTTP_HEADER = "REFERER";
  private final Map<Integer, String> changesProjectKeyValueStore;
  private final String canonicalUrl;
  private Pattern pattern = Pattern.compile("^/(\\d+)$");

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
    Optional<String> maybeEclipseProjectAsReferer =
        Optional.ofNullable(httpRequest.getHeader(REFERER_HTTP_HEADER))
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

  private Optional<Integer> extractChangeNumberFromURI(String uri) {
    Matcher matcher = pattern.matcher(uri);
    if (matcher.find()) return Optional.of(Integer.parseInt(matcher.group(1)));
    else return Optional.empty();
  }

  private Optional<String> findProjectByChangeNumber(int changeNumber) {
    return Optional.ofNullable(changesProjectKeyValueStore.get(changeNumber));
  }

  private String buildRedirectURL(String project, int change) {
    return canonicalUrl + project + "/+/" + change;
  }
}
