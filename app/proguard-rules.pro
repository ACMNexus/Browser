-dontshrink
#屏蔽警告，脚本中把这行注释去掉
-ignorewarnings
#指定代码的压缩级别
-optimizationpasses 10
 #是否使用大小写混合
-dontusemixedcaseclassnames
#是否混淆第三方jar
-dontskipnonpubliclibraryclasses
# 混淆时输出信息
-verbose
# 混淆时是否做预校验
-dontpreverify
# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#保持Activity及子类不要混淆
-keep public class * extends android.app.Activity
# 保持哪些类不被混淆
-keep public class * extends android.app.Application
# 保持哪些类不被混淆
-keep public class * extends android.app.Service
# 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver
# 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider
# 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper
# 保持哪些类不被混淆
-keep public class * extends android.preference.Preference
# 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService


-keepclasseswithmembernames class * {
    # 保持自定义控件类不要被混淆
    public <init>(android.content.Context);
    # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
    # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# natvie 方法不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持webView不被混淆
-dontwarn android.webkit.WebView

# 保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

#保持json解析的类不被混淆
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

#本地的R类不要被混淆,不然就找不到相应的资源
-keep public class *.R$*{
	public static final int *;
}

# EventBus事件监听中的回调方法不混淆
-keepclassmembers class ** {
    public void onEvent*(**);
}

# 保持枚举类不要被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# umeng 分享不要混淆
-dontwarn com.sina.**
-dontwarn com.umeng.**
-dontwarn com.facebook.**
-dontwarn com.tencent.weibo.sdk.**
-keep public class javax.**
-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep public class com.umeng.socialize.* {*;}
-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
 -keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

 -dontwarn twitter4j.**
 -keep class twitter4j.** { *; }

 -keep class com.tencent.** {*;}
 -keep public class com.umeng.soexample.R$*{
     public static final int *;
 }
 -keep public class com.umeng.soexample.R$*{
     public static final int *;
 }

-keep class com.sina.** {*;}
-keep class com.linkedin.** { *; }
-keep class com.alipay.share.sdk.** { *; }
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}


#保持签名属性不要被混淆
-keepattributes Signature
#注解属性不要被混淆
-keepattributes *Annotation*
#保持内部类，异常类
-keepattributes Exceptions, InnerClasses
#保持签名、注解、源代码之类的不被混淆
-keepattributes Signature, Deprecated,  SourceFile
-keepattributes LineNumberTable, *Annotation*, EnclosingMethod
#保持v4包下面的类不要被混淆
#Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment