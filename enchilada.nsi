; enchilada.nsi
;
; This script is based on example2.nsi
; which was included in the distribution of the Nullsoft Software Installation
; System.  Yay!  Adapted shoddily by Thomas Smith


;;; the compiler for this file, which makes an installer executable, is
;;; available from http://nsis.sourceforge.net/

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
  File "meta.dtd"
  File "enchilada.dtd"
  File "Enchilada.bat"
  File "gpl.txt"
  File "library.txt"
  File "MPL-1.1.txt"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\EDAM_Enchilada "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "DisplayName" "EDAM Enchilada"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\EDAM Enchilada"
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\EDAM Enchilada\EDAM Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\Enchilada.bat" 0
  
SectionEnd

Section "Desktop Shortcut"

	CreateShortCut "$DESKTOP\Enchilada.lnk" "$INSTDIR\Enchilada.bat" "" "$INSTDIR\Enchilada.bat" 0

SectionEnd

;--------------------------------

; Uninstaller

Section "un.Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\EDAM_Enchilada"
  DeleteRegKey HKLM SOFTWARE\EDAM_Enchilada

  ; Remove files and uninstaller
  Delete $INSTDIR\edam-enchilada.jar
  Delete $INSTDIR\meta.dtd
  Delete $INSTDIR\enchilada.dtd
  Delete $INSTDIR\Enchilada.bat
  Delete "$INSTDIR\gpl.txt"
  Delete "$INSTDIR\library.txt"
  Delete "$INSTDIR\MPL-1.1.txt"
  Delete $INSTDIR\*.par
  Delete $INSTDIR\*.set
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\EDAM Enchilada\*.*"
  Delete "$DESKTOP\Enchilada.lnk"

  ; Remove directories used
  RMDir "$SMPROGRAMS\EDAM Enchilada"
  RMDir "$INSTDIR"

SectionEnd