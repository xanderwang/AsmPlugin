package com.xander.xaop

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class XaopPlugin implements Plugin<Project> {

  void apply(Project project) {
    AppExtension appExtension = (AppExtension) project.getProperties().get("android")
//    appExtension.registerTransform(new CustormTransform(project), Collections.EMPTY_LIST)
    appExtension.registerTransform(new AopToolTransform(project), Collections.EMPTY_LIST)
  }
}
