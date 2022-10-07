package io.github.xander.plugin.lib

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.util.TraceClassVisitor
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

open class BaseWeaverManager : IWeaverManager {

    override fun weaveJar(inputJar: File, outputJar: File) {
        val inputZip = ZipFile(inputJar)
        val outputZip = ZipOutputStream(BufferedOutputStream(Files.newOutputStream(outputJar.toPath())))
        val inEntries = inputZip.entries()
        while (inEntries.hasMoreElements()) {
            val entry = inEntries.nextElement()
            val originalFile: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
            val outEntry = ZipEntry(entry.name)
            var newEntryContent: ByteArray = if (!isWearableClass(outEntry.name.replace("/", "."))) {
                IOUtils.toByteArray(originalFile)
            } else {
                if (pluginConfig.classLog) {
                    println("weaveJar entry:${entry.name}, outEntry:$outEntry")
                }
                try {
                    weaveSingleClassByteArraByy(originalFile)
                } catch (exception: Exception) {
                    println("weaveJar error entry:${entry.name}, outEntry:$outEntry")
                    exception.printStackTrace()
                    IOUtils.toByteArray(originalFile)
                }
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

    override fun weaveSingleClass(inputFile: File, outputFile: File, inputBaseDir: String) {
        if (inputFile.isDirectory) {
            println("weaveSingleClass:${inputFile.absolutePath} is dir !!!!!!!!!!")
            return
        }
        var inputBaseDir = inputBaseDir
        if (!inputBaseDir.endsWith(File.separatorChar)) {
            inputBaseDir = "$inputBaseDir${File.separatorChar}"
        }
        val className = inputFile.absolutePath.replace(inputBaseDir, "").replace("${File.separatorChar}", ".")
        if (pluginConfig.classLog) {
            // println("weaveSingleClass inputFile:${inputFile.absolutePath}")
            // println("weaveSingleClass outputFile:${outputFile.absolutePath}")
            // println("weaveSingleClass inputBaseDir:$inputBaseDir")
            println("weaveSingleClass:$className")
        }
        FileUtils.touch(outputFile)
        if (isWearableClass(className)) {
            try {
                val inputStream: InputStream = FileInputStream(inputFile)
                val bytes = weaveSingleClassByteArraByy(inputStream)
                val fos = FileOutputStream(outputFile)
                fos.write(bytes)
                fos.close()
                inputStream.close()
            } catch (exception: Exception) {
                println("weaveSingleClass error:$className")
                exception.printStackTrace()
                FileUtils.copyFile(inputFile, outputFile)
            }
        } else {
            FileUtils.copyFile(inputFile, outputFile)
        }
    }

    open fun weaveSingleClassByteArraByy(inputStream: InputStream): ByteArray {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        // val traceClassVisitor = TraceClassVisitor(classWriter, PrintWriter(System.out))
        // val extendClassWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
        val classReader = ClassReader(inputStream)
        // 开始访问 class
        classReader.accept(createClassVisitor(classWriter), parsingOptions())
        return classWriter.toByteArray()
    }

    override fun isWearableClass(className: String): Boolean {
        return (className.endsWith(".class") && !className.contains(".R\$") && !className.contains(".R.class"))
        // return false
    }

    override fun parsingOptions(): Int {
        return ClassReader.EXPAND_FRAMES
    }

    override fun createClassVisitor(classVisitor: ClassVisitor): ClassVisitor {
        return TraceClassVisitor(classVisitor, PrintWriter(System.out))
    }

    companion object {
        private val ZERO = FileTime.fromMillis(0)
        var pluginConfig = BasePluginConfig.debug
    }
}