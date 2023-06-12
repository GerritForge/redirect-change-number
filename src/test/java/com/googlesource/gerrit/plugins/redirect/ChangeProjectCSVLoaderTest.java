package com.googlesource.gerrit.plugins.redirect;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class ChangeProjectCSVLoaderTest {

  @Test
  public void shouldLoadCSVFileWithProjectAndChange() {
    Map<Integer, String>  changeProjectMap = ChangeProjectCSVLoader.loadCSV("change_project.csv");
    Map<Integer, String> expectedChangeProjectMap = new HashMap<>();
    expectedChangeProjectMap.put(789096,"test-namespace/project1");
    expectedChangeProjectMap.put(789097,"test-namespace/project1");
    expectedChangeProjectMap.put(789098,"test-namespace/project2");

    assertThat(changeProjectMap, is(expectedChangeProjectMap));
  }
}
