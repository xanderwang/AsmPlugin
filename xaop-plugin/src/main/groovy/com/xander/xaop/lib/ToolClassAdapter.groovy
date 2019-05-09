package com.xander.xaop.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ToolClassAdapter extends ClassVisitor {

  private String className

  ToolClassAdapter(final ClassVisitor cv) {
    super(Opcodes.ASM5, cv)
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName,
    String[] interfaces) {
    this.className = name.replaceAll("/", ".")
    if (className.contains("xander")) {
      println "vist class:${className}"
    }
    super.visit(version, access, name, signature, superName, interfaces)
  }

  @Override MethodVisitor visitMethod(final int access, final String name, final String desc,
    final String signature, final String[] exceptions) {
    if (className.contains("xander")) {
      println "visitMethod:${name}"
    }
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
    return mv == null ? null : new ToolMethodAdapter(className + '.' + name, access, desc, mv)
  }
}
