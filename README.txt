===============================================================================
      README.TXT - IPDS (GRID CONTROL + ONLINE AUCTIONING)
===============================================================================

   Intelligent Power Distribution System (IPDS)
   PhD Software Engineering Project
   Kansas State University
   
   Holonic multiagent architecture for cyber-physical systems.
   
   The IPDS simulation is being used to evaluate power quality control algorithms 
   for electrical power distribution systems with significant amounts of 
   potentially-intermittent photovoltaic renewable generation sources.

   Unless required by applicable law or agreed to in writing, software
   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.

   Denise Case
   dmcase@ksu.edu
   denisecase@gmail.com
   
   Some visualizations produced during the design process are available at:
   http://people.cis.ksu.edu/~dmcase/designing_complex_systems/index.html


===============================================================================
       ABOUT THIS PROJECT
===============================================================================
   This project provides a cyber-physical system (CPS) architecture for a complex
   multiagent system (MAS) for the Intelligent Power Distribution System (IPDS)
   project at K-State, a multi-year, multi-disciplinary effort involving
   researchers from the Computational Informations Systems Department, the
   Electrical and Computer Engineering Department, and others.
   http://ipds.cis.ksu.edu

   The project is based on the OBAA architecture developed previously at
   the K-State MACR lab and reflects contributions by:
   
   Dr. Scott DeLoach                    sdeloach@ksu.edu
   Dr. Scott Harmon                     harmon@ksu.edu
   Dr. Chris Zhong                      czhong@ksu.edu
   Rui Zhuang, PhD Candidate            zrui@ksu.edu
   Donald Lee, MSE Student              mieux@ksu.edu
   Joshua Wurtz, MS Student             wurtzjoshua@gmail.com
   Matthew Brown, MACR Java Developer   mbrown33@ksu.edu
   Greg Martin, MACR Java Developer     gregm@ksu.edu
   
===============================================================================
      DEPENDENCIES - DEVELOPMENT
===============================================================================

Install the following development software:

  JDK 1.8 from
    http://www.oracle.com/technetwork/java/javase/downloads/index.html.
    Set your JAVA_HOME environment variable to 
    C:\Program Files\Java\jdk1.8.0_05 (or your jdk location)
    Add  %JAVA_HOME%\bin to your path.
    I installed jdk-8u5-windows-x64.exe
    to :\Program Files\Java\jdk1.8.0_05.
    
  RabbitMQ (including the required Erlang Windows Binary, 64-bit) from
     https://www.rabbitmq.com/install-windows.html
     
     along with the RabbitMQ Management Plugin from
     https://www.rabbitmq.com/management.html
     
     
  
===============================================================================
      Integrated Development Environment (IntelliJ or Eclipse)
===============================================================================
   
Install at least one IDE.

  I recommend IntellijIDEA 13 or later with support for Java 1.8.
    Free community version: 
    http://www.jetbrains.com/idea/
    Or free fully-functional classroom version:
    http://www.jetbrains.com/idea/buy/choose_edition.jsp?license=CLASSROOM.
    Intellij integrates with our other option, Eclipse.  
 
  Eclipse Kepler SR2: Standard 4.3.2 with Java 8 support
    http://www.eclipse.org/downloads/index-java8.php
    eclipse-standard-kepler-SR2-Java8-win32-x86_64.zip
    I extracted the contents to:
    C:\eclipse-standard-kepler-SR2-Java8-win32-x86_64
    Note: Java and Eclipse must be be either BOTH 32-bit OR BOTH 64-bit.
    The modified eclipse.ini file is included below. 

  Then through "new software":
    Java8 Support: 
    http://download.eclipse.org/eclipse/updates/4.3-P-builds/
	
The following Eclipse plugins are used for modifying goal & role 
models and for running Spock tests, respectively:
	
    AgentTool3 Eclipse plugin (Core and Process Editor) from
        http://agenttool.cis.ksu.edu/update/
        
   Groovy Eclipse plugin from 
        http://dist.springsource.org/release/GRECLIPSE/e4.3/
        
        
        
===============================================================================
      ECLIPSE WORKSPACE FOLDERS
