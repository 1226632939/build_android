# gradle

---------------
gradle配置详解
---

buildTypes属性：

子渠道配置 productFlavors：

| 代码 | 信息 |
| :-------- | :-----:  |
| applicationId | 配置包名 |
| signingConfig | 配置签名信息 |
| manifestPlaceholders | 配置manifest中的meta字段 |
| javaCompileOptions | 可以单独配置 java信息 |
```groovy
    子渠道缩写{ 
        applicationId "com.bairimeng.dmmdzz"
        signingConfig signingConfigs.snakeV2
        manifestPlaceholders = [CHANNEL_NAME:"MOMOYU", CHANNEL_TYPE:rootProject.ext.AndroidChannel.NEWMOMOYU]
        multiDexEnabled true
        javaCompileOptions {
            compileOptions {
                sourceCompatibility JavaVersion.VERSION_1_8
                targetCompatibility JavaVersion.VERSION_1_8
            }
        }
    }
```
资源配置 sourceSets

| 代码 | 信息 |
| :-------- | :-----:  |
| srcFile | 设置aidl文件的目录  |
| srcDirs | 设置资源文件的目录  |


repositories

| 代码 | 信息 |
| :-------- | :-----:  |
| flatDir | 告诉gradle，编译中依赖的jar包存储在dirs指定的目录  |
   