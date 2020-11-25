package com.xander.plugin.asm

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.plugin.asm.lib.BaseWeaverFactory
import com.xander.plugin.asm.lib.PluginConfig
import org.gradle.api.Project
import java.io.IOException

class AsmTransform(project: Project) : BaseTransform(project) {
  override fun createWeaver(): BaseWeaverFactory {
    return BaseWeaverFactory()
  }

  override fun createPluginConfig(): PluginConfig {
    return project.extensions.getByName(CONFIG) as PluginConfig
  }

  @Throws(TransformException::class, InterruptedException::class, IOException::class)
  override fun transform(transformInvocation: TransformInvocation) {
    try {
      super.transform(transformInvocation)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  companion object {
    var CONFIG = "asmConfig"
  }

  init {
    project.extensions.create(CONFIG, PluginConfig::class.java)
  }
}