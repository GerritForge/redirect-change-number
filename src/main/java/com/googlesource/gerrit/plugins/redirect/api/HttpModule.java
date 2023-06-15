package com.googlesource.gerrit.plugins.redirect.api;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.googlesource.gerrit.plugins.redirect.csv.ChangeProjectCSVLoader;
import java.util.Map;

public class HttpModule extends ServletModule {
  private final String CHANGE_PROJECT_CSV = "change_project.csv";

  public HttpModule() {}

  @Override
  protected void configureServlets() {
    Map<Integer, String> changeProject = ChangeProjectCSVLoader.loadCSV(CHANGE_PROJECT_CSV);
    bind(new TypeLiteral<Map<Integer, String>>() {}).toInstance(changeProject);
    DynamicSet.bind(binder(), AllRequestFilter.class)
        .to(RedirectChangeRequestFilter.class)
        .in(Scopes.SINGLETON);
  }
}
