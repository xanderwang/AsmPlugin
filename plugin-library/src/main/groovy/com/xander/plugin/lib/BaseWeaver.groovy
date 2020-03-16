package com.xander.plugin.lib

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.LocalVariablesSorter

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BaseWeaver implements IWeaver {

  private static final FileTime ZERO = FileTime.fromMillis(0)

  protected ClassLoader classLoader

  PluginConfig pluginConfig

  BaseWeaver() {}

  final void weaveJar(File inputJar, File outputJar) throws IOException {
    ZipFile inputZip = new ZipFile(inputJar)
    ZipOutputStream outputZip = new ZipOutputStream(
        new BufferedOutputStream(Files.newOutputStream(outputJar.toPath())))
    Enumeration<? extends ZipEntry> inEntries = inputZip.entries()
    while (inEntries.hasMoreElements()) {
      ZipEntry entry = inEntries.nextElement()
      InputStream originalFile = new BufferedInputStream(inputZip.getInputStream(entry))
      ZipEntry outEntry = new ZipEntry(entry.getName())
      byte[] newEntryContent
      if (!isWearableClass(outEntry.getName().replace("/", "."))) {
        newEntryContent = IOUtils.toByteArray(originalFile)
      } else {
        newEntryContent = weaveSingleClassToByteArray(originalFile)
      }
      CRC32 crc32 = new CRC32()
      crc32.update(newEntryContent)
      outEntry.setCrc(crc32.getValue())
      outEntry.setMethod(ZipEntry.STORED)
      outEntry.setSize(newEntryContent.length)
      outEntry.setCompressedSize(newEntryContent.length)
      outEntry.setLastAccessTime(ZERO)
      outEntry.setLastModifiedTime(ZERO)
      outEntry.setCreationTime(ZERO)
      outputZip.putNextEntry(outEntry)
      outputZip.write(newEntryContent)
      outputZip.closeEntry()
    }
    outputZip.flush()
    outputZip.close()
  }

  final void weaveSingleClass(File inputFile, File outputFile, String inputBaseDir)
      throws IOException {
    if (!inputBaseDir.endsWith("/")) inputBaseDir = inputBaseDir + "/"
    String className = inputFile.getAbsolutePath().replace(inputBaseDir, "").replace("/", ".")
//    println "calss name:${className}"
    if (isWearableClass(className)) {
      FileUtils.touch(outputFile)
      InputStream inputStream = new FileInputStream(inputFile)
      byte[] bytes = weaveSingleClassToByteArray(inputStream)
      FileOutputStream fos = new FileOutputStream(outputFile)
      fos.write(bytes)
      fos.close()
      inputStream.close()
    } else {
      if (inputFile.isFile()) {
        FileUtils.touch(outputFile)
        FileUtils.copyFile(inputFile, outputFile)
      }
    }
  }

  final void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader
  }

  @Override
  byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException {
    ClassReader classReader = new ClassReader(inputStream)
    ClassWriter classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
    ClassVisitor classWriterWrapper = wrapClassWriter(classWriter)
    classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

  protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
    return createClassVisitor(classWriter)
  }

  @Override
  boolean isWearableClass(String fullQualifiedClassName) {
    return fullQualifiedClassName.endsWith(".class") && !fullQualifiedClassName.contains("R\$") &&
        !fullQualifiedClassName.contains("R.class") &&
        !fullQualifiedClassName.contains("BuildConfig.class")
  }

  @Override
  ClassVisitor createClassVisitor(ClassWriter classWriter) {
    BaseClassAdapter baseClassAdapter = new BaseClassAdapter(classWriter)
    baseClassAdapter.iWeaver = this
    baseClassAdapter.pluginConfig = pluginConfig
    return baseClassAdapter
  }

  @Override
  LocalVariablesSorter createMethodVisitor(String name, int access, String desc, MethodVisitor mv) {
    BaseMethodAdapter baseMethodAdapter = new BaseMethodAdapter(name, access, desc, mv)
    baseMethodAdapter.pluginConfig = pluginConfig
    return baseMethodAdapter
  }
}
