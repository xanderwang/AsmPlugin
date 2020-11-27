package com.xander.plugin.asm.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import java.io.File
import java.io.IOException
import java.io.InputStream

open interface IWeaverFactory {

  fun setClassLoader(classLoader: ClassLoader)

  @Throws(IOException::class)
  fun weaveJar(inputJar: File, outputJar: File)

  @Throws(IOException::class)
  fun weaveSingleClass(inputFile: File, outputFile: File, inputBaseDir: String)

  /**
   * Check a certain file is wearable
   */
  @Throws(IOException::class)
  fun isWearableClass(className: String): Boolean

  /**
   * Weave single class to byte array
   */
  @Throws(IOException::class)
  fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray

  /**
   * 创建类编辑器
   */
  fun createClassVisitor(classWriter: ClassWriter): ClassVisitor

  /**
   * 创建方法编辑器
   */
  fun createMethodVisitor(methodName: String, access: Int, desc: String?, mv: MethodVisitor): MethodVisitor
}