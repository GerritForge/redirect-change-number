package com.googlesource.gerrit.plugins.redirect;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ChangeProjectCSVLoader {
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
        int changeNumber = Integer.parseInt(columns[0]);
        String projectName = columns[1];
        dataMap.put(changeNumber, projectName);
      }
    } catch (Exception e) {
      throw new CSVProcessingException(String.format("Error processing CSV file: %s", filePath), e);
    }

    return dataMap;
  }
}
