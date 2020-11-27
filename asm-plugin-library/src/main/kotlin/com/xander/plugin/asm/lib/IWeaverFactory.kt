package com.xander.plugin.asm.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import java.io.File

open interface IWeaverFactory {

  @Deprecated("not good method")
  fun setClassLoader(classLoader: ClassLoader)

  fun weaveJar(inputJar: File, outputJar: File)

  fun weaveSingleClass(inputFile: File, outputFile: File, inputBaseDir: String)

  /**
   * Check a certain file is wearable
   */
  fun isWearableClass(className: String): Boolean

  /**
   * 创建类编辑器
   */
  fun createClassVisitor(classWriter: ClassWriter): ClassVisitor

  /**
   * 创建方法编辑器
   */
  fun createMethodVisitor(methodName: String, access: Int, desc: String?, mv: MethodVisitor): MethodVisitor
}