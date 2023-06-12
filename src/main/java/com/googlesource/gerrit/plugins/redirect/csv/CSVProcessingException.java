package com.googlesource.gerrit.plugins.redirect.csv;

public final class CSVProcessingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public CSVProcessingException(String message, Throwable why) {
    super(message, why);
  }
}
