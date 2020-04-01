package com.xander.plugin.asm

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsmPlugin implements Plugin<Project> , IPluginFactory {

  void apply(Project project) {
//    AppExtension appExtension = (AppExtension) project.getProperties().get("android")
//    //appExtension.registerTransform(new CustormTransform(project), Collections.EMPTY_LIST)
//    appExtension.registerTransform(new AopToolTransform(project), Collections.EMPTY_LIST)
    AppExtension android = project.extensions.getByType(AppExtension.class)
    android.registerTransform(createTransform(project))
  }

  @Override
  Transform createTransform(Project project) {
    return new AsmTransform(project)
  }
}
