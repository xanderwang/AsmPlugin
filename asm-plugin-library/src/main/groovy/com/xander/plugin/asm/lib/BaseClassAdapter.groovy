package com.xander.plugin.asm.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class BaseClassAdapter extends ClassVisitor {

  protected String className

  IWeaverFactory iWeaverFactory

  static PluginConfig pluginConfig

  BaseClassAdapter(final ClassVisitor cv, IWeaverFactory weaverFactory) {
    super(Opcodes.ASM5, cv)
    iWeaverFactory = weaverFactory
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName,
    String[] interfaces) {
    this.className = name.replaceAll("/", ".")
    if ( null != pluginConfig && pluginConfig.log) println "BaseClassAdapter visit class:$className"
    super.visit(version, access, name, signature, superName, interfaces)
  }

  @Override
  MethodVisitor visitMethod(final int access, final String name, final String desc,
    final String signature, final String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
    return mv == null ? null : createMethodAdapter(className + '.' + name, access, desc, mv)
  }

  BaseMethodAdapter createMethodAdapter(String name, int access, String desc, MethodVisitor mv) {
    if (iWeaverFactory) return iWeaverFactory.createMethodVisitor(name, access, desc, mv)
    return new BaseMethodAdapter(name, access, desc, mv)
  }
}
