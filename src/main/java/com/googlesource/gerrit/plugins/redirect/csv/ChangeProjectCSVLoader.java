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

package com.googlesource.gerrit.plugins.redirect.csv;

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
      while ((line = br.readLine()) != null) {
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
