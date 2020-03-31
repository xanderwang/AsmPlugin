package com.xander.xaop

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.plugin.BaseTransform
import com.xander.plugin.lib.BaseWeaverFactory
import com.xander.plugin.lib.PluginConfig
import org.gradle.api.Project

class AopToolTransform extends BaseTransform {

  public static String CONFIG = "aopConfig"

  private Project project

  AopToolTransform(Project project) {
    super(project)
    this.project = project
//    project.getExtensions().create(CONFIG, AopConfig.class)
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
    } catch(Exception e) {
      e.printStackTrace()
    }
  }

  @Override 
  protected boolean inDuplcatedClassSafeMode() {
    return super.inDuplcatedClassSafeMode()
  }
}