=============================================================================== 
  Eclipse uses workspace folders to hold IDE configuration information that 
  we do not want shared across machines. Our current location is a 
  sub folder of the associated workspace. See:
  http://eclipse.dzone.com/articles/eclipse-workspace-tips
  for more information. 
        
===============================================================================
      SOURCE CODE
===============================================================================        

Install Git for source control.  

   I use Git for Windows from 
   http://msysgit.github.io
   and TortoiseGit from
   http://code.google.com/p/tortoisegit.
   
        
===============================================================================
       QUICK START
===============================================================================
	
Check out code.
	Go to a folder where you want to keep your code, say a "projects" folder.
	Use Git to clone the code from https://ksucase@bitbucket.org/ksucase/ipds.git.
	This will create a new folder called "ipds".
   
Create an Eclipse project.
	Open Eclipse and click File / New / Project / Java Project. 
	Enter the Project name: ipds  
	Uncheck Use default location and find your ipde
	File / Import / General / File system / Select your ipds dir and click OK.
	Check the ipds box and click Finish to import.
		
Start RabbitMQ.
	Open a command window as administrator. From
	C:\Program Files (x86)\RabbitMQ Server\rabbitmq_server-3.2.1\sbin	
    type "rabbitmq-service start" and hit Enter.
	    rabbitmq-server.bat starts the broker as an application.
	    rabbitmq-service.bat manages the service and starts the broker.
	    rabbitmqctl.bat manages a running broker.
	
	Point a browser to (the final slash is required): http://localhost:15672/
	Login with:  guest / guest
  
Start simulation.
	In Eclipse, under src/main/java/
	Right-click on edu.ksu.cis.macr.ipds.Launcher.java
	and Run as Java Application.
     
Verify display.
	When everything is running successfully, you'll see screens appear showing
	agents and assigned tasks.
		
Stop rabbitmq with:
    rabbitmq_service stop


===============================================================================
       RabbitMQ for Messaging 
===============================================================================  

RabbitMQ 3.1.5 and the management plugin from
   http://www.rabbitmq.com/install-windows.html/
   Requires Erlang: http://www.erlang.org/download.html
   Set RABBITMQ_SERVER environment variable and add to PATH.
   Open a command window as administrator and type the following commands from
   C:\Program Files (x86)\RabbitMQ Server\rabbitmq_server-3.2.1\sbin:
      rabbitmq-plugins enable rabbitmq_management  
      rabbitmq-server start

To delete all queues and start completely fresh after running, use:
  rabbitmqctl stop_app
  rabbitmqctl reset
  rabbitmqctl start_app
                 
===============================================================================
      MatLab for simulated sensor data and power flow analysis
===============================================================================                 
  
  A local version of MatLab is required.  We have licenses for use at K-State or
  you can get a student license for $99:
  https://www.mathworks.com/programs/nrd/buy-matlab-student.html?s_eid=ppc_3302

===============================================================================
     Gradle build tool (optional)
===============================================================================    
   
Completely updating the project involves a variety of tasks:
     - Deleting the compiled artefacts and starting a fresh build.
     - Compiling Java, Groovy, and Scala files.
     - Run the Java JUnit and Groovy Spock tests to be sure everything works.
     - Update the JavaDocs, GroovyDocs, and ScalaDocs.
     - Create a new executable jar file.
   There are a variety of good build tools that will automate those tasks (and more).
      Ant or Maven are examples of build tools.  Our current choice
      is Gradle.  It'stringBuilder easy to get started, it allows us to
      remove many of the standard library jar files from our IPDS 
      src/main/resources/lib folder, and it offers a quicker, standard
      way of checking out the IPDS project and getting up to speed.  
   Download and install gradle from http://www.gradle.org/.
   Set GRADLE_HOME environment variable to C:\gradle-1.10
   and include %GRADLE_HOME%/bin in your Windows Environment path variable.
   Open an Administrator Command Window in the root ipds folder, and type:  
                 gradle clean build assemble

===============================================================================
       MAIN FILES - HOW TO RUN 
===============================================================================

This module includes the following main files:

  ipds\src\main\java\edu\ksu\cis\macr\ipds\Launcher.java:
    This is the main program that runs IPDS to test various control simulations.
    In Eclipse, right-click the file and say "Run as Java Application"
    If you get an error about rabbitMQ, follow the instructions above. 
	
  ipds\run.properties:
    This file defines the test case (e.g. TC62) and other information to 
    configure the run.

  README.txt:
    This readme file. 	   
	   

