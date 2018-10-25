package com.xander.xaop

import org.gradle.api.Plugin
import org.gradle.api.Project

public class XAOP_Plugin implements Plugin<Project> {

  void apply(Project project) {
    System.out.println("========================");
    System.out.println("Hello XAOP_Plugin!");
    System.out.println("========================");
  }
}
