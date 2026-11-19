@echo off
REM Lance le test de charge JMeter en mode non-GUI et genere le rapport HTML.
REM Usage : run-jmeter.bat [options JMeter supplementaires, ex: -Jduration=120 -JcatalogUsers=50]
setlocal
set SCRIPT_DIR=%~dp0
set RESULTS=%SCRIPT_DIR%results
if exist "%RESULTS%" rmdir /s /q "%RESULTS%"
mkdir "%RESULTS%"
jmeter -n -t "%SCRIPT_DIR%ecommerce-load-test.jmx" -l "%RESULTS%\results.jtl" -j "%RESULTS%\jmeter.log" -e -o "%RESULTS%\report" %*
echo Rapport : %RESULTS%\report\index.html
endlocal
