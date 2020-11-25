package com.xander.plugin.asm.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

open class BaseClassVisitor(cv: ClassVisitor, private var iWeaverFactory: IWeaverFactory) : ClassVisitor(Opcodes.ASM5, cv) {

  override fun visit(
      version: Int, access: Int, name: String, signature: String,
      superName: String, interfaces: Array<String>) {
    className = name.replace("/", ".")
    if (pluginConfig.log) {
      println("BaseClassVisitor visit class:$className")
    }
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitMethod(
      access: Int, name: String, desc: String, signature: String, exceptions: Array<String>): MethodVisitor? {
    val mv = cv.visitMethod(access, name, desc, signature, exceptions)
    mv?.let {
      return createMethodVisitor("$className.$name", access, desc, mv)
    }
    return null
  }

  private fun createMethodVisitor(
      name: String, access: Int, desc: String, mv: MethodVisitor): MethodVisitor {
    return iWeaverFactory.createMethodVisitor(name, access, desc, mv)
  }

  private var className = ""

  companion object {
    var pluginConfig = PluginConfig.debug
  }
}