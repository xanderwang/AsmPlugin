package com.xander.plugin.asm

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @Description //TODO
 *
 * @author Xander Wang
 * Created on 2020/11/27.
 */
open abstract class BasePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val android = project.extensions.getByType(AppExtension::class.java)
    createTransforms(project).forEach {
      android.registerTransform(it)
    }
  }

  abstract fun createTransforms(project: Project): List<Transform>
}