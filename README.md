# 任务列表中显示"小程序"入口的原理分析 

>作者:菜刀文   
>简书:http://www.jianshu.com/u/ccabae3e72f2  
demo: https://github.com/helen-x/NewTaskDemo  

今天被小程序刷屏了^^  我也来凑凑热闹.  
谈谈微信是怎么实现在任务列表中显示"小程序"的. 

## 效果
微信中打开了"滴滴(小程序)"后,   
可以看到,任务列表不仅显示了"微信", 还显示了"滴滴(小程序)"的人口.  
通过这个入口,就可以很方面的切换小程序了, 体验和原生程序也一样一样的. 

![](http://upload-images.jianshu.io/upload_images/4048192-4cc465a56b1ecea0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



## 分析   

下面简单分析一下他的实现.    

1.Android系统中,显示最近程序列表的View是 `RecentsPanelView`.    
  他通过`refreshRecentTasksList()`加载程序列表,我们来看看代码:  
可以看到`RecentTasksLoader mRecentTasksLoader`负责真正处理数据加载.   

```java    
private RecentTasksLoader mRecentTasksLoader;  

***  

private void refreshRecentTasksList(
    ArrayList<TaskDescription> recentTasksList, boolean firstScreenful) {
    if (mRecentTaskDescriptions == null && recentTasksList != null){
      onTasksLoaded(recentTasksList, firstScreenful);
    } else {
      //加载最近的列表
      mRecentTasksLoader.loadTasksInBackground();
    }
  }
```

2.顺着看看RecentTasksLoader的实现:   
    这里可以清楚看到,加载的数据是`ActivityManager.getRecentTasks()`. 
    也就是说显示的是Task列表.

```java   
 ArrayList<TaskDescription>  getRecentTasks() {
       cancelLoadingThumbnails();

       ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();
       final PackageManager pm = mContext.getPackageManager();
       final ActivityManager am = (ActivityManager)
               mContext.getSystemService(Context.ACTIVITY_SERVICE);

       //获取最近的Task
       final List<ActivityManager.RecentTaskInfo> recentTasks =
               am.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        ***
       return tasks;
   }

```


__到这里,已经很清楚了. 要显示"小程序"入口, 只需要新建一个Task启动就好了~  
是不是很简单啊.__   

来来来验证一下我们的想法.    
这里使用 `adb shell dumpsys activity activities`查看一下Task状态   
##### 1. 只开启微信    
![](http://upload-images.jianshu.io/upload_images/4048192-a831dd63c388583c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### 2. 开启小程序    
如下图所示, 微信新开启了一个.AppBrandUI的task栈
![](http://upload-images.jianshu.io/upload_images/4048192-5a0c5c7ec755e2cf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 实现     

知道原理,实现就很简单了.    
>假设小程序的Activity是 TaskTestActivity,整个实现分两步:     

##### 1. AndroidManifest.xml中为Activity设置taskAffinity     

```java  
<activity
        android:icon="@drawable/didi"
        android:name=".TaskTestActivity"
        android:label="小程序测试"
        android:taskAffinity=".NewTask" />    

```  

##### 2. 以NEW_TASK方式启动Activity   

```java   
    //以Intent.FLAG_ACTIVITY_NEW_TASK方式启动Activity
    Intent intent = new Intent(this, TaskTestActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);

``` 

##### 为什么要设置taskAffinity呢,  
这和`FLAG_ACTIVITY_NEW_TASK`的特性有关:
>__FLAG_ACTIVITY_NEW_TASK:__ 设置此状态，首先会查找是否存在和被启动的Activity具有相同的taskAffinity的task（注意同一个应用程序中默认所有activity 的taskAffinity是一样的），如果有，刚直接把这个栈整体移动到前台，并保持栈中的状态不变，即栈中的activity顺序不变，如果没有，则新建一个栈来存放被启动的activity.   

也就是说,如果App已经启动,即使用`FLAG_ACTIVITY_NEW_TASK`新起Activity, 因为taskAffinity相同,也会被压到一个task中, 自然recent panel 就看不到两个入口了.  

所以我们需要为小程序设置一个新的taskAffinity    

### 下面是demo效果   
![](http://upload-images.jianshu.io/upload_images/4048192-52b2129bdbcf56b5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  


## 拓展   

### 让自己的程序不显示在任务列表中      

有时候我们做一个工具, 或者后台界面, 不希望显示在程序列表中.      
也很简单,只要设置task的属性就好了    

##### 方法1:   

在AndroidManifest.xml设置Activity的  `android:excludeFromRecents`为true   
   
```java     
<activity
      android:excludeFromRecents="true"
      android:icon="@drawable/didi"
      android:name=".TaskTestActivity"
      android:label="小程序测试" />
```  

##### 方法2:   

在启动Activity的时候加上`Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS` FLAG    

```java      
//Activity不显示在recent列表中.
Intent intent = new Intent(this, TaskTestActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
startActivity(intent);
``` 


## 更多文章请关注公众号   

![](http://upload-images.jianshu.io/upload_images/4048192-ece9d6c782f566d8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)