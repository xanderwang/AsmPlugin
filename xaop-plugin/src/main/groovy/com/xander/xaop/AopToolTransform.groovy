package com.xander.xaop

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.aop.transform.XaopConfig
import com.xander.aop.transform.XaopTransform
import com.xander.xaop.lib.ToolWeaver
import org.gradle.api.Project

public class AopToolTransform extends XaopTransform {
  private Project project

  public AopToolTransform(Project project) {
    super(project)
    this.project = project
    project.getExtensions().create("aopConfig", XaopConfig.class)
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
    return (XaopConfig) project.getExtensions().getByName("aopConfig")
  }

  @Override 
  protected boolean inDuplcatedClassSafeMode() {
    return getXaopConfig().duplcatedClassSafeMode
  }
}
