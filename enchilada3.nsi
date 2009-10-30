; enchilada3.nsi
;
; This script is based on example2.nsi
; which was included in the distribution of the Nullsoft Software Installation
; System.  Yay!  Adapted shoddily by Thomas Smith
;   and further adapted by christja and rzeszotj


;;; the compiler for this file, which makes an installer executable, is
;;; available from http://nsis.sourceforge.net/

; To build a copy of the installer with EXPRESS, uncomment this line:
; ;!define WITH_EXPRESS

; To build a copy of the installer that you'd like to post on the Internets,
; uncomment this line: (engages slow but effective compression)
!define RELEASE


;--------------------------------

; The name of the installer
Name "EDAM-Enchilada Installer"


!ifdef WITH_EXPRESS

!ifdef RELEASE
; EXPRESS is huge, so use special compression when building with it.
SetCompressor /solid lzma
!endif

Outfile "EDAM-Enchilada-EXPRESS-install.exe"
!else
OutFile "EDAM-Enchilada-install.exe"
!endif

; The default installation directory
InstallDir $PROGRAMFILES\EDAM-Enchilada

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\EDAM_Enchilada" "Install_Dir"

; License text to display
!ifdef WITH_EXPRESS
LicenseData "MSDE-EULA.txt"
!else
LicenseData "MPL-1.1.txt"
!endif

;Location of osql in an MSSQL installation.
Var OSQL ; gets set in the first section.


;--------------------------------

; Pages

Page license
Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------


; The stuff to install
Section "EDAM Enchilada (required)"

  SectionIn RO
  
  StrCpy $OSQL "$PROGRAMFILES\Microsoft SQL Server\90\Tools\Binn\SQLCMD"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
!ifdef WITH_EXPRESS
  File MSDE-EULA.txt
