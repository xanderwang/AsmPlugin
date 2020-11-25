package com.xander.plugin.asm.lib

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

open class BaseWeaverFactory : IWeaverFactory {

  @Throws(IOException::class)
  override fun weaveJar(inputJar: File, outputJar: File) {
    val inputZip = ZipFile(inputJar)
    val outputZip = ZipOutputStream(BufferedOutputStream(Files.newOutputStream(outputJar.toPath())))
    val inEntries = inputZip.entries()
    while (inEntries.hasMoreElements()) {
      val entry = inEntries.nextElement()
      val originalFile: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
      val outEntry = ZipEntry(entry.name)
      var newEntryContent: ByteArray
      newEntryContent = if (!isWearableClass(outEntry.name.replace("/", "."))) {
        IOUtils.toByteArray(originalFile)
      } else {
        weaveSingleClassToByteArray(originalFile)
      }
      val crc32 = CRC32()
      crc32.update(newEntryContent)
      outEntry.crc = crc32.value
      outEntry.method = ZipEntry.STORED
      outEntry.size = newEntryContent.size.toLong()
      outEntry.compressedSize = newEntryContent.size.toLong()
      outEntry.lastAccessTime = ZERO
      outEntry.lastModifiedTime = ZERO
      outEntry.creationTime = ZERO
      outputZip.putNextEntry(outEntry)
      outputZip.write(newEntryContent)
      outputZip.closeEntry()
    }
    outputZip.flush()
    outputZip.close()
  }

  @Throws(IOException::class)
  override fun weaveSingleClass(inputFile: File, outputFile: File, inputBaseDir: String) {
    var inputBaseDir = inputBaseDir
    if (!inputBaseDir.endsWith("/")) {
      inputBaseDir = "$inputBaseDir/"
    }
    val className = inputFile.absolutePath
        .replace(inputBaseDir, "")
        .replace("/", ".")
    if (pluginConfig?.log==true) {
      println("class name:$className")
    }
    if (isWearableClass(className)) {
      FileUtils.touch(outputFile)
      val inputStream: InputStream = FileInputStream(inputFile)
      val bytes = weaveSingleClassToByteArray(inputStream)
      val fos = FileOutputStream(outputFile)
      fos.write(bytes)
      fos.close()
      inputStream.close()
    } else {
      if (inputFile.isFile) {
        FileUtils.touch(outputFile)
        FileUtils.copyFile(inputFile, outputFile)
      }
    }
  }



  @Throws(IOException::class)
  override fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
    // val classWriter: ClassWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
    // 自定义逻辑开始介入
    val classWriterWrapper = wrapClassWriter(classWriter)
    val classReader = ClassReader(inputStream)
    // 开始访问 class
    classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
    return classWriter.toByteArray()
  }

  private fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
    return createClassVisitor(classWriter)
  }

  override fun isWearableClass(fullQualifiedClassName: String): Boolean {
    return (fullQualifiedClassName.endsWith(".class") && !fullQualifiedClassName.contains("R\$")
        && !fullQualifiedClassName.contains("R.class") && !fullQualifiedClassName.contains("BuildConfig.class"))
  }

  override fun createClassVisitor(classWriter: ClassWriter): ClassVisitor {
    return BaseClassVisitor(classWriter, this)
  }

  override fun createMethodVisitor(name: String, access: Int, desc: String, mv: MethodVisitor): MethodVisitor {
    return TimeMethodVisitor(name, access, desc, mv)
  }

  var classLoader: ClassLoader? = null


  companion object {
    private val ZERO = FileTime.fromMillis(0)
    var pluginConfig = PluginConfig.debug
  }
}