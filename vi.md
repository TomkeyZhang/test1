# 可视化埋点主要功能

各个端主要工作：

## android端
* 客户端WebSocket实时上报手机界面截图和控件信息（debug版）
* 根据服务端动态配置监听指定控件点击事件，并按一定策略上报（通用版）

## 服务端
* 服务端WebSocket实时将手机信息同步给H5页面
* 存储H5配置好的事件信息，提供接口将配置信息返回给app
* 存储事件数据，并提供以获取不同形式数据的接口

## H5
* 同步展示手机界面及可配置事件的元素，点击元素可配置事件。参考设计页面
  + 连接手机页
  ![](https://github.com/TomkeyZhang/test1/raw/master/vi1_ready2connect.png)
  + 确认连接页
  ![](https://github.com/TomkeyZhang/test1/raw/master/vi2_connect_accept.png)
  + 已连接页
  ![](https://github.com/TomkeyZhang/test1/raw/master/vi3_connected.png)
  **说明：** 支持配置click和pv事件，已配置事件支持修改名称和删除，支持按指定类型查看
  + 配置事件页
  ![](https://github.com/TomkeyZhang/test1/raw/master/vi4_config_event.png)
  **说明：** 点击按钮设置click事件，点击其他空白设置pv事件
  
* 图形化的方式展示事件的变化趋势
  + 事件统计页
  ![](https://github.com/TomkeyZhang/test1/raw/master/vi_statistic.png)
  **说明：** 按天统计事件数、事件达成用户数、每活跃用户发生率和每启动发生数，以图形和表哥的方式呈现

* 参考网站 [talkingdata](https://www.talkingdata.com/spa/app-analytics/#/productCenter)，帐号TomkeyZhang@gmail.com，密码：imdada0517


# 开发时间表
## android
* 完成抓取手机界面截图和控件信息(2018-05-18)
* 完成根据配置监听事件及上报日志（2018-05-25）
* WebSocket前后端联调完成（2018-06-01）
* 完成全流程自测联调（2017-06-15）

## 服务端

## H5


### 接口设计：
* WebSocket屏幕上报接口(all)：

```java
{
  "type":"snapshot",
  "content":{
    "activity":"com.sensorsdata.analytics.android.demo.MainActivity",
    "views":[
      {
        "className":"android.widget.FrameLayout",
        "idName":"fl_publish",
        "top": 210, 
        "left": 0, 
        "width": 1080, 
        "height": 1710
      }
      ],
    "imageHash":"785C4DC3B01B4AFA56BA0E3A56CE8657",
    "screenshot": "iVBORw0KGgoAAAANSUhEUgAAAZsAAALbCAIAAACjSrpeAA..."
  }
}
```

* 保存元素及事件关键信息api（web、api）：
	+ activity：View所在的Activity类名
	+ event_type：事件类型，例如点击事件
	+ event_name：事件名称
	+ view:包括上面views数组单个对象的全部信息
	+ screenshot：屏幕截图
	+ platform：Android,Ios,Web
 
* app获取事件配置（app、api）：
 
```java
{
  "status":"ok"
  "content":[{
    "activity":"com.sensorsdata.analytics.android.demo.MainActivity",
    "events":[{
       "id":1,
       "type":"click",
       "view":{
           "className":"android.widget.FrameLayout",
           "idName":"fl_publish"
        }
    }]
  }]
}
```

* app上报事件（包含启动事件、点击事件）数据（app、api）：
 
```java
{"events":[{
    "id":1,
    "type":"click"
    "createdTime":1223333444332
  }],
	"appName":"i-dada",
	"deviceId":"sd2321xssss",
	"appVersion":"8.1.0",
}
```



