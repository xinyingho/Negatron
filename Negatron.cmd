@echo off & setLocal enableExtensions enableDelayedExpansion

set DIR=%~dp0
set /a MINIMUMVERSION=11
set JVM_OPTIONS=-Xms512m -Xmx2g --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=negatron --add-opens javafx.controls/javafx.scene.control=negatron --add-opens javafx.controls/javafx.scene.control.skin=negatron --module-path="%DIR%modules";"%DIR%modules/win"

:: Check whether any system-wide Java runtimes meet the minimum requirements

for /f "tokens=*" %%j in ('where java') do (
	:: default java check
	if not "%%j" == "" (
		set JRE=

		:: runtime check
		for /f "tokens=1" %%r in ('2^>^&1 "%%j" -version ^| find "version"') do (
			if "%%r" == "java" set JRE=%%r
			if "%%r" == "openjdk" set JRE=%%r
		)

		if not "!JRE!" == "" (
			:: version check
			for /f "tokens=3" %%v in ('2^>^&1 "%%j" -version ^| find "version"') do (
				for /f "tokens=1 delims=." %%m in (%%v) do set /a MAJORVERSION=%%m
			)

			if "!MAJORVERSION!" geq "%MINIMUMVERSION%" (
				:: All checks have been passed
				set JAVA=%%j
				set JAVA=!JAVA:java.exe=javaw.exe!
				start "Negatron" "!JAVA!" %JVM_OPTIONS% -m negatron/net.babelsoft.negatron.NegatronApp %*
				exit /b 0
			)
		)
	)
)
cmd /k echo **Negatron requires Oracle's Java or OpenJDK %MINIMUMVERSION%+ to run**
exit /b 1