===============================================================================
       IPDS CONFIGURATION FILES
===============================================================================
		
The following configuration files are required:

	Agent.xm  		- local organization agents and their capabilities.
	Environment.xml - objects in the IPDS environment
	Initialize.xml 	- provides custom goal parameters.
	GoalModel.goal 	- describes the objectives of the system
	RoleModel.role 	- describes  capabilities needed to play roles and
                          roles that agents can play to achieve specific goals.
                          
The utils package has builder programs to assist with auto-generation of the 
many agent and initialize files used in the test cases.

===============================================================================
       EDITING CONFIGURATION FILES
===============================================================================

Two of the configuration files - the goal models (.goal) and the
role models (.role) can be easily edited using the recently updated
agentTool3 modeling plugins.

See http://agenttool.cis.ksu.edu for more information.

===============================================================================
       PROJECT REPOSITORY
===============================================================================

 The following module is required and can be checked out with your Git client
 from https://bitbucket.org/ksucase.  

 The IPDS project includes jar files for:
	The Goal Model for Dynamic Systems (GMoDS).
	The Organization Model for Adaptive Computational Systems (OMACS).

===============================================================================
       ADDING NEW BEHAVIOR
===============================================================================

To add new behavior to the system:

Add goals to goal model(s).
    Use agentTool3 on the models in src/main/resources/configs/standardmodels.
    Include goal(s) under AND or OR.
    Add parameters.
    Make sure parameters get passed from top goal to child goals to triggered goals etc.
    Add triggers, precedes, etc as appropriate.
Add roles to roles model(s).
    Use agentTool3 on the models in src/main/resources/configs/standardmodels.
    Refer the role to the goal and the required capabilities.
    All leaf goals in the goal model must have an associated role in the role model.
Update goal identifiers.
Update goal events.
Update goal parameters.
Create new goal parameter "guideline" classes as needed.
Update GuidelineManager to read guideline information from XML file.
Update role indentifers.
Create new message classes as needed.
Create new capabilities.
Create new plan(s) and plan states.
Update CapabilityUniqueIndentifier provider in agent/persona/process.
Update MasterCapabilityUniqueIndentifier provider in agent/cc_master/process.
Update plan_selector.

Update the Builder utility to add the new capabilities and/or agent types.
Update the Builder utility to create custom agent guidelines.


===============================================================================
       PROGRAMMING GUIDELINES
===============================================================================

	LOG.info should be used to record significant program steps.

	LOG.debug should be used at least once in every public method.

	The ipds.log file is available in ipds\build\tmp\ipds.log.

	Provide JavaDoc comments for all public interface methods -
	implementing classes will inherit them automatically.

	Generally, write code that compiles without warnings (as well as without errors).

===============================================================================
       ADDITIONAL RESOURCES
===============================================================================

 Gradle in Action (Excellent series by Manning) Chapter 2:
   "Next generation builds with Gradle"
    http://www.manning.com/muschko/GradleinAction_CH02.pdf
    
Modifying large numbers of XML files (e.g. in src/main/resources/configs):
Use this Find-And-Replace utility for Windows - simple, super-fast open-source 
tool for finding and replaceing text in multiple files. (fnr.exe)
http://findandreplace.codeplex.com.

===============================================================================
 C:\eclipse-standard-kepler-SR2-Java8-win32-x86_64\eclipse\eclipse.ini
===============================================================================
-startup
plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar
--launcher.library
plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.200.v20140116-2212
-product
org.eclipse.epp.package.standard.product
--launcher.defaultAction
openFile
--launcher.XXMaxPermSize
256M
-showsplash
org.eclipse.platform
--launcher.XXMaxPermSize
256m
--launcher.defaultAction
openFile
--launcher.appendVmargs
-showLocation
-vmargs
-Dfile.encoding=UTF-8
-Dosgi.requiredJavaVersion=1.7
-XX:MaxPermSize=256m
-Xms256m
-Xmx1024m

===============================================================================
  END
===============================================================================