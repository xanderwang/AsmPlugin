package com.xander.plugin.asm

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.plugin.asm.lib.BaseWeaverFactory
import org.gradle.api.Project
import java.io.IOException

class AsmTransform(project: Project) : BaseClassTransform(project) {

  override fun createWeaver(): BaseWeaverFactory {
    return BaseWeaverFactory()
  }

  @Throws(TransformException::class, InterruptedException::class, IOException::class)
  override fun transform(transformInvocation: TransformInvocation) {
    // println("AsmTransform transform =============================================")
    try {
      super.transform(transformInvocation)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun getConfigName(): String {
    return CONFIG
  }

  companion object {
    var CONFIG = "asmConfig"
  }

}