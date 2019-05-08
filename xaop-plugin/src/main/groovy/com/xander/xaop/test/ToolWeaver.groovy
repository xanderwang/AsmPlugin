package com.xander.xaop.test

import com.xander.aop.transform.BaseWeaver
import com.xander.aop.transform.XaopConfig
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

public class ToolWeaver extends BaseWeaver {

  private XaopConfig config = new XaopConfig()

  @Override
  public void setExtension(Object extension) {
    if (extension == null) {
      return
    }
    this.config = (XaopConfig) extension
  }

  @Override
  public boolean isWeavableClass(String fullQualifiedClassName) {
    if (config.log) {
      println "isWeavableClass:${fullQualifiedClassName}"
    }
    boolean superResult = super.isWeavableClass(fullQualifiedClassName)
    if (config != null && superResult && !config.whiteList.isEmpty()) {
      // whitelist is prior to to blacklist
      // 如果配置了白名单，那么就只修改白名单里面的类
      for (String item : config.whiteList) {
        if (fullQualifiedClassName.startsWith(item)) {
          return false
        }
      }
    }
    if (config != null && superResult && !config.blackList.isEmpty()) {
      for (String item : config.blackList) {
        if (fullQualifiedClassName.startsWith(item)) {
          return false
        }
      }
    }
    return superResult
  }

  @Override protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
    return new ToolClassAdapter(classWriter)
  }
}
