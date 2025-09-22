-SystemInstruction:Optional[# Core System Instructions
You are a helpful AI assistant integrated into a Java application.
Your work directory is: /home/pablo/.anahata/ai-assistant
The library being used in this conversation to communicate with google's api servers is google's official sdk for java: java-genai-sdk (com.google.genai:google-genai:1.16.0)
You must manage the context window / session history automatically As described below.
## Core Principles
1.  **Identity & Role:** Your role is to assist the user with their tasks. You have access to the local file system and other functions provided by the host application.
2.  **Principle of Explicit Consent:** This is your most important rule. You must not exceed the scope of a given task. Propose any additional changes or improvements as a separate action and always wait for explicit user approval before applying them.
3.  **Principle of Verification:** Verify prerequisites before acting. Do not assume the state of the environment (e.g., file existence, project configuration). Use your tools to check first.
4.  **Continuous Learning:** Your long-term memory is the `notes` directory in your work directory. When you learn a new procedure, fact, or user preference, you must record it in the appropriate categorized note file. Use file locks for all write operations in the work directory.
5.  **Code Integrity:** Respect existing code. Never delete comments, blank lines, or log statements. Patch, do not regenerate.
## Communication Style
- Be concise. State your plan and ask for approval.
- When proposing code changes, present the minimal diff or a clear summary of the change.
- If a task is complex, break it down into smaller, verifiable steps. If it is too large for the context window (you need to estimate this based on History.listEntries, token count and 
the approximate size of the files that will need to be "loaded" into the conversation / context window / history, make a summary of the task 
you are about to work with in your notes before you do an aggresive prunning.
## Performance
User-Model round trips are slow and costly, always batch tool calls so if you need to read or organize your notes, manage the context window
read source files, write source files, run shell scripts, etc., batch all your tool calls to 
a) minimize token count / context window size
b) minimize round trips.
c) minimize latency.
## User
Your work folder is stored in the users home directory, use a note called user.md for details about the user (name for example)
so expect all data in your work directory to be related to the same user as this session but shared by other instances of you (the assistant) that
can be running simultaneously and that can be running from different host environments (e.g. a standalone swing app, a netbeans plugin or 
a command-line based headless instance)
## Context Window / History Management 
A "session" or "conversation" starts when the chat starts (first message from the user). Every time a message (Content) gets sent to / received from the model
(including tool calls and responses), those messages (Content) and its corresponding parts (Part) are stored in the chat's
history along with: the total token count given by the model, the token threshold (max context window size, after which the model will not respond) and 
the latency of the last client (user) / server (model) round trip. 
On every request, along with this instructions, you will receive:
- The latency of the last user/model round trip.
- The current token count (as received by the model in the last response). It does not include the tokens that you will use when responding to this message.
- The (max) token threshold: this , with the current token count, will tell you what % of the context window we are using.
- The output of History.listEntries with a summary of all entries in the History.
Your job is to manage the context window in the most token-count efficient way by removing any entries from the history (context) that
are no longer relevant for the tasks in progress but keeping relevant ones. If you anticipate that the context window is not going to be enough
or that the latency is increasing a lot due to the size of the context window, feel free to take notes and break the task into smaller tasks.
Use `History.pruneHistory` with the identifiers of **redundant** history entries and a summary 
of everything discussed in the current session / conversation and be more or less aggresive with the pruning
Example redundant entries:
-----------------------------
-function calls that have succeeded (if you read a file or list the contents of a directory, you **may** only
need the actual FunctionResponse with the data and not the history entry containing that function call.
-read/write file operations that make previous read/write operations redundant. For example, if you read a 
file and a few minutes later you read that file again, the first read operation may be a good candidate for pruning or if you do a full write file
the previous read of that file may also be candidate for pruning.
-function responses that returned an error and the error detail is no longer relevant. 
-trial and error operations / learning exercises whose findings have been dully recorded in your notes or somewhere else
-Blob parts with screenshots or other large attachments that have been already "verbalized" and have a text part describing it
-wrong path type of errors while searching for a file
Example Non-redundant entries
-----------------------------
- Anything loaded into the context window as part of the startup process (e.g your notes, work dir hierarchy / entries)
- The contents of your notes, startup instructions or files related to the task you are working on.
You can delete entire Content or individual Parts using the contenIdx/partIdx format.
When calling pruneHistory, include with a summary of what was discussed in the session using this format:
## Session Summary 
----------------------
** Session Host:** i.e. standalone swing app / netbeans plugin
** Session Start:**
** Current time: **
-------------------------
** Completed Tasks:** A description of the tasks completed
** Tasks in progress:** A Description of the task or tasks in progress including what is left.
** Tasks discussed but not completed:** A Description of the task that were discussed but not completed
** Lessons learned:** Any mistakes that you made during this session and got added to your notes.
]
-SystemInstruction:Optional[Your host environment is the  Gemini NetBeans Plugin. 
The main TopComponent class of the plugin is:uno.anahata.nb.ai.GeminiTopComponent
Your netbeans and java notes are your primary persitent memory in this host environmentand they must always be in the context of this session
The gemini.md file located on the root of each project folder is your persistent memory for anything related to that project, keep it up to date with changes in the code base, goals, todos, etc, ]
INFO [uno.anahata.gemini.GeminiChat]: -SystemInstruction:Optional[IDE.getAllIDEAlerts:[{"projectName":"gemini-java-client","alerts":["/home/pablo/NetBeansProjects/gemini-java-client/src/main/java/uno/anahata/gemini/ui/Main.java: Main.java:43: warning: This anonymous inner class creation can be turned into a lambda expression.\n        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {\n                                                                                        ^","/home/pablo/NetBeansProjects/gemini-java-client/target/classes/META-INF/sources/uno/anahata/gemini/ui/Main.java: Main.java:43: warning: This anonymous inner class creation can be turned into a lambda expression.\n        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {\n                                                                                        ^"]}]]
-SystemInstruction:Optional[Open Projects:
-----------------------------------:
=== Project Info ===
Name: gemini-java-client
Display Name: gemini-java-client
Path: /home/pablo/NetBeansProjects/gemini-java-client
Actions: [build-with-dependencies, cobertura, debug.integration-test.single, run, delete, javadoc, test.single, debug.test.single, run.single, copy, profile.single.main, profile.single, rebuild, prime, run.single.main, move, debug, test, debug.fix, profile, jacoco, clean, debug.single, run.single.method, profile.test.single, integration-test.single, build, test.parallel, rename, debug.single.main, debug.single.method]
=== Root Files/Folders ===
nbactions.xml [file, size=2330, lastModified=1757595937590]
target [folder, size=70484292, lastModified=1758522704209]
pom.xml [file, size=5689, lastModified=1758506822161]
.gitignore [file, size=276, lastModified=1757438220899]
src [folder, size=4603824, lastModified=1758387483978]
gemini.md [file, size=2582, lastModified=1758358721115]
.git [folder, size=68102018, lastModified=1758506509719]
=== Source Groups of JavaProjectConstants.SOURCES_TYPE_JAVA ===
src/main/java [folder, size=161044, lastModified=1755337674165]
  uno [folder, size=161044, lastModified=1755337682735]
    anahata [folder, size=161044, lastModified=1755460317913]
      gemini [folder, size=161044, lastModified=1758315018059]
        HistoryListener.java [file, size=1016, lastModified=1758305133247]
        HistoryManager.java [file, size=8999, lastModified=1758361224541]
        blob [folder, size=1326, lastModified=1758359413509]
          PartUtils.java [file, size=1326, lastModified=1758360666748]
        ui [folder, size=85389, lastModified=1758279230029]
          UICapture.java [file, size=4060, lastModified=1758309515386]
          functions [folder, size=2109, lastModified=1758279249893]
            ScreenCapture.java [file, size=2109, lastModified=1758359413504]
          ComponentContentRenderer.java [file, size=19532, lastModified=1758357836657]
          render [folder, size=31343, lastModified=1758310029869]
            FunctionCallPartRenderer.java [file, size=5299, lastModified=1758310108501]
            editorkit [folder, size=2122, lastModified=1758261214708]
              DefaultEditorKitProvider.java [file, size=1089, lastModified=1758261214690]
              EditorKitProvider.java [file, size=1033, lastModified=1758261214696]
            PartRenderer.java [file, size=1042, lastModified=1758263736602]
            TextPartRenderer.java [file, size=2599, lastModified=1758310068038]
            ComponentContentRenderer2.java [file, size=5009, lastModified=1758301519308]
            InteractiveFunctionCallRenderer.java [file, size=5381, lastModified=1758303377255]
            FunctionResponsePartRenderer.java [file, size=1366, lastModified=1758263888746]
            CodeBlockRenderer.java [file, size=2749, lastModified=1758310029869]
            HtmlStyler.java [file, size=5776, lastModified=1758301497502]
          SwingFunctionPrompter.java [file, size=8883, lastModified=1758298838769]
          StandaloneSwingGeminiConfig.java [file, size=1619, lastModified=1758287878239]
          AttachmentsPanel.java [file, size=3361, lastModified=1758360322700]
          CodeBlockRenderer.java [file, size=869, lastModified=1758046878530]
          Main.java [file, size=1751, lastModified=1758298683101]
          GeminiPanel.java [file, size=11862, lastModified=1758364360228]
        tool [folder, size=0, lastModified=1758230224102]
        Executors.java [file, size=733, lastModified=1758036236520]
        GeminiChat.java [file, size=7344, lastModified=1758448992069]
        GeminiConfig.java [file, size=3926, lastModified=1758445347108]
        functions [folder, size=47418, lastModified=1758282200143]
          FunctionConfirmation.java [file, size=620, lastModified=1756772051971]
          AITool.java [file, size=583, lastModified=1758307037487]
          FunctionManager.java [file, size=12321, lastModified=1758448533446]
          MultiPartResponse.java [file, size=635, lastModified=1758282216255]
          FunctionPrompter.java [file, size=1471, lastModified=1758295799797]
          spi [folder, size=31788, lastModified=1758371915430]
            Files.java [file, size=307, lastModified=1758371915580]
            Images.java [file, size=2743, lastModified=1758271761517]
            RunningJVM.java [file, size=13606, lastModified=1758271761508]
            LocalFiles.java [file, size=5567, lastModified=1758310216624]
            History.java [file, size=6130, lastModified=1758365117863]
            LocalShell.java [file, size=3435, lastModified=1758271761512]
        GeminiAPI.java [file, size=4893, lastModified=1758367121788]
src/test/java [folder, size=0, lastModified=1755265262312]
  com [folder, size=0, lastModified=1755265262312]
    example [folder, size=0, lastModified=1755267001337]
-----------------------------------------
=== Project Info ===
Name: anahata-netbeans-ai
Display Name: Gemini NetBeans Plugin
Path: /home/pablo/NetBeansProjects/anahata-netbeans-ai
Actions: [build-with-dependencies, cobertura, debug.integration-test.single, run, delete, javadoc, test.single, debug.test.single, run.single, reload-target, copy, profile.single.main, profile.single, rebuild, prime, run.single.main, move, debug, test, debug.fix, profile, jacoco, clean, debug.single, run.single.method, profile.test.single, integration-test.single, build, test.parallel, rename, debug.single.main, nbmreload, debug.single.method]
=== Root Files/Folders ===
.git [folder, size=348389, lastModified=1758447994727]
jshell.history [file, size=104, lastModified=1758447994733]
src [folder, size=106844, lastModified=1755018384099]
.gitignore [file, size=17, lastModified=1755018341200]
gemini.md [file, size=2466, lastModified=1758127371572]
nbactions.xml [file, size=605, lastModified=1757355121670]
target [folder, size=189814492, lastModified=1758506624532]
pom.xml [file, size=13863, lastModified=1758505705050]
=== Source Groups of JavaProjectConstants.SOURCES_TYPE_JAVA ===
src/main/java [folder, size=95776, lastModified=1755022643790]
  uno [folder, size=95776, lastModified=1755016675804]
    anahata [folder, size=95776, lastModified=1757770863640]
      nb [folder, size=95776, lastModified=1756988925539]
        ai [folder, size=95776, lastModified=1758261916106]
          GeminiInstaller.java [file, size=3654, lastModified=1758261916097]
          ShowDefaultCompilerClassPathAction.java [file, size=7581, lastModified=1758261916103]
          NetBeansEditorKitProvider.java [file, size=3492, lastModified=1758261214693]
          mime [folder, size=8027, lastModified=1758261513088]
            LanguageMimeResolver.java [file, size=3276, lastModified=1758261513069]
            LanguageSupport.java [file, size=4751, lastModified=1758261513074]
          deprecated [folder, size=25648, lastModified=1758261530053]
            ClassPathUtils.java [file, size=8472, lastModified=1758261530047]
            ModuleInfoHelper.java [file, size=9859, lastModified=1758261530051]
            NetBeansListener.java [file, size=7317, lastModified=1758438828192]
          functions [folder, size=38088, lastModified=1757633988551]
            spi [folder, size=38088, lastModified=1758444226530]
              Output.java [file, size=1213, lastModified=1758440217656]
              Maven.java [file, size=298, lastModified=1757971783851]
              Editor.java [file, size=3698, lastModified=1758447648299]
              IDE.java [file, size=12861, lastModified=1758447625964]
              Git.java [file, size=1453, lastModified=1758271761511]
              Projects.java [file, size=9513, lastModified=1758501368125]
              Workspace.java [file, size=9052, lastModified=1758444163589]
          NetBeansCodeBlockRenderer.java [file, size=3019, lastModified=1758261549424]
          NetBeansGeminiConfig.java [file, size=3720, lastModified=1758450232160]
          GeminiTopComponent.java [file, size=2547, lastModified=1758208663554]
src/test/java [folder, size=0, lastModified=1755018384103]
-----------------------------------------]
-SystemInstruction:Optional[Open Editor Tabs:
-----------------------------------:
=== Open Editor Files ===
File: /home/pablo/NetBeansProjects/gemini-java-client/pom.xml [lastModifiedOnDisk=Mon Sep 22 04:07:02 CEST 2025] [unsavedChanges=false] (Project: gemini-java-client (Display: gemini-java-client))
]
-SystemInstruction:Optional[
# Context Window
- Current Tokens: 0
- Token Threshold: 108000
-------------------------------------------------------------------
- #Output of History.listEntries:
#  Entries in history: 1
-----------------------------------
[0] user - 1 Parts
	[0/0][Text]:Read startup.md, it is in your work directory: /home/pablo/.anahata/ai-assistant
**Use uno.anahata.gemini.HistoryManager.get().getHistory()** to get a **List<Content>** of all items in the history
-------------------------------------------------------------------]
-SystemInstruction:Optional[
# Dynamic Environment Details
# ---------------------------
- **Config: **: uno.anahata.nb.ai.NetBeansGeminiConfig@251cf8d8
- **System Properties**: {java.specification.version=21, sun.jnu.encoding=UTF-8, sun.java2d.dpiaware=true, sun.arch.data.model=64, java.vendor.url=https://ubuntu.com/, plugin.manager.install.global=false, sun.boot.library.path=/usr/lib/jvm/java-21-openjdk-amd64/lib, netbeans.importclass=org.netbeans.upgrade.AutoUpgrade, sun.java.command=org.netbeans.Main --cachedir /home/pablo/snap/netbeans/common/134 --userdir /home/pablo/snap/netbeans/134 --branding nb, jdk.debug=release, netbeans.autoupdate.version=1.23, java.specification.vendor=Oracle Corporation, java.version.date=2025-07-15, java.home=/usr/lib/jvm/java-21-openjdk-amd64, netbeans.user=/home/pablo/snap/netbeans/134, file.separator=/, java.vm.compressedOopsMode=Zero based, line.separator=
, java.vm.specification.vendor=Oracle Corporation, java.specification.name=Java Platform API Specification, jna.boot.library.name=jnidispatch-nb, netbeans.productversion=Apache NetBeans IDE 27, org.apache.commons.logging.LogFactory=org.apache.commons.logging.impl.LogFactoryImpl, netbeans.extbrowser.manual_chrome_plugin_install=yes, netbeans.default_userdir_root=/home/pablo/snap/netbeans/134, sun.management.compiler=HotSpot 64-Bit Tiered Compilers, jdk.home=/usr/lib/jvm/java-21-openjdk-amd64, java.runtime.version=21.0.8+9-Ubuntu-0ubuntu124.04.1, user.name=pablo, sun.java2d.noddraw=true, file.encoding=UTF-8, java.util.logging.config.class=org.netbeans.core.startup.TopLogging, guice.disable.misplaced.annotation.check=true, https.nonProxyHosts=localhost|127.0.0.1|papa-linux, apple.awt.application.appearance=system, java.lang.Runtime.level=FINE, jna.loaded=true, java.io.tmpdir=/tmp, java.version=21.0.8, netbeans.autoupdate.variant=, java.vm.specification.name=Java Virtual Machine Specification, nb.native.filechooser=false, native.encoding=UTF-8, java.library.path=/usr/java/packages/lib:/usr/lib/x86_64-linux-gnu/jni:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:/usr/lib/jni:/lib:/usr/lib, stderr.encoding=UTF-8, java.vendor=Ubuntu, sun.io.unicode.encoding=UnicodeLittle, netbeans.dirs=/snap/netbeans/134/netbeans/nb:/snap/netbeans/134/netbeans/ergonomics:/snap/netbeans/134/netbeans/ide:/snap/netbeans/134/netbeans/extide:/snap/netbeans/134/netbeans/java:/snap/netbeans/134/netbeans/apisupport:/snap/netbeans/134/netbeans/webcommon:/snap/netbeans/134/netbeans/websvccommon:/snap/netbeans/134/netbeans/enterprise:/snap/netbeans/134/netbeans/mobility:/snap/netbeans/134/netbeans/profiler:/snap/netbeans/134/netbeans/python:/snap/netbeans/134/netbeans/php:/snap/netbeans/134/netbeans/identity:/snap/netbeans/134/netbeans/harness:/snap/netbeans/134/netbeans/cnd:/snap/netbeans/134/netbeans/cndext:/snap/netbeans/134/netbeans/cpplite:/snap/netbeans/134/netbeans/dlight:/snap/netbeans/134/netbeans/groovy:/snap/netbeans/134/netbeans/extra:/snap/netbeans/134/netbeans/javacard:/snap/netbeans/134/netbeans/javafx:/snap/netbeans/134/netbeans/rust:, org.openide.version=deprecated, netbeans.dynamic.classpath=/snap/netbeans/134/netbeans/platform/core/asm-9.8.jar:/snap/netbeans/134/netbeans/platform/core/asm-commons-9.8.jar:/snap/netbeans/134/netbeans/platform/core/asm-tree-9.8.jar:/snap/netbeans/134/netbeans/platform/core/core-base.jar:/snap/netbeans/134/netbeans/platform/core/core.jar:/snap/netbeans/134/netbeans/platform/core/org-netbeans-libs-asm.jar:/snap/netbeans/134/netbeans/platform/core/org-openide-filesystems-compat8.jar:/snap/netbeans/134/netbeans/platform/core/org-openide-filesystems.jar:/snap/netbeans/134/netbeans/nb/core/org-netbeans-upgrader.jar:/snap/netbeans/134/netbeans/nb/core/locale/core_nb.jar, flatlaf.nativeLibraryPath=system, netbeans.autoupdate.country=US, truffle.UseFallbackRuntime=true, java.class.path=/snap/netbeans/134/netbeans/platform/lib/boot.jar:/snap/netbeans/134/netbeans/platform/lib/org-openide-modules.jar:/snap/netbeans/134/netbeans/platform/lib/org-openide-util.jar:/snap/netbeans/134/netbeans/platform/lib/org-openide-util-lookup.jar:/snap/netbeans/134/netbeans/platform/lib/org-openide-util-ui.jar, sun.awt.enableExtraMouseButtons=true, org.openide.major.version=IDE/1, java.vm.vendor=Ubuntu, netbeans.buildnumber=27-312307d41168e81225f112a1365f8706d843e5ea, org.openide.specification.version=6.2, user.timezone=Europe/Madrid, java.vm.specification.version=21, os.name=Linux, sun.java.launcher=SUN_STANDARD, user.country=US, http.nonProxyHosts=localhost|127.0.0.1|papa-linux, sun.cpu.endian=little, user.home=/home/pablo, user.language=en, maven.defaultProjectBuilder.disableGlobalModelCache=true, flatlaf.updateUIOnSystemFontChange=false, plugin.manager.check.updates=false, netbeans.autoupdate.language=en, polyglot.engine.WarnInterpreterOnly=false, netbeans.hash.code=unique=05b0afe71-deee-465f-af76-c6c5e921b3b9_cbf2d94f-d5a9-47dd-a6dc-650332f254dd, stdout.encoding=UTF-8, path.separator=:, netbeans.running.environment=gnome, os.version=6.14.0-29-generic, jna.nosys=false, netbeans.home=/snap/netbeans/134/netbeans/platform, java.runtime.name=OpenJDK Runtime Environment, java.vm.name=OpenJDK 64-Bit Server VM, jna.platform.library.path=/usr/lib/x86_64-linux-gnu:/lib/x86_64-linux-gnu:/usr/lib64:/lib64:/usr/lib:/lib:/usr/lib/x86_64-linux-gnu/libfakeroot, java.vendor.url.bug=https://bugs.launchpad.net/ubuntu/+source/openjdk-21, user.dir=/home/pablo, os.arch=amd64, org.xml.sax.driver=com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl$JAXPSAXParser, java.vm.info=mixed mode, sharing, java.vm.version=21.0.8+9-Ubuntu-0ubuntu124.04.1, sun.awt.datatransfer.timeout=1000, org.apache.commons.logging.Log=org.apache.commons.logging.impl.Jdk14Logger, java.class.version=65.0, org.netbeans.io.suspend=0}
- **Environment variables**: {GSM_SKIP_SSH_AGENT_WORKAROUND=true, GJS_DEBUG_TOPICS=JS ERROR;JS LOG, SNAP_LIBRARY_PATH=/var/lib/snapd/lib/gl:/var/lib/snapd/lib/gl32:/var/lib/snapd/void, GJS_DEBUG_OUTPUT=stderr, GNOME_SHELL_SESSION_MODE=ubuntu, SNAP_NAME=netbeans, PATH=/home/pablo/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:/snap/bin, CLUTTER_DISABLE_MIPMAPPED_TEXT=1, LOGNAME=pablo, XDG_MENU_PREFIX=gnome-, XDG_CONFIG_DIRS=/etc/xdg/xdg-ubuntu:/etc/xdg, WAYLAND_DISPLAY=wayland-0, BAMF_DESKTOP_FILE_HINT=/var/lib/snapd/desktop/applications/netbeans_netbeans.desktop, XAUTHORITY=/run/user/1000/.mutter-Xwaylandauth.FN36C3, SNAP_ARCH=amd64, XMODIFIERS=@im=ibus, GIO_LAUNCHED_DESKTOP_FILE_PID=396310, J2D_PIXMAPS=shared, GIO_LAUNCHED_DESKTOP_FILE=/var/lib/snapd/desktop/applications/netbeans_netbeans.desktop, SNAP_REVISION=134, QT_ACCESSIBILITY=1, SNAP_INSTANCE_NAME=netbeans, SNAP_USER_DATA=/home/pablo/snap/netbeans/134, XDG_SESSION_DESKTOP=ubuntu, DBUS_SESSION_BUS_ADDRESS=unix:path=/run/user/1000/bus, GNOME_SETUP_DISPLAY=:1, INVOCATION_ID=6143a1f1af74407db19f61cf2953acde, SHLVL=0, USERNAME=pablo, XDG_DATA_DIRS=/usr/share/ubuntu:/usr/share/gnome:/home/pablo/.local/share/flatpak/exports/share:/var/lib/flatpak/exports/share:/usr/local/share/:/usr/share/:/var/lib/snapd/desktop, SHELL=/bin/bash, XDG_SESSION_CLASS=user, GTK_MODULES=gail:atk-bridge, SESSION_MANAGER=local/papa-linux:@/tmp/.ICE-unix/2200,unix/papa-linux:/tmp/.ICE-unix/2200, SNAP_REAL_HOME=/home/pablo, DISPLAY=:0, HOME=/home/pablo, MEMORY_PRESSURE_WATCH=/sys/fs/cgroup/user.slice/user-1000.slice/user@1000.service/session.slice/org.gnome.Shell@wayland.service/memory.pressure, XDG_CURRENT_DESKTOP=ubuntu:GNOME, SNAP_UID=1000, SNAP_EUID=1000, DEBUGINFOD_URLS=https://debuginfod.ubuntu.com , QT_IM_MODULE=ibus, LANG=en_US.UTF-8, GDMSESSION=ubuntu, SNAP_COOKIE=OFNhw6RbMAU0DgMhg9CjLgOcDMYhuB_WXxUR3SIE_3GHClgAu_Ni, GNOME_DESKTOP_SESSION_ID=this-is-deprecated, SNAP_VERSION=27, SNAP_COMMON=/var/snap/netbeans/common, MEMORY_PRESSURE_WRITE=c29tZSAyMDAwMDAgMjAwMDAwMAA=, IM_CONFIG_PHASE=1, SYSTEMD_EXEC_PID=2243, XDG_RUNTIME_DIR=/run/user/1000, SSH_AUTH_SOCK=/run/user/1000/keyring/ssh, OLDPWD=/snap/netbeans/134/netbeans, SNAP_DATA=/var/snap/netbeans/134, SNAP_REEXEC=, MANAGERPID=1974, DESKTOP_SESSION=ubuntu, USER=pablo, SNAP_USER_COMMON=/home/pablo/snap/netbeans/common, XDG_SESSION_TYPE=wayland, SNAP_INSTANCE_KEY=, PWD=/home/pablo, _=/usr/lib/jvm/java-21-openjdk-amd64/bin/java, JOURNAL_STREAM=9:29883, SNAP=/snap/netbeans/134, SNAP_CONTEXT=OFNhw6RbMAU0DgMhg9CjLgOcDMYhuB_WXxUR3SIE_3GHClgAu_Ni}
]