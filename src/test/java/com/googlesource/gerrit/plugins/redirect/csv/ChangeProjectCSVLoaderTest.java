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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ChangeProjectCSVLoaderTest {

  @Test
  public void shouldLoadCSVFileWithProjectAndChange() {
    Map<Integer, String> changeProjectMap = ChangeProjectCSVLoader.loadCSV("change_project.csv");
    Map<Integer, String> expectedChangeProjectMap = new HashMap<>();
    expectedChangeProjectMap.put(789096, "test-namespace/project1");
    expectedChangeProjectMap.put(789097, "test-namespace/project1");
    expectedChangeProjectMap.put(789098, "test-namespace/project2");

    assertThat(changeProjectMap, is(expectedChangeProjectMap));
  }
}
