package com.xander.xaop.lib

import com.xander.aop.transform.BaseWeaver
import com.xander.aop.transform.XaopConfig
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class ToolWeaver extends BaseWeaver {

  private XaopConfig config = new XaopConfig()

  @Override
  void setExtension(Object extension) {
    if (extension == null) {
      return
    }
    this.config = (XaopConfig) extension
  }

  @Override
  boolean isWeavableClass(String fullQualifiedClassName) {
//    if (config.log && fullQualifiedClassName.concat("xander") ) {
//      println "======find class:${fullQualifiedClassName}"
//    }
    boolean superResult = super.isWeavableClass(fullQualifiedClassName)
    if (config != null && superResult && !config.whiteList.isEmpty()) {
      // whitelist is prior to to blacklist
      // 如果配置了白名单，那么就只修改白名单里面的类
      for (String item : config.whiteList) {
        if (fullQualifiedClassName.startsWith(item)) {
          return true
        }
      }
      return false
    }
    if (config != null && superResult && !config.blackList.isEmpty()) {
      for (String item : config.blackList) {
        if (fullQualifiedClassName.startsWith(item)) {
          return false
        }
      }
      return true
    }
    return superResult
  }

  @Override protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
    return new ToolClassAdapter(classWriter)
  }
}
