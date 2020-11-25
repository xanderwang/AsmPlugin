package com.xander.plugin.asm

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.android.ide.common.workers.ExecutorServiceAdapter
import com.google.common.io.Files
import com.xander.plugin.asm.lib.*
import com.xander.plugin.asm.lib.URLClassLoaderHelper.getClassLoader
import groovy.lang.Closure
import groovy.lang.Reference
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ForkJoinPool

open abstract class BaseTransform(val project: Project) : Transform() {

  override fun getName(): String {
    return this.javaClass.simpleName
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return TransformManager.CONTENT_CLASS
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
    return SCOPES
  }

  override fun isIncremental(): Boolean {
    return true
  }

  override fun isCacheable(): Boolean {
    return true
  }

  abstract fun createWeaver(): BaseWeaverFactory

  abstract fun createPluginConfig(): PluginConfig

  private fun applyConfig(config: PluginConfig) {
    BaseWeaverFactory.pluginConfig = config
    BaseClassVisitor.pluginConfig = config
    TimeMethodVisitor.pluginConfig = config
  }

  @Throws(TransformException::class, InterruptedException::class, IOException::class)
  override fun transform(transformInvocation: TransformInvocation) {
    //super.transform(transformInvocation)
    val skip = Reference(false)
    println("plugin start ====================================================================")
    pluginConfig = createPluginConfig()
    println("$name config:$pluginConfig")
    applyConfig(pluginConfig)
    val variantName = transformInvocation.context.variantName
    if ("debug" == variantName) {
      skip.set(pluginConfig.debugSkip)
    } else if ("release" == variantName) {
      skip.set(pluginConfig.releaseSkip)
    }
    println("name: $name variant:$variantName ,skip:${skip.get()}")
    val startTime = System.currentTimeMillis()
    val outputProvider = transformInvocation.outputProvider
    if (!transformInvocation.isIncremental) {
      outputProvider.deleteAll()
    }
    val inputs = transformInvocation.inputs
    val referencedInputs = transformInvocation.referencedInputs
    val urlClassLoader = getClassLoader(inputs, referencedInputs, project)
//    weaver.setClassLoader(urlClassLoader)
    var flagForCleanDexBuilderFolder = false
    for (input in inputs) {
      for (jarInput in input.jarInputs) {
        val dest = outputProvider.getContentLocation(
            jarInput.file.absolutePath,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        if (pluginConfig.log) {
          println("jarInput:${jarInput.file.absolutePath} ,dest:${dest.absolutePath}")
        }
        if (skip.get() || pluginConfig.skipJar) {
          println("name:$name ,skip transform jar: ${jarInput.file.absolutePath}")
          FileUtils.copyFile(jarInput.file, dest)
          continue
        }
        val status = jarInput.status
        if (transformInvocation.isIncremental) {
          when (status) {
            Status.NOTCHANGED -> { }
            Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, dest)
            Status.REMOVED -> if (dest.exists()) FileUtils.forceDelete(dest)
          }
        } else {
          //Forgive me!, Some project will store 3rd-party aar for serveral copies in dexbuilder folder,,unknown issue.
          if (inDuplcatedClassSafeMode() && !flagForCleanDexBuilderFolder) {
            cleanDexBuilderFolder(dest)
            flagForCleanDexBuilderFolder = true
          }
          transformJar(jarInput.file, dest)
        }
      }
      for (directoryInput in input.directoryInputs) {
        val dest = outputProvider.getContentLocation(
            directoryInput.name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        if (pluginConfig.log) {
          println("name:$name, directoryInput:${directoryInput.file.absolutePath},dest:${dest.absolutePath}")
        }
        FileUtils.forceMkdir(dest)
        if (skip.get()) {
          println("skip transform dir:${directoryInput.file.absolutePath}")
          FileUtils.copyDirectory(directoryInput.file, dest)
          continue
        }
        if (transformInvocation.isIncremental) {
          val srcDirPath = directoryInput.file.absolutePath
          val destDirPath = dest.absolutePath
          val fileStatusMap = directoryInput.changedFiles
          for ((inputFile, status) in fileStatusMap) {
            val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            when (status) {
              Status.NOTCHANGED -> {
              }
              Status.REMOVED -> if (destFile.exists()) {
                destFile.delete()
              }
              Status.ADDED, Status.CHANGED -> {
                try {
                  FileUtils.touch(destFile)
                } catch (e: IOException) {
                  //maybe mkdirs fail for some strange reason, try again.
                  Files.createParentDirs(destFile)
                }
                transformSingleFile(inputFile, destFile, srcDirPath)
              }
            }
          }
        } else {
          transformDir(directoryInput.file, directoryInput.file.absolutePath, dest.absolutePath)
        }
      }
    }

    //waitableExecutor.waitForTasksWithQuickFail(true)
    val costTime = System.currentTimeMillis() - startTime
    println("plugin: $name, cost time: $costTime ms")
  }

  private fun transformSingleFile(inputFile: File, outputFile: File, srcBaseDir: String?) {
    if (pluginConfig.log) {
      println("transformSingleFile inputFile:${inputFile.absolutePath}")
      println("transformSingleFile outputFile:${outputFile.absolutePath}")
    }
    waitableExecutor.execute(object : Closure<Any?>(this, this) {
      fun doCall(it: Any?): Any? {
        weaver.weaveSingleClass(inputFile, outputFile, srcBaseDir!!)
        return null
      }

      fun doCall() {
        doCall(null)
      }
    })
  }

  @Throws(IOException::class)
  protected fun transformDir(sourceDir: File?, inputDirPath: String?, outputDirPath: String?) {
    if (null != sourceDir && sourceDir.isDirectory) {
      val files = sourceDir.listFiles()
      if (null == files || files.isEmpty()) {
        return
      }
      val childFiles = ArrayList<File>()
      for (sourceFile in sourceDir.listFiles()) {
        if (sourceFile.isDirectory) {
          transformDir(sourceFile, inputDirPath, outputDirPath)
        } else {
          childFiles.add(sourceFile)
        }
      }

      // 一次处理一个文件夹下面的文件，防止创建的任务过多
      if (!childFiles.isEmpty()) {
        transformFileList(childFiles, inputDirPath, outputDirPath)
      }
    }
  }

  private fun transformFileList(sourceList: ArrayList<File>, inputDirPath: String?, outputDirPath: String?) {
    waitableExecutor.execute(object : Closure<Void?>(this, this) {
      fun doCall(it: Any? = null) {
        for (sourceFile in sourceList) {
          val sourceFilePath = sourceFile.absolutePath
          val outputFile = File(sourceFilePath.replace(inputDirPath!!, outputDirPath!!))
          if (pluginConfig.log) {
            DefaultGroovyMethods.println(this@BaseTransform,
                "transformFileList sourceFile:" + sourceFile.absolutePath)
            DefaultGroovyMethods.println(this@BaseTransform,
                "transformFileList outputFile:" + outputFile.absolutePath)
          }
          weaver.weaveSingleClass(sourceFile, outputFile, inputDirPath)
        }
      }
    })
  }

  private fun transformJar(srcJar: File, destJar: File) {
    if (pluginConfig.log) {
      DefaultGroovyMethods.println(this, "transformJar srcJar:" + srcJar.absolutePath)
      DefaultGroovyMethods.println(this, "transformJar destJar:" + destJar.absolutePath)
    }
    waitableExecutor.execute(object : Closure<Any?>(this, this) {
      fun doCall(it: Any?): Any? {
        weaver.weaveJar(srcJar, destJar)
        return null
      }

      fun doCall() {
        doCall(null)
      }
    })
  }

  private fun cleanDexBuilderFolder(dest: File) {
    waitableExecutor.execute(object : Closure<Any?>(this, this) {
      fun doCall(it: Any?): Any? {
        try {
          val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
          //intermediates/transforms/dexBuilder/debug
          val file = File(dexBuilderDir).parentFile
          println("clean dexBuilder folder:${file.absolutePath}")
          if (file.exists() && file.isDirectory) {
            FileUtils.deleteDirectory(file)
            //com.android.utils.FileUtils.deleteDirectoryContents(file)
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
        return null
      }

      fun doCall() {
        doCall(null)
      }
    })
    executorFacade.executor
  }

  protected fun replaceLastPart(originString: String, replacement: String, toreplace: String?): String {
    val start = originString.lastIndexOf(replacement)
    val builder = StringBuilder()
    builder.append(originString.substring(0, start))
    builder.append(toreplace)
    builder.append(originString.substring(start + replacement.length))
    return builder.toString()
  }

  protected open fun inDuplcatedClassSafeMode(): Boolean {
    return false
  }

  protected var pluginConfig = PluginConfig.debug

  protected var weaver: IWeaverFactory

  private val waitableExecutor: WaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

  private val executorFacade: ExecutorServiceAdapter = ExecutorServiceAdapter(
      "",
      "",
      ForkJoinPool.commonPool()
  )

  companion object {
    private val SCOPES: MutableSet<QualifiedContent.Scope> = HashSet()
  }

  init {
    weaver = createWeaver()
  }
}