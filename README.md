# SlideContainer
仿抖音右滑清屏，左滑列表展示

## 效果图
![demo](https://github.com/fxfSean/SlideContainer/blob/master/images/video.gif)

## 引入

在app module 下引入

``implementation 'com.github.fxfSean:SlideContainer:1.0'``

## 使用

```
// 添加需要清屏的view
fun addClearViews(vararg views: View?)
```

```
// 添加需要滑入的view
fun addSlideView(view: RightSlideLayout)
```

### 感谢

主要参考了 [clearscreen](https://github.com/lmxjw3/clearscreen) 的滑动逻辑，但是由于我们项目中的主布局也是可以上下滑动的，所以添加右侧滑动列表后，添加了很多解决冲突的代码逻辑，这部分还需要大家使用过程中根据自己项目的实际情况有针对性的修改，其实只要理解了事件传递的原理，一切问题就是调试的过程而已了。



详细文章参考 [博客地址](https://blog.csdn.net/qq_33224517/article/details/105935321)

