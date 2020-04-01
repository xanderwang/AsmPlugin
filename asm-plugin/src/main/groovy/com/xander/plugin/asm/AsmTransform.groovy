package com.xander.plugin.asm

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.plugin.asm.lib.BaseWeaverFactory
import com.xander.plugin.asm.lib.PluginConfig
import org.gradle.api.Project

class AsmTransform extends BaseTransform {

  public static String CONFIG = "asmConfig"

  private Project project

  AsmTransform(Project project) {
    super(project)
    this.project = project
    project.getExtensions().create(CONFIG, PluginConfig.class)
  }

  @Override
  BaseWeaverFactory createWeaver() {
    return new BaseWeaverFactory()
  }

  @Override
  PluginConfig createPluginConfig() {
    return project.getExtensions().getByName(CONFIG)
  }

  @Override
  void transform(TransformInvocation transformInvocation)
    throws TransformException, InterruptedException, IOException {
    try {
      super.transform(transformInvocation)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  @Override
  protected boolean inDuplcatedClassSafeMode() {
    return super.inDuplcatedClassSafeMode()
  }
}
