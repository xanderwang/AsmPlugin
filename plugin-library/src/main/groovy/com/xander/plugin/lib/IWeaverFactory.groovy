package com.xander.plugin.lib

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.LocalVariablesSorter

interface IWeaverFactory {

  /**
   * Check a certain file is wearable*/
  boolean isWearableClass(String filePath) throws IOException

  /**
   * Weave single class to byte array*/
  byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException

  ClassVisitor createClassVisitor(ClassWriter classWriter)

  LocalVariablesSorter createMethodVisitor(String name, int access, String desc, MethodVisitor mv)
}

