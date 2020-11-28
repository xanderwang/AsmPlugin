package com.xander.plugin.asm

import com.android.build.api.transform.Transform
import org.gradle.api.Project

class AsmPlugin : BasePlugin() {

  override fun createTransforms(project: Project): List<Transform> {
    return listOf(AsmTransform(project))
  }

}