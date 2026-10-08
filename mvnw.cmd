@echo off
@rem =========================================================================
@rem Fast Maven Runner Wrapper
@rem Uses local Apache Maven 3.9.6 and honors the caller's JAVA_HOME
@rem =========================================================================

if exist "%USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    call "%USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

echo ERROR: Maven not found in %USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd
exit /b 1
