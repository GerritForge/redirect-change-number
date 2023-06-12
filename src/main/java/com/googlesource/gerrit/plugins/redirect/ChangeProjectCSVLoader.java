package com.googlesource.gerrit.plugins.redirect;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeProjectCSVLoader {
  private static final Logger LOG = LoggerFactory.getLogger(ChangeProjectCSVLoader.class);

  public static Map<Integer, String> loadCSV(String filePath) {
    Map<Integer, String> dataMap = new HashMap<>();

    try (InputStream csvInputStream =
            ChangeProjectCSVLoader.class.getClassLoader().getResourceAsStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(csvInputStream))) {
      String line;
      boolean isFirstLine = true;
      while ((line = br.readLine()) != null) {
        if (isFirstLine) {
          isFirstLine = false;
          continue; // Skip the header line
        }

        String[] columns = line.split(",");
        if (columns.length == 2) {
          int changeNumber = Integer.parseInt(columns[0]);
          String project = columns[1];
          dataMap.put(changeNumber, project);
        }
      }
    } catch (IOException e) {
      LOG.error(String.format("Error processing CSV file: %s", filePath), e);
    }

    return dataMap;
  }
}
