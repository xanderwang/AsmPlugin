package com.xander.plugin.asm

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AsmPlugin : Plugin<Project>, IPluginFactory {

  override fun apply(project: Project) {
    // println("AsmPlugin =============================================")
    // AppExtension appExtension = (AppExtension) project.getProperties().get("android")
    // // appExtension.registerTransform(new CustormTransform(project), Collections.EMPTY_LIST)
    // appExtension.registerTransform(new AopToolTransform(project), Collections.EMPTY_LIST)
    val android = project.extensions.getByType(AppExtension::class.java)
    // println("android:$android")
    val transform = createTransform(project)
    // println("transform:$transform")
    android.registerTransform(transform)
  }

  override fun createTransform(project: Project): Transform {
    return AsmTransform(project)
  }
}