### 1 LICENSE


TSParser is licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.html), which means user can use it for free, and without strings attached in commercial or non-commercial projects except [Mediatek](https://www.mediatek.com/). 

[Mediatek](https://www.mediatek.com/) and its subsidiaries forbidden to use this software.


### 2 Introduction
TSParser is a powerful cross-platform DVB/ MPEG stream analyzer. 
It works well under Windows, Linux, and Mac OS.
![introduce](https://cloud.githubusercontent.com/assets/8717254/6841219/94298906-d3c1-11e4-9fcb-225089ada675.png)


### 3 Features
#### 3.1 Web based application
TSParser is a web-based application. User can install it on a server, and the client will access the application via a browser. 
[Main](https://cloud.githubusercontent.com/assets/8717254/6841100/dc949af2-d3bf-11e4-9f97-6208ba18921a.png)
#### 3.2 Easy to configure PID/ Table ID
User can customize the PID filters easily for runtime usage. Notice that PMT filter will be disabled if PAT is disabled. Besides, decreasing the number of filters can make parser faster. [system_setting_pid](https://cloud.githubusercontent.com/assets/8717254/6841193/144da38e-d3c1-11e4-8c6b-685dad7f58de.png)

Table ID filters can also be customized. User can enable/disable a special Table when parsing a stream.
[system_setting_tid](https://cloud.githubusercontent.com/assets/8717254/6841213/61f5b6a8-d3c1-11e4-9db0-484645f1c764.png)

#### 3.3 Easy to add new Section/ Descriptor syntax
TSParser uses Section/Descriptor syntax to parse sections/ descriptors. It has the ability to let user modify the Section & Descriptor syntax at runtime. Once the syntax is changed, the stream’s analysis result will be updated meanwhile. Syntax files locate under %INSTALL_DIR%\syntax. User can also modify these syntax files outside of the system.

TSParser has already integrated SPEC listed as below. The main work is to copy syntax from SPEC and paste it into the application.

  * 13818
  * [ETSI EN 300 468](http://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.11.01_60/en_300468v011101p.pdf)
  * [ETSI TS 102 809](http://www.etsi.org/deliver/etsi_ts/102800_102899/102809/01.01.01_60/ts_102809v010101p.pdf)
  * [ETSI TS 102 812](http://www.etsi.org/deliver/etsi_ts/102800_102899/102812/01.02.01_60/ts_102812v010201p.pdf)
  * DTG D-Book
  * [ETSI TR 101 202](http://www.etsi.org/deliver/etsi_tr/101200_101299/101202/01.02.01_60/tr_101202v010201p.pdf)
  * Polsat_stb
  * DVB-SI Extension For TURKSAT


#### 3.4 Dynamically load Section/ Descriptor script
TSParser has both Lexer and Parser, which can help recognize the Section/ Descriptor scripts.  <br>
(Thanks [Antlr](http://www.antlr.org/))

TSParser translates the scripts into java source code at first, then to compile java code into byte code, finally to dynamically load byte code into JVM at runtime. <br>(Thanks [Janino](http://janino-compiler.github.io/janino/))


#### 3.5 Friendly presentation of Section/Descriptor 
For a specific section, TSParser has three methods to show the section/descriptor data.
* [Tree view](https://cloud.githubusercontent.com/assets/8717254/6841486/65445c56-d3c6-11e4-99a0-d1c01e6e5db5.png)
* [Text view](https://cloud.githubusercontent.com/assets/8717254/6841499/98f1e276-d3c6-11e4-9010-7c1e4e81f169.png)
* [Raw data view](https://cloud.githubusercontent.com/assets/8717254/6841507/b7a141c6-d3c6-11e4-9eb3-342c54311e31.png)
* [Syntax view](https://cloud.githubusercontent.com/assets/8717254/6841517/d6a27572-d3c6-11e4-90a4-688382fda603.png)


#### 3.6 Export SI/SPI data to file
Usually, the size of stream is very large. It costs time to transfer the stream for analysis. TSParser supports a way to export only SI/ PSI data. It can be very useful since the size of SI/ PSI data becomes very small while audio/ video data is ignored. Especially, it makes quite convenient when field try.

#### 3.7 Support 3rd to add new application in Java 
User can write third party applications based on stream data in Java with Java1.5 style. Third party Applications locate in %INSTALL_DIR%/3rd/src. When TSParser startups, these applications will be auto registered into the system.

There are 3 build-in applications in TSParser:
* Service List application base on SDT(Show service list )
* EPG Application base on EIT (show EPG)
* Demo Application (simple demo)


### 4 Run and Build
Please ensure jre1.6 or above is intalled.<br>
```bash
java -version
```

Run from command:
```bash
cd TSP_DIST
java -jar jetty-runner-8.1.9.v20130131.jar --port 8080  TSP.war
```
Or you can Run startup.bat or startup.sh base on your OS

When you see console output "AbstractConnector:Started SelectChannelConnector@0.0.0.0:8080"
That mean program started, using browse visitor "http://localhost:8080/TSP"

Run from eclipse RAP IDE please see file "HowToRun" for detail

### 5 Explanation
Most codes are open source except for Lexer and Parser which are compiled in tsp_core.jar.

