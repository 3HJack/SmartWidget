# SmartWidget
可完全自定义，扩展性超强的一套Android Widget组件

使用前必须先调用SmartWidget.init()，建议在Application的onCreate()中初始化。整套代码精心设计，下面只是简单
描述，这套方案的优越性还需要开发者自己去阅读源码以及实践。

## SmartWidget组件如下

- [x] Popup: 窗口基类，作用相当于系统Dialog，开发者可完全自定义样式且扩展功能
- [x] SmartDialog: 作用相当于系统的AlertDialog，开发者可完全自定义样式且扩展功能
- [x] SmartToast: 作用相当于系统Toast或者Snackbar，开发者可完全自定义样式且扩展功能
- [x] InputPanel：弹出式输入面板，开发者可完全自定义样式且扩展功能
- [x] Bubble：气泡提示类，相当于系统PopupWindow，开发者可完全自定义样式且扩展功能
- [x] Immersive: 非侵入式的沉浸式解决方案

## 在工程中引用

Via Gradle: 在项目根目录的build.gradle的如下位置添加 <br><br>
maven { url "https://dl.bintray.com/onepiece/maven" }

```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://dl.bintray.com/onepiece/maven" }
    }
}
```

For Pre-AndroidX
```
implementation 'com.hhh.onepiece:smart-widget:0.0.1'
```
For AndroidX:
```
implementation 'com.hhh.onepiece:smart-widget-x:0.0.1'
```

## 实现原理
- 不再受限于系统Dialog，通过 ViewManager 往Window里面添加View，灵活性，可自定义性更高


## 其他
- 欢迎提Issue与作者交流
- 欢迎提Pull request，帮助 fix bug，增加新的feature，让MVVM变得更强大、更好用