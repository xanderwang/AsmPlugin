package com.xander.plugin

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension

interface IBasePlugin {

  Transform applyCustomTransform(AppExtension android)
}
