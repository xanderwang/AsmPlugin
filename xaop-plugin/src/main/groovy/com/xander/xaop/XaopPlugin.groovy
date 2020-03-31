package com.xander.xaop

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import com.xander.plugin.IPluginFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

class XaopPlugin implements Plugin<Project> , IPluginFactory {

  void apply(Project project) {
//    AppExtension appExtension = (AppExtension) project.getProperties().get("android")
////    appExtension.registerTransform(new CustormTransform(project), Collections.EMPTY_LIST)
//    appExtension.registerTransform(new AopToolTransform(project), Collections.EMPTY_LIST)
    AppExtension android = project.extensions.getByType(AppExtension)
    android.registerTransform(createTransform(project))
  }

  @Override
  Transform createTransform(Project project) {
    return new AopToolTransform(project)
  }
}
