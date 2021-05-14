## 微吼课堂SDK  
具体调用方式参考官方文档  地址：https://www.vhall.com/class/doc/1126.html

## 集成方式：

1、获取Demo中libs中的AAR包  
2、将AAR引用自己工程下的App中的Libs 并更新Gradle
3、引入方式修改maven引入  2021/02/01
在项目的gradle文件中添加  
	allprojects {  
    repositories {  
        google()  
        jcenter() 
        maven { url 'https://s01.oss.sonatype.org/content/repositories/releases/'}
    }
}  
在项目的gradle中添加  
添加  api 'com.github.vhall.android.library:vhallyun-class:3.7.0'

## 版本更新信息：
### v3.7.0 更新时间： 20210514
1.远程库更新到3.7.0
2.增加回放+文档demo
3.修复部分已知bug

## 版本更新信息：
### v3.6.3 更新时间： 20210222
远程库更新到3.6.3

## 版本更新信息：
### v3.6.0 更新时间： 20210218
1、消息优化  
2、增加sdk稳定性  
3、传统的aar 引入修改为 maven 引入方式 (见集成方式)

## 版本更新信息：
### V3.0.0 更新时间：20200902
1、兼容h5模式课堂
2、文档模块接口调整（见文档）；

## 版本更新信息：
### V2.1.0 更新时间：20200430
1、升级glide依赖版本到4.9.0；   
2、升级互动功能，支持美颜；  
   
```
//删除stream.setRenderView()；替换方法如下
stream.removeAllRenderView();
stream.addRenderView(renderView);
//美颜设置，仅本地流可用
localStream.setEnableBeautify(true);//默认等级2
//美颜等级设置，建议渲染可见后使用
//localStream.setBeautifyLevel(2);//美颜等级1-4 默认 2
```  
3、升级直播点播播放器，优化观看体验；   
4、接口RequestCallback 位置变更、去掉VHClass 重新引入   
5、其他类位置并更，删除旧引用，重新引用即可
### v2.0.1 更新时间： 20200306
1、修复日志上报数据异常；

### v2.0.0 更新时间： 2019.04.17    
1、更新互动、直播相关底层库；  
2、修复互动偶现崩溃问题；  


### v1.1 更新时间：2018.10.18  
1、微吼课堂更新互动底层库2.0   
2、修复偶现不能拉流的情况  
3、优化细节！

### v1.0 更新时间：2018.09.30

更新内容：  
1、微吼课堂SDK1.0版本包含（直播、回放、互动、聊天、文档/白板等功能）  
2、Demo中有对应功能的演示





