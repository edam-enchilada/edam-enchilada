; enchilada.nsi
;
; This script is based on example2.nsi
; which was included in the distribution of the Nullsoft Software Installation
; System.  Yay!  Adapted shoddily by Thomas Smith


;;; the compiler for this file, which makes an installer executable, is
;;; available from http://nsis.sourceforge.net/

;!define WITH_MSDE
!define RELEASE


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
  File "importation files\meta.dtd"
  File "importation files\enchilada.dtd"
  File "importation files\ImporterInstructions"
  
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

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\EDAM Enchilada"
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\EDAM Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\icon.ico" 0
  
SectionEnd

Section "Desktop Shortcut"

	CreateShortCut "$DESKTOP\Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\icon.ico" 0

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
	ExecWait `"C:\MSDE-install-temp\MSDERelA\setup.exe" SAPWD="sa-account-password" TARGETDIR="$INSTDIR\dbt\" DATADIR="$INSTDIR\dbd\" DISABLENETWORKPROTOCOLS=0 SECURITYMODE=SQL` $0
	DetailPrint "setup.exe returned $0"
	
	IntCmp $0 0 setup_success
    	MessageBox MB_OK 'MS SQL Server failed to install.  This could be because MS SQL Server is already installed---if so, try installing again, this time unchecking "MS SQL Desktop Environment."  It could also be because the Microsoft File and Print Sharing protocol is not installed (install it from Properties from the right-click menu of a Network Connection in the Control Panel).'
        Abort 'MSSQL failed to install.'
    setup_success:
	
	ExecWait "net start MSSQLSERVER" $0
	IntCmp $0 0 netstart_success
        Abort "Failed to start SQL server---something must be wrong with its installation!"
	netstart_success:
	
    ; XXX - password
    ExecWait `"C:\Program Files\Microsoft SQL Server\80\Tools\Binn\osql.exe" -U sa -P "sa-account-password" -Q "EXEC sp_addlogin 'SpASMS','finally' EXEC sp_addsrvrolemember 'SpASMS','bulkadmin' EXEC sp_addsrvrolemember 'SpASMS','dbcreator'"` $0
	DetailPrint "osql returned $0"

	IntCmp $0 0 makeusers_success
		Abort "failed to make Enchilada users in the database!"
	makeusers_success:

	SetOutPath "$INSTDIR"
	RMDir /r "C:\MSDE-install-temp"

SectionEnd
!endif

;--------------------------------

; Uninstaller

Section "un.Uninstall"
  
!ifdef WITH_MSDE
  ExecWait "net stop MSSQLSERVER"
  
  MessageBox MB_OK "Since you are uninstalling Enchilada, it is likely that you should also uninstall SQL Server.  You can do this from the Add/Remove Programs dialog in the Control Panel."
!endif

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
  Delete $INSTDIR\labeling\temp_nion-sigs.txt"
  Delete $INSTDIR\labeling\temp_pion-sigs.txt"
  
  RMDir "$INSTDIR\labeling"

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\EDAM Enchilada\*.*"
  Delete "$DESKTOP\Enchilada.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\EDAM Enchilada"
  RMDir "$INSTDIR"

SectionEnd

