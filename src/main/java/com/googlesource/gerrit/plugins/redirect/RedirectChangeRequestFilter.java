package com.googlesource.gerrit.plugins.redirect;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RedirectChangeRequestFilter implements Filter {
  static final String ECLIPSE_PROJECT_HOST = "git.eclipse.org";
  static final String GERRIT_HUB_URL = "https://review.gerrithub.io"; // TODO injected ?

  private final Map<String, List<Integer>> changesPerProject;
  private static final String JGIT_PROJECT = "GerritForge/jgit";

  RedirectChangeRequestFilter(Map<String, List<Integer>> changesPerProject) {
    this.changesPerProject  = changesPerProject;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization code goes here, if needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String referer = httpRequest.getHeader("Referer"); // TODO use Optional
    if (referer != null && referer.contains(ECLIPSE_PROJECT_HOST)) {
      String requestUrl = httpRequest.getRequestURI();
      String changeNumber = extractChangeNumberFromURL(requestUrl);
      try {
        int cn = Integer.parseInt(changeNumber);
        Optional<String> project = findProjectByChangeNumber(cn);
        if (project.isPresent()) {
          String redirectUrl =
              GERRIT_HUB_URL + "/" + project.get() + "+" + changeNumber; // TODO Build properly
          httpResponse.sendRedirect(redirectUrl);
        } else {
          httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
      } catch (NumberFormatException e) {
        // If the last element is not an integer, continue with the normal flow
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } else {
      // Continue with the normal flow
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // Cleanup code goes here, if needed
  }

  private String extractChangeNumberFromURL(String url) {
    String[] urlParts = url.split("/"); // TODO regular expression to guarantee the well format
    int lastIndex = urlParts.length - 1;
    return urlParts[lastIndex];
  }

  private Optional<String> findProjectByChangeNumber(int changeNumber) {
    for (Map.Entry<String, List<Integer>> entry : changesPerProject.entrySet()) {
      List<Integer> integers = entry.getValue();
      if (integers.contains(changeNumber)) {
        return Optional.of(entry.getKey());
      }
    }
    return Optional.empty();
  }
}

