package com.xander.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.google.common.io.Files
import com.xander.plugin.lib.PluginConfig
import com.xander.plugin.lib.BaseWeaverFactory
import com.xander.plugin.lib.URLClassLoaderHelper
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

abstract class BaseTransform extends Transform {

  private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>()

  static {
    SCOPES.add(QualifiedContent.Scope.PROJECT)
    SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
    SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
  }

  private Project project
  // 配置
  protected PluginConfig pluginConfig = new PluginConfig()

  // 用来编辑字节码
  protected BaseWeaverFactory weaver
  // 多任务
//  private WaitableExecutor waitableExecutor
  private WaitableExecutor waitableExecutor

  BaseTransform(Project project) {
    this.project = project
//    this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    weaver = createWeaver()
  }

  @Override
  String getName() {
    return this.getClass().getSimpleName()
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
  }

  @Override
  Set<QualifiedContent.Scope> getScopes() {
    return SCOPES
  }

  @Override
  boolean isIncremental() {
    return true
  }

  @Override
  boolean isCacheable() {
    return true
  }

  abstract BaseWeaverFactory createWeaver()

  abstract PluginConfig createPluginConfig()

  @Override
  void transform(TransformInvocation transformInvocation)
    throws TransformException, InterruptedException, IOException {
    //super.transform(transformInvocation)
    boolean skip = false
    pluginConfig = createPluginConfig()
    println "${name} config:${pluginConfig}"
    weaver.pluginConfig = pluginConfig
    String variantName = transformInvocation.context.variantName
    if ("debug" == variantName) {
      skip = pluginConfig.debugSkip
    } else if ("release" == variantName) {
      skip = pluginConfig.releaseSkip
    }
    println "${name} variant:${variantName},skip:${skip}"
    long startTime = System.currentTimeMillis()
    TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
    if (!transformInvocation.incremental) {
      outputProvider.deleteAll()
    }
    Collection<TransformInput> inputs = transformInvocation.inputs
    Collection<TransformInput> referencedInputs = transformInvocation.referencedInputs
    URLClassLoader urlClassLoader = URLClassLoaderHelper.
      getClassLoader(inputs, referencedInputs, project)
    this.weaver.setClassLoader(urlClassLoader)
    boolean flagForCleanDexBuilderFolder = false

    for (TransformInput input : inputs) {
      for (JarInput jarInput : input.jarInputs) {
        File dest = outputProvider.getContentLocation(jarInput.file.absolutePath,
          jarInput.contentTypes,
          jarInput.scopes,
          Format.JAR)
        if (pluginConfig.log) {
          println "${name} jarInput:${jarInput.file.absolutePath},dest:${dest.absolutePath}"
        }
        if (skip || pluginConfig.skipJar) {
          println " ${name} skip transform jar:${jarInput.file.absolutePath}"
          FileUtils.copyFile(jarInput.file, dest)
          continue
        }
        Status status = jarInput.status
        if (transformInvocation.incremental) {
          switch (status) {
            case NOTCHANGED:
              break
            case ADDED:
            case CHANGED:
              transformJar(jarInput.file, dest)
              break
            case REMOVED:
              if (dest.exists()) {
                FileUtils.forceDelete(dest)
              }
              break
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

      for (DirectoryInput directoryInput : input.directoryInputs) {
        File dest = outputProvider.getContentLocation(directoryInput.name,
          directoryInput.contentTypes,
          directoryInput.scopes,
          Format.DIRECTORY)
        if (pluginConfig.log) {
          println "${name} directoryInput:${directoryInput.file.absolutePath},dest:${dest.absolutePath}"
        }
        FileUtils.forceMkdir(dest)
        if (skip) {
          println "skip transform dir:${directoryInput.file.absolutePath}"
          FileUtils.copyDirectory(directoryInput.file, dest)
          continue
        }
        if (transformInvocation.incremental) {
          String srcDirPath = directoryInput.file.absolutePath
          String destDirPath = dest.absolutePath
          Map<File, Status> fileStatusMap = directoryInput.changedFiles
          for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
            Status status = changedFile.value
            File inputFile = changedFile.key
            String destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            switch (status) {
              case NOTCHANGED:
                break
              case REMOVED:
                if (destFile.exists()) {
                  //noinspection ResultOfMethodCallIgnored
                  destFile.delete()
                }
                break
              case ADDED:
              case CHANGED:
                try {
                  FileUtils.touch(destFile)
                } catch (IOException e) {
                  //maybe mkdirs fail for some strange reason, try again.
                  Files.createParentDirs(destFile)
                }
                transformSingleFile(inputFile, destFile, srcDirPath)
                break
            }
          }
        } else {
          transformDir(directoryInput.file, directoryInput.file.absolutePath, dest.absolutePath)
        }
      }
    }

    waitableExecutor.waitForTasksWithQuickFail(true)
    long costTime = System.currentTimeMillis() - startTime
    println "plugin ${name} costed:${costTime}ms";
  }

  protected void transformSingleFile(final File inputFile, final File outputFile,
    final String srcBaseDir) {
    if (pluginConfig.log) {
      println "transformSingleFile inputFile:${inputFile.absolutePath}"
      println "transformSingleFile outputFile:${outputFile.absolutePath}"
    }
    waitableExecutor.execute({
      weaver.weaveSingleClass(inputFile, outputFile, srcBaseDir)
      return null
    })
  }

  protected void transformDir(final File sourceDir, final String inputDirPath,
    final String outputDirPath) throws IOException {
    if (null != sourceDir && sourceDir.isDirectory()) {
      File[] files = sourceDir.listFiles()
      if (null == files || files.length == 0) {
        return
      }
      ArrayList<File> childFiles = new ArrayList<>()
      for (File sourceFile : sourceDir.listFiles()) {
        if (sourceFile.isDirectory()) {
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

  protected void transformFileList(final ArrayList<File> sourceList, final String inputDirPath,
    final String outputDirPath) {
    waitableExecutor.execute({
      for (File sourceFile : sourceList) {
        String sourceFilePath = sourceFile.getAbsolutePath()
        File outputFile = new File(sourceFilePath.replace(inputDirPath, outputDirPath))
        if (pluginConfig.log) {
          println "transformFileList sourceFile:${sourceFile.getAbsolutePath()}"
          println "transformFileList outputFile:${outputFile.getAbsolutePath()}"
        }
        weaver.weaveSingleClass(sourceFile, outputFile, inputDirPath)
      }
    })
  }

  protected void transformJar(final File srcJar, final File destJar) {
    if (pluginConfig.log) {
      println "transformJar srcJar:${srcJar.getAbsolutePath()}"
      println "transformJar destJar:${destJar.getAbsolutePath()}"
    }
    waitableExecutor.execute({
      weaver.weaveJar(srcJar, destJar)
      return null
    })
  }

  protected void cleanDexBuilderFolder(File dest) {
    waitableExecutor.execute({
      try {
        String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName(), "dexBuilder")
        //intermediates/transforms/dexBuilder/debug
        File file = new File(dexBuilderDir).getParentFile()
        println("clean dexBuilder folder = " + file.getAbsolutePath())
        if (file.exists() && file.isDirectory()) {
          FileUtils.deleteDirectory(file)
          //com.android.utils.FileUtils.deleteDirectoryContents(file)
        }
      } catch (Exception e) {
        e.printStackTrace()
      }
      return null
    })
  }

  protected String replaceLastPart(String originString, String replacement, String toreplace) {
    int start = originString.lastIndexOf(replacement)
    StringBuilder builder = new StringBuilder()
    builder.append(originString.substring(0, start))
    builder.append(toreplace)
    builder.append(originString.substring(start + replacement.length()))
    return builder.toString()
  }

//  protected PluginConfig getPluginConfig() {
//    return pluginConfig
//  }

  protected boolean inDuplcatedClassSafeMode() {
    return false
  }
}
