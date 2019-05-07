package com.xander.xaop.test

import com.xander.aop.transform.BaseWeaver
import com.xander.aop.transform.XaopConfig
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

public class TestWeaver extends BaseWeaver {

  private static final String PLUGIN_LIBRARY = "com.xander.xaop.test"

  private XaopConfig config

  @Override public void setExtension(Object extension) {
    if (extension == null) {
      return
    }
    this.config = (XaopConfig) extension
  }

  @Override public boolean isWeavableClass(String fullQualifiedClassName) {
    boolean superResult = super.isWeavableClass(fullQualifiedClassName)
    boolean isByteCodePlugin = fullQualifiedClassName.startsWith(PLUGIN_LIBRARY)
    if (tetsHunterExtension != null) {
      //whitelist is prior to to blacklist
      if (!config.whitePackageList.isEmpty()) {
        boolean inWhiteList = false
        for (String item : config.whitePackageList) {
          if (fullQualifiedClassName.startsWith(item)) {
            inWhiteList = true
          }
        }
        return superResult && !isByteCodePlugin && inWhiteList
      }
      if (!tetsHunterExtension.blacklist.isEmpty()) {
        boolean inBlackList = false
        for (String item : tetsHunterExtension.blacklist) {
          if (fullQualifiedClassName.startsWith(item)) {
            inBlackList = true
          }
        }
        return superResult && !isByteCodePlugin && !inBlackList
      }
    }
    return superResult && !isByteCodePlugin
  }

  @Override protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
    return new TestClassAdapter(classWriter)
  }
}
