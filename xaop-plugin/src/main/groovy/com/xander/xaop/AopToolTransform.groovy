package com.xander.xaop

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.aop.transform.XaopConfig
import com.xander.aop.transform.XaopTransform
import com.xander.xaop.lib.ToolWeaver
import org.gradle.api.Project

class AopToolTransform extends XaopTransform {

  public static String CONFIG = "aopConfig"

  private Project project

  AopToolTransform(Project project) {
    super(project)
    this.project = project
    project.getExtensions().create(CONFIG, XaopConfig.class)
    this.weaver = new ToolWeaver()
  }

  @Override
  void transform(TransformInvocation transformInvocation)
      throws TransformException, InterruptedException, IOException {
    weaver.setExtension(getXaopConfig())
    try {
      super.transform(transformInvocation)
    } catch(Exception e) {
      e.printStackTrace()
    }
  }

  @Override
  protected XaopConfig getXaopConfig() {
    XaopConfig config =  (XaopConfig) project.getExtensions().getByName(CONFIG)
    return config
  }

  @Override 
  protected boolean inDuplcatedClassSafeMode() {
    return getXaopConfig().duplcatedClassSafeMode
  }
}
