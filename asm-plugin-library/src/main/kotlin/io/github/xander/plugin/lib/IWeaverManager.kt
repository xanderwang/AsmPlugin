package io.github.xander.plugin.lib

import org.objectweb.asm.ClassVisitor
import java.io.File

/**
 * 编织器工厂
 */
interface IWeaverManager {

    /**
     * 编织 jar 包
     */
    fun weaveJar(inputJar: File, outputJar: File)

    /**
     * 编织单个 class 文件
     */
    fun weaveSingleClass(inputFile: File, outputFile: File, inputBaseDir: String)

    /**
     * 是否是一个可以编织的 class
     * @return true 表示是一个可以编织的 class
     */
    fun isWearableClass(className: String): Boolean

    fun parsingOptions(): Int

    /**
     * 创建类编辑器
     */
    fun createClassVisitor(classVisitor: ClassVisitor): ClassVisitor
}