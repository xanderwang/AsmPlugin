package com.xander.xaop

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class XaopPlugin implements Plugin<Project> {

  void apply(Project project) {
    System.out.println("========================")
    System.out.println("Hello XaopPlugin!")
    System.out.println("========================")
    // method 1
//    project.android.registerTransform(new CustormTransform())
    // method 2
    AppExtension appExtension = (AppExtension)project.getProperties().get("android")
    appExtension.registerTransform(new CustormTransform(), Collections.EMPTY_LIST)

  }
}
