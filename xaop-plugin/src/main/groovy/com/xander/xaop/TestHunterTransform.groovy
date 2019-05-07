package com.xander.xaop

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.xander.aop.transform.XaopConfig
import com.xander.aop.transform.XaopTransform
import com.xander.xaop.test.TestWeaver
import org.gradle.api.Project

public class TestHunterTransform extends XaopTransform {
  private Project project

  public TestHunterTransform(Project project) {
    super(project)
    this.project = project
    project.getExtensions().create("aopConfig", XaopConfig.class)
    this.weaver = new TestWeaver()
  }

  @Override
  void transform(TransformInvocation transformInvocation)
      throws TransformException, InterruptedException, IOException {
    weaver.setExtension(getXaopConfig())
    super.transform(transformInvocation)
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
