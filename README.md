
###1 Introduction
TSParser is a cross-platform powerful DVB / MPEG stream analyzer program.
It works on Windows, Linux, Mac OS X.
![introduce](https://cloud.githubusercontent.com/assets/8717254/6841219/94298906-d3c1-11e4-9fcb-225089ada675.png)


###2 Features
####2.1 Web based application
TSParser is a web-based application, you can install it on a server, the other client to access the application through a browser. [Main](https://cloud.githubusercontent.com/assets/8717254/6841100/dc949af2-d3bf-11e4-9f97-6208ba18921a.png)
####2.2 Easy configue PID/Table ID
User cans custom PID filter for runtime used.PMT filter will be disabled if PAT is disabled.Decrease filter will cause parser faster.[system_setting_pid](https://cloud.githubusercontent.com/assets/8717254/6841193/144da38e-d3c1-11e4-8c6b-685dad7f58de.png)

User can custom Table ID filter for runtime used,User can enabled/disable a special Table for parser used.
[system_setting_tid](https://cloud.githubusercontent.com/assets/8717254/6841213/61f5b6a8-d3c1-11e4-9db0-484645f1c764.png)

####2.3 Easy add new Secion/Descriptor syntax
TSParser use Section/Descriptor syntax to parse Section/Descriptor.
TSParser has the ability to modify the Section & Descriptor syntax at runtime.
Change the syntax makes analysis results at the same time change.
Syntax file locate: %INSTALL_DIR%\syntax, User can modify these syntax files outside system.

We already integrated bellow SPEC,The main job is to copy syntax from SPEC and paste it into the system
  * 13818
  * 300468
  * 102809
  * 102812
  * Dbook
  * 101202
  * polsat_stb


####2.4 Dynamic load Section/Descriptor 
We have Lexer and Parser, which can help us to recognize the Section/Descriptor scripts.(Thanks [Antlr](http://www.antlr.org/))

We translate the script into java source code , compile java code to byte code, dynamic load byte code into JVM at runtime. (Thanks [Janino](http://docs.codehaus.org/display/JANINO/Home))

####2.5 Friendly presentation of Section/Descriptor 
For a special section we have 3 methods to view the section/descriptor data.
* [Tree view](https://cloud.githubusercontent.com/assets/8717254/6841486/65445c56-d3c6-11e4-99a0-d1c01e6e5db5.png)
* [Text view](https://cloud.githubusercontent.com/assets/8717254/6841499/98f1e276-d3c6-11e4-9010-7c1e4e81f169.png)
* [Raw data view](https://cloud.githubusercontent.com/assets/8717254/6841507/b7a141c6-d3c6-11e4-9eb3-342c54311e31.png)
* [Syntax view](https://cloud.githubusercontent.com/assets/8717254/6841517/d6a27572-d3c6-11e4-90a4-688382fda603.png)


####2.6 Export SI/SPI data to file
Usually, the size of bitstream is very large, using tool we can export only the SI/SPI data,the size is small because AV data is ignored. It's very usefull at field try.

####2.7 Support 3rd add new application in java 
User can write 3rd application in java, please using Java1.5 style. 3rd Application Locate %INSTALL_DIR%/3rd/src. When system startup these applications will be auto registered into system. 

There are 3 build-in applications in system
* Service List application base on SDT(Show service list )
* EPG Application base on EIT (show EPG)
* Demo Application (simple demo)


####  


###3 LICENSE
TSParser is licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.html), meaning you
can use it free of charge, without strings attached in commercial and non-commercial projects. 

###4 Explanation
Most of the code is open source except for Lexer and parser part.
These parts was compiled in tsp_core.jar.



