package com.xander.plugin.asm.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

open class BaseClassVisitor(cv: ClassVisitor, private var iWeaverFactory: IWeaverFactory) :
    ClassVisitor(Opcodes.ASM9, cv) {

  override fun visit(version: Int, access: Int, name: String?, signature: String?,
                     superName: String?, interfaces: Array<out String>?) {
    if (name != null) {
      className = name.replace("/", ".")
    }
    if (pluginConfig.classLog) {
      println("BaseClassVisitor visit name:$name")
      println("BaseClassVisitor visit class:$className")
    }
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitMethod(access: Int, methodName: String?, descriptor: String?,
                           signature: String?, exceptions: Array<out String>?): MethodVisitor {
    val mv = cv.visitMethod(access, methodName, descriptor, signature, exceptions)
    mv?.let {
      return createMethodVisitor("$className.$methodName", access, descriptor!!, mv)
    }
    return super.visitMethod(access, methodName, descriptor, signature, exceptions)
  }

  private fun createMethodVisitor(
      name: String, access: Int, desc: String?, mv: MethodVisitor): MethodVisitor {
    return iWeaverFactory.createMethodVisitor(name, access, desc, mv)
  }

  private var className = ""

  companion object {
    var pluginConfig = PluginConfig.debug
  }
}