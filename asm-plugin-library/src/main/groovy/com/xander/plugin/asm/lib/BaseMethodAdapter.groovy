package com.xander.plugin.asm.lib

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

class BaseMethodAdapter extends LocalVariablesSorter implements Opcodes {

  private int startVarIndex

  protected String methodName

  static PluginConfig pluginConfig

  protected boolean debugTime = false
  protected boolean debugLine = false

  BaseMethodAdapter(String name, int access, String desc, MethodVisitor mv) {
    super(Opcodes.ASM5, access, desc, mv)
    this.methodName = name
    if (null != pluginConfig && pluginConfig.log) {
      println "method:$methodName"
    }
  }

  @Override
  AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    desc = desc.replaceAll("/", ".")
    if (desc.contains("com.xander.dev.tool.DebugTime")) {
      if (null != pluginConfig && pluginConfig.log) println "BaseMethodAdapter visitAnnotation ${desc}"
      debugTime = true
    }
    return super.visitAnnotation(desc, visible)
  }

  @Override
  void visitCode() {
    super.visitCode()
    if (debugTime) {
      startVarIndex = newLocal(Type.LONG_TYPE)
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J",
        false)
      mv.visitVarInsn(Opcodes.LSTORE, startVarIndex)
    }
  }

  @Override
  void visitInsn(int opcode) {
    if (debugTime) {
      if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
        int index = newLocal(Type.LONG_TYPE)
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        mv.visitVarInsn(LLOAD, startVarIndex)
        mv.visitInsn(LSUB)
        mv.visitVarInsn(LSTORE, index)
        mv.visitLdcInsn(methodName)
        mv.visitVarInsn(LLOAD, index)
        mv.
          visitMethodInsn(INVOKESTATIC, "com/xander/dev/tool/help/LogHelper", "d",
            "(Ljava/lang/String;J)V",
            false)
      }
    }
    super.visitInsn(opcode)
  }

  @Override
  void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    super.visitMethodInsn(opcode, owner, name, desc, itf)
  }
}
