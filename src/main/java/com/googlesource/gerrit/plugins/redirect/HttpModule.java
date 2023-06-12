package com.googlesource.gerrit.plugins.redirect;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import java.util.Map;

public class HttpModule extends ServletModule {

  public HttpModule() {
  }

  @Override
  protected void configureServlets() {
    MapBinder<Integer, String> mapBinder = MapBinder.newMapBinder(binder(), Integer.class, String.class);
    mapBinder.addBinding(1234).toInstance("example1/example2");

    DynamicSet.bind(binder(), AllRequestFilter.class)
        .to(RedirectChangeRequestFilter.class)
        .in(Scopes.SINGLETON);
  }
}
