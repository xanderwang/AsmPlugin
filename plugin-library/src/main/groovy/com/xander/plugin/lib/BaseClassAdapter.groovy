package com.xander.plugin.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class BaseClassAdapter extends ClassVisitor {

  protected String className

  boolean log

  IWeaver iWeaver

  PluginConfig pluginConfig

  BaseClassAdapter(final ClassVisitor cv) {
    super(Opcodes.ASM5, cv)
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName,
             String[] interfaces) {
    this.className = name.replaceAll("/", ".")
    if (log) println "BaseClassAdapter visit class:$className"
    super.visit(version, access, name, signature, superName, interfaces)
  }

  @Override
  MethodVisitor visitMethod(final int access, final String name, final String desc,
                            final String signature, final String[] exceptions) {
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
    return mv == null ? null : createMethodAdapter(className + '.' + name, access, desc, mv)
  }

  BaseMethodAdapter createMethodAdapter(String name, int access, String desc, MethodVisitor mv) {
    if (iWeaver) return iWeaver.createMethodVisitor(name, access, desc, mv)
    return new BaseMethodAdapter(name, access, desc, mv)
  }
}
