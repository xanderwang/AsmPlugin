package com.xander.xaop

import org.gradle.api.Plugin
import org.gradle.api.Project

public class XaopPlugin implements Plugin<Project> {

  void apply(Project project) {
    System.out.println("========================");
    System.out.println("Hello XaopPlugin!");
    System.out.println("========================");
  }
}
