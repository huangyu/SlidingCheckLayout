# SlidingCheckLayout
滑动选择布局，用于嵌套RecyclerView实现滑动选择功能

文章：https://huangyu.github.io/archives/2720d64c.html

引用方式：[![](https://jitpack.io/v/huangyu0522/SlidingCheckLayout.svg)](https://jitpack.io/#huangyu0522/SlidingCheckLayout)

在根目录：

```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
    }
}
```

添加依赖：
```
dependencies {
    ...
    implementation 'com.github.huangyu:SlidingCheckLayout:x.x.x'
}
```
最新版本请查看release。

使用方法：

在layout的xml中，在RecyclerView外层嵌套SlidingCheckLayout作为唯一子节点。

在App中实现SlidingCheckLayout.OnSlidingCheckListener接口即可，具体可参考Demo工程。