!endif
  File "MPL-1.1.txt"
  File "edam-enchilada.jar"
  File "Enchilada.bat"
  File "gpl.txt"
  File "library.txt"
  File "MPL-1.1.txt"
  File "SQLServerRebuildDatabase.txt"
  File "new_icon.ico"
  File "new_icon.gif"
  
  CreateDirectory $INSTDIR\errorframework
  CreateDirectory $INSTDIR\errorframework\ErrorLogs
  
  SetOutPath "$INSTDIR\importation files"
  File "importation files\atofmsBatchDemo.csv"
  File "importation files\atofmsBatchInfo.txt"
  File "importation files\demo.task"
  File "importation files\enchilada.dtd"
  File "importation files\ImporterInstructions"
  File "importation files\meta.dtd"
  
  SetOutPath $INSTDIR\labeling
  File "labeling\nion-sigs.txt"
  File "labeling\pion-sigs.txt"
  File "labeling\run.bat"
  File "labeling\spectrum.exe"
  
  SetOutPath $INSTDIR
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\EDAM_Enchilada "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "DisplayName" "EDAM Enchilada (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

SectionEnd


!ifdef WITH_EXPRESS
Section "MS SQL Desktop Environment (SQL Server Replacement)"

	!define JUST_INSTALLED_SQL
	
	ExecWait "net stop $\"sql server (sqlexpress)$\"" $0

	ExecWait "net start $\"sql server (sqlexpress)$\"" $0
	IntCmp $0 0 netstart_success

	CreateDirectory "C:\EXPRESS-install-temp"
	SetOutPath "C:\EXPRESS-install-temp"
	File /r "C:\SQLEXPRESS"
	
	; XXX - can't yet uninstall MSSQLSERVER.  maybe in uninstall page, do a 
	; messagebox as a callback at the end?
	
	; YARR, ok, so, File and Print Sharing needs to be enabled for the install to work.
	; XXX - password
	ExecWait `"C:\EXPRESS-install-temp\SQLEXPRESS\SQLEXPR.EXE" SAPWD="sa-account-password" DISABLENETWORKPROTOCOLS=2 SECURITYMODE=SQL` $0
    IfErrors 0 +2 
		Abort "SQLEXPRESS could not be installed. Contact the Enchilada team for assistance."
	DetailPrint "setup.exe returned $0"
	IntCmp $0 0 setup_success
    	MessageBox MB_OK 'MS SQL Server failed to install.  This could be because MS SQL Server is already installed---if so, try installing again, this time unchecking "MS SQL Desktop Environment."'
        SetOutPath "$INSTDIR"
        RMDir /r "C:\EXPRESS-install-temp"
	RMDir "C:\EXPRESS-install-temp"
        Abort 'MSSQL failed to install.'
    setup_success:

	ExecWait "net stop $\"sql server (sqlexpress)$\"" $0
	IfErrors 0 +2
		Abort "Could not invoke 'net stop' to stop SQL Server. 'net stop' is a standard Windows command; something is wrong with your Windows installation."
	IntCmp $0 0 netstop_success
        Abort "Failed to stop SQL server---something must be wrong with its installation!"
	
	netstop_success:
	ExecWait "net start $\"sql server (sqlexpress)$\"" $0
	IfErrors 0 +2
		Abort "Could not invoke 'net start' to start SQL Server. 'net start' is a standard Windows command; something is wrong with your Windows installation."
	IntCmp $0 0 netstart_success
        Abort "Failed to start SQL server---something must be wrong with its installation!"

	netstart_success:
	SetOutPath "$INSTDIR"
	RMDir /r "C:\EXPRESS-install-temp"


  	ExecWait '"$OSQL" -S \SQLEXPRESS -U SpASMS -P finally -Q ""' $0
  	IfErrors 0 +2
  		Abort "OSQL refused to respond when testing to see if the Enchilada user was already installed."
  	IntCmp $0 0 userexists1
    	DetailPrint "Enchilada's login not present, adding it."
		ExecWait `"$OSQL" -S \SQLEXPRESS -E -Q "EXEC sp_addlogin 'SpASMS','finally'"` $0
    	IfErrors 0 +2
    		Abort "OSQL refused to respond when trying to install the Enchilada user."
    	StrCmp $0 0 +3
        	MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't install the Enchilada user because TCP/IP ports are disabled.  To fix this, in SQL Configuration Manager, double click on network configurations, double click protocols, right click on connections, double click TCP/IP, change to enabled, and set all the TCP ports to 1433, then run Enchilada set-up again."
        	Abort "Please install the Enchilada user yourself, and try installing again.  User: SpASMS.  Pass: finally.  Must be sysadmin.  Must be accessible via TCP."
    	DetailPrint "Enchilada's login successfully added to SQL Server."
    
    userexists1:
    ExecWait `"$OSQL" -S \SQLEXPRESS -E -Q "EXEC sp_addsrvrolemember 'SpASMS','sysadmin'"`
  	IfErrors 0 +2
  	  Abort "OSQL refused to respond when making user a system administrator."
    IntCmp $0 0 +3
      MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't make the SQL Server login 'SpASMS' a system administrator.  Giving up."
      Abort "Try making the SQL Server login SpASMS into a system administrator."
 
	
SectionEnd
!endif

;Make absolutely sure that some form of SQLExpress is on the computer.
!ifdef WITH_EXPRESS
!ifndef JUST_INSTALLED_SQL
Section
; Error checking
Var OSQLLOC ;
StrCpy $OSQLLOC "osql"
  ExecWait '"$OSQLLOC" -?' $0
  IfErrors 0 +5
    !define NOTHING_HERE
    ; we don't support running without a local installation of SQL Server yet.
    MessageBox MB_OK|MB_ICONEXCLAMATION "SQL Server 2005 doesn't seem to be installed on this system.  If it really is (perhaps with a different 'instance name'?), or if you want to use a remote installation of SQL Server, please contact the developers at dmusican@carleton.edu."
    Abort "Not smart enough to find SQL Server on your system."
  DetailPrint "Found SQL Server installation."
SectionEnd
!endif
!endif

Section "-Check for Java 1.5"
    ExecWait "java -version:1.5.0 -version" $0
    IfErrors 0 installedJava
        DetailPrint "Java is not installed or is not installed correctly."
        MessageBox MB_OK|MB_ICONEXCLAMATION "Enchilada was unable to find Java on your computer. Please go to http://java.com/ to download and install Java 1.5 or newer."
        Abort "Java is not installed."

	installedJava:
    IntCmp $0 0 rightjava
        ExecWait "java -version:1.6.0 -version" $0
        IntCmp $0 0 rightjava
        DetailPrint "Insufficient Java version."
        MessageBox MB_OK|MB_ICONEXCLAMATION "A newer version of Java is needed to run Enchilada.  Please go to http://java.com/ and download Java 1.5 or newer."
        Abort "Please go to java.com and install java 1.5"
        
    rightjava:
    DetailPrint "Found current version of Java"
SectionEnd

!ifndef WITH_EXPRESS
Section "-Check DB and user existence"
  ; See if the SQL Server that theoretically exists, actually does.
  ExecWait '"$OSQL" -?' $0
  IfErrors 0 foundsql
    ; we don't support running without a local installation of SQL Server yet.
    StrCpy $OSQL "$PROGRAMFILES\Microsoft SQL Server\100\Tools\Binn\SQLCMD"
      ExecWait '"$OSQL" -?' $0
      IfErrors 0 foundsql
        MessageBox MB_OK|MB_ICONEXCLAMATION "SQL Server 2005/2008 doesn't seem to be installed on this system.  If it really is (perhaps with a different 'instance name'?), or if you want to use a remote installation of SQL Server, please contact the developers at dmusican@carleton.edu."
        Abort "Not smart enough to find SQL Server on your system."

  foundsql:
  DetailPrint "Found SQL Server installation."

  
  ; 2. is the SpASMS user installed?
  ExecWait '"$OSQL" -U SpASMS -P finally -Q ""' $0
  IfErrors 0 +2
  	Abort "OSQL refused to respond when testing to see if the Enchilada user was already installed."
  IntCmp $0 0 userexists
    DetailPrint "Enchilada's login not present, adding it."
    ExecWait `"$OSQL" -E -Q "EXEC sp_addlogin 'SpASMS','finally'"` $0
    IfErrors 0 +2
    	Abort "OSQL refused to respond when trying to install the Enchilada user."
    StrCmp $0 0 +3
        MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't install Enchilada's login on SQL Server.  Please do this manually, then try installing again.  The login name is 'SpASMS', the password is 'finally'.  SpASMS must be a 'System Administrator'.  You can do this using Enterprise Manager:  Add a login to the local installation of SQL Server."
        Abort "Please install the Enchilada user yourself, and try installing again.  User: SpASMS.  Pass: finally.  Must be sysadmin.  Must be accessible via TCP."
    DetailPrint "Enchilada's login successfully added to SQL Server."

  ExecWait `"$OSQL" -E -Q "ALTER DATABASE SpASMSdb SET RECOVERY SIMPLE"`
  IfErrors 0 +2
  	Abort "Something weird"
  ; so now the user is installed or the whole installation has been aborted.
  userexists:
  ExecWait `"$OSQL" -E  -Q "EXEC sp_addsrvrolemember 'SpASMS','sysadmin'"`
  IfErrors 0 +2
  	Abort "OSQL refused to respond when making user a system administrator."
  IntCmp $0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't make the SQL Server login 'SpASMS' a system administrator.  Giving up."
    Abort "Try making the SQL Server login SpASMS into a system administrator."
      
SectionEnd
!endif


; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\EDAM Enchilada"
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\EDAM Enchilada.lnk" "javaw.exe" `-Xmx800m -jar "$INSTDIR\edam-enchilada.jar" -redirectOutput` "$INSTDIR\new_icon.ico" 0
  
SectionEnd

Section "Desktop Shortcut"

    CreateShortCut "$DESKTOP\Enchilada.lnk" "javaw.exe" `-Xmx800m -jar "$INSTDIR\edam-enchilada.jar" -redirectOutput` "$INSTDIR\new_icon.ico" 0

SectionEnd

; Notify the user of the port issues (port 1433)
!ifdef WITH_EXPRESS
Section
MessageBox MB_OK|MB_ICONEXCLAMATION "Since SQL Server Express was just installed, some of its port settings must be changed.$\nTo do this:$\nChoose Start->Programs$\n->Microsoft SQL Server 2005$\n->Configuration Tools$\n->Microsoft SQL Server Configuration Manager.$\n$\nUnder Network Configuration->Protocols, Right-Click TCP/IP, choose Properties, and choose the IP Addresses Tab.$\n$\nNow, change all the TCP Dynamic ports to 1433."
SectionEnd
!endif

;--------------------------------

; Uninstaller

Section "un.Uninstall"
  
!ifdef WITH_EXPRESS
  MessageBox MB_OK "Since you are uninstalling Enchilada, it is likely that you should also uninstall SQL Server.  You can do this from the Add/Remove Programs dialog in the Control Panel."
!endif

  StrCpy $OSQL "$PROGRAMFILES\Microsoft SQL Server\80\Tools\Binn\osql.exe"
  ExecWait '"$OSQL" -?' $0
  IfErrors 0 dropdatabase
    StrCpy $OSQL "$PROGRAMFILES\Microsoft SQL Server\100\Tools\Binn\osql.exe"
    ExecWait '"$OSQL" -?' $0
    IfErrors 0 dropdatabase
      DetailPrint "Can't find SQL Server, maybe you've already uninstalled it?"
      Goto donewithuser
  
  dropdatabase:
  ExecWait `"$OSQL" -E -Q "DROP DATABASE SpASMSdb EXEC sp_droplogin 'SpASMS'"` $0
  IfErrors 0 +3
  	DetailPrint "Can't find SQL Server when trying to drop database."
  	Goto donewithuser
  IntCmp $0 0 donewithuser
    MessageBox MB_OK "Tried to remove Enchilada's login from SQL Server, 'SpASMS', but it didn't work.  You should try to do this manually."

  donewithuser:

  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada"
  DeleteRegKey HKLM SOFTWARE\EDAM_Enchilada

  ; Remove files and uninstaller
  Delete $INSTDIR\edam-enchilada.jar
  Delete $INSTDIR\meta.dtd
  Delete $INSTDIR\enchilada.dtd
  Delete $INSTDIR\ImporterInstructions
  Delete $INSTDIR\Enchilada.bat
  Delete $INSTDIR\MSDE-EULA.txt
  Delete "$INSTDIR\gpl.txt"
  Delete "$INSTDIR\library.txt"
  Delete "$INSTDIR\MPL-1.1.txt"
  Delete "$INSTDIR\SQLServerRebuildDatabase.txt"
  Delete $INSTDIR\*.par
  Delete $INSTDIR\*.set
  Delete $INSTDIR\uninstall.exe
  Delete $INSTDIR\new_icon.ico
  Delete $INSTDIR\labeling\cygwin1.dll
  Delete $INSTDIR\labeling\nion-sigs.txt
  Delete $INSTDIR\labeling\pion-sigs.txt
  Delete $INSTDIR\labeling\run.bat
  Delete $INSTDIR\labeling\spc_negative.txt
  Delete $INSTDIR\labeling\spc_positive.txt
  Delete $INSTDIR\labeling\spectrum.exe
  Delete $INSTDIR\labeling\spectrum.exe.stackdump
  Delete $INSTDIR\labeling\temp_nion-sigs.txt"
  Delete $INSTDIR\labeling\temp_pion-sigs.txt"
  
  RMDir /r $INSTDIR\errorframework
  RMDir $INSTDIR\errorframework
  
  ; Added Aug 2007
  Delete $INSTDIR\TEMP\*.*
  RMDir /r $INSTDIR\TEMP
  Delete $INSTDIR\*.gif
  
  Delete "$INSTDIR\importation files\*.*"
  RMDir "$INSTDIR\importation files"
  
  RMDir "$INSTDIR\labeling"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\EDAM Enchilada\*.*"
  Delete "$DESKTOP\Enchilada.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\EDAM Enchilada"
  SetOutPath "C:\"
  RMDir "$INSTDIR"

SectionEnd

