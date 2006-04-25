; enchilada.nsi
;
; This script is based on example2.nsi
; which was included in the distribution of the Nullsoft Software Installation
; System.  Yay!  Adapted shoddily by Thomas Smith


;;; the compiler for this file, which makes an installer executable, is
;;; available from http://nsis.sourceforge.net/

; To build a copy of the installer with MSDE, uncomment this line:
!define WITH_MSDE

; To build a copy of the installer that you'd like to post on the Internets,
; uncomment this line:
;!define RELEASE


;--------------------------------

; The name of the installer
Name "EDAM-Enchilada Installer"


!ifdef WITH_MSDE

!ifdef RELEASE
; MSDE is huge, so use special compression when building with it.
SetCompressor /solid lzma
!endif

Outfile "EDAM-Enchilada-MSDE-install.exe"
!else
OutFile "EDAM-Enchilada-install.exe"
!endif

; The default installation directory
InstallDir $PROGRAMFILES\EDAM-Enchilada

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\EDAM_Enchilada" "Install_Dir"

; License text to display
!ifdef WITH_MSDE
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
  
  StrCpy $OSQL "C:\Program Files\Microsoft SQL Server\80\Tools\Binn\osql.exe"
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
!ifdef WITH_MSDE
  File MSDE-EULA.txt
!endif
  File "MPL-1.1.txt"
  File "edam-enchilada.jar"
  File "Enchilada.bat"
  File "gpl.txt"
  File "library.txt"
  File "MPL-1.1.txt"
  File "SQLServerRebuildDatabase.txt"
  File "icon.ico"
  
  SetOutPath "$INSTDIR\importation files"
  File "importation files\atofmsBatchDemo.csv"
  File "importation files\atofmsBatchInfo.txt"
  File "importation files\demo.task"
  File "importation files\enchilada.dtd"
  File "importation files\ImporterInstructions"
  File "importation files\meta.dtd"
  
  SetOutPath $INSTDIR\labeling
  File "labeling\cygwin1.dll"
  File "labeling\nion-sigs.txt"
  File "labeling\pion-sigs.txt"
  File "labeling\run.bat"
  File "labeling\spc_negative.txt"
  File "labeling\spc_positive.txt"
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

!ifdef WITH_MSDE
Section "MS SQL Desktop Environment (SQL Server Replacement)"

	CreateDirectory "C:\MSDE-install-temp"
	SetOutPath "C:\MSDE-install-temp"
	File /r MSDERelA
	
	; XXX - can't yet uninstall MSSQLSERVER.  maybe in uninstall page, do a 
	; messagebox as a callback at the end?
	
	; YARR, ok, so, File and Print Sharing needs to be enabled for the install to work.
	; XXX - password
	ExecWait `"C:\MSDE-install-temp\MSDERelA\setup.exe" SAPWD="sa-account-password" DISABLENETWORKPROTOCOLS=0 SECURITYMODE=SQL` $0
	DetailPrint "setup.exe returned $0"
	IntCmp $0 0 setup_success
    	MessageBox MB_OK 'MS SQL Server failed to install.  This could be because MS SQL Server is already installed---if so, try installing again, this time unchecking "MS SQL Desktop Environment."  It could also be because the Microsoft File and Print Sharing protocol is not installed (install it from Properties from the right-click menu of a Network Connection in the Control Panel).'
        SetOutPath C:\
        RMDir /r "C:\MSDE-install-temp"
	RMDir "C:\MSDE-install-temp"
        Abort 'MSSQL failed to install.'
    setup_success:
	
	ExecWait "net start MSSQLSERVER" $0
	IntCmp $0 0 netstart_success
        Abort "Failed to start SQL server---something must be wrong with its installation!"
	netstart_success:

	SetOutPath "$INSTDIR"
	RMDir /r "C:\MSDE-install-temp"

SectionEnd
!endif



Section "-Check DB and user existence"
  ; See if the SQL Server that theoretically exists, actually does.
  ExecWait '"$OSQL" -?' $0
  IfErrors 0 +3
    ; we don't support running without a local installation of SQL Server yet.
    MessageBox MB_OK|MB_ICONEXCLAMATION "SQL Server 2000 doesn't seem to be installed on this system.  If it really is (perhaps with a different 'instance name'?), or if you want to use a remote installation of SQL Server, please contact the developers at dmusican@carleton.edu."
    Abort "Not smart enough to find SQL Server on your system."
  DetailPrint "Found SQL Server installation."
  
  ; 2. is the SpASMS user installed?
  ExecWait '"$OSQL" -U SpASMS -P finally -Q ""' $0
  IntCmp $0 0 userexists
    DetailPrint "Enchilada's login not present, adding it."
    ExecWait `"$OSQL" -E -Q "EXEC sp_addlogin 'SpASMS','finally'"` $0
    StrCmp $0 0 +3
        MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't install Enchilada's login on SQL Server.  Please do this manually, then try installing again.  The login name is 'SpASMS', the password is 'finally'.  SpASMS must be a 'System Administrator'.  You can do this using Enterprise Manager:  Add a login to the local installation of SQL Server."
        Abort "Please install the Enchilada user yourself, and try installing again.  User: SpASMS.  Pass: finally.  Must be sysadmin.  Must be accessible via TCP."
    DetailPrint "Enchilada's login successfully added to SQL Server."

  ; so now the user is installed or the whole installation has been aborted.
  userexists:
  ExecWait `"$OSQL" -E  -Q "EXEC sp_addsrvrolemember 'SpASMS','sysadmin'"`
  IntCmp $0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "Couldn't make the SQL Server login 'SpASMS' a system administrator.  Giving up."
    Abort "Try making the SQL Server login SpASMS into a system administrator."
      
SectionEnd



; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\EDAM Enchilada"
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\EDAM Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\icon.ico" 0
  
SectionEnd

Section "Desktop Shortcut"

    CreateShortCut "$DESKTOP\Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\icon.ico" 0

SectionEnd



;--------------------------------

; Uninstaller

Section "un.Uninstall"
  
!ifdef WITH_MSDE
  MessageBox MB_OK "Since you are uninstalling Enchilada, it is likely that you should also uninstall SQL Server.  You can do this from the Add/Remove Programs dialog in the Control Panel."
!endif

  StrCpy $OSQL "C:\Program Files\Microsoft SQL Server\80\Tools\Binn\osql.exe"
  ExecWait '"$OSQL" -?' $0
  IfErrors 0 +3
    DetailPrint "Can't find SQL Server, maybe you've already uninstalled it?"
    Goto donewithuser
  
  ExecWait `"$OSQL" -E -Q "DROP DATABASE SpASMSdb EXEC sp_droplogin 'SpASMS'"` $0
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
  Delete $INSTDIR\icon.ico
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

