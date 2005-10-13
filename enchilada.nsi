; enchilada.nsi
;
; This script is based on example2.nsi
; which was included in the distribution of the Nullsoft Software Installation
; System.  Yay!  Adapted shoddily by Thomas Smith


;;; the compiler for this file, which makes an installer executable, is
;;; available from http://nsis.sourceforge.net/

SetCompressor /solid lzma

;--------------------------------

; The name of the installer
Name "EDAM-Enchilada Installer"

; The file to write
OutFile "EDAM-Enchilada-install.exe"

; The default installation directory
InstallDir $PROGRAMFILES\EDAM-Enchilada

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\EDAM_Enchilada" "Install_Dir"

; License text to display
; LicenseText "MPL-1.1.txt"

;--------------------------------

; Pages

; Page license
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
  File "edam-enchilada.jar"
  File "importation files\meta.dtd"
  File "importation files\enchilada.dtd"
  File "importation files\ImporterInstructions"
  File "Enchilada.bat"
  File "gpl.txt"
  File "library.txt"
  File "MPL-1.1.txt"
  File "SQLServerRebuildDatabase.txt"
  File "icon.ico"
  
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

Section "MS SQL Desktop Environment (SQL Server Replacement)"

	CreateDirectory "C:\MSDE-install-temp"
	SetOutPath "C:\MSDE-install-temp"
	File /r MSDERelA
	
	; XXX - no errors are detected or anything.  Meep!  (return 0 is good).
	; XXX - can't yet uninstall MSSQLSERVER.  maybe in uninstall page, do a 
	; messagebox as a callback at the end?
	
	; YARR, ok, so, File and Print Sharing needs to be enabled for the install to work.
	; XXX - password
	ExecWait `"C:\MSDE-install-temp\MSDERelA\setup.exe" SAPWD="sa-account-password" TARGETDIR="$INSTDIR\dbt\" DATADIR="$INSTDIR\dbd\" DISABLENETWORKPROTOCOLS=0 SECURITYMODE=SQL` $0
	DetailPrint "setup.exe returned $0"
	
	ExecWait "net start MSSQLSERVER"
	
    ; XXX - password
    ExecWait `"C:\Program Files\Microsoft SQL Server\80\Tools\Binn\osql.exe" -U sa -P "sa-account-password" -Q "EXEC sp_addlogin 'SpASMS','finally' EXEC sp_addsrvrolemember 'SpASMS','bulkadmin' EXEC sp_addsrvrolemember 'SpASMS','dbcreator'"` $0
	DetailPrint "osql returned $0"

	SetOutPath "$INSTDIR"
	RMDir /r "C:\MSDE-install-temp"

SectionEnd


;--------------------------------

; Uninstaller

Section "un.Uninstall"
  
  ExecWait "net stop MSSQLSERVER"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada"
  DeleteRegKey HKLM SOFTWARE\EDAM_Enchilada

  ; Remove files and uninstaller
  Delete $INSTDIR\edam-enchilada.jar
  Delete $INSTDIR\meta.dtd
  Delete $INSTDIR\enchilada.dtd
  Delete $INSTDIR\ImporterInstructions
  Delete $INSTDIR\Enchilada.bat
  Delete "$INSTDIR\gpl.txt"
  Delete "$INSTDIR\library.txt"
  Delete "$INSTDIR\MPL-1.1.txt"
  Delete "$INSTDIR\SQLServerRebuildDatabase.txt"
  Delete $INSTDIR\*.par
  Delete $INSTDIR\*.set
  Delete $INSTDIR\uninstall.exe
  Delete $INSTDIR\icon.ico

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\EDAM Enchilada\*.*"
  Delete "$DESKTOP\Enchilada.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\EDAM Enchilada"
  RMDir "$INSTDIR"

SectionEnd