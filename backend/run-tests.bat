@echo off
setlocal enabledelayedexpansion

:: Colors for better readability (Windows)
set GREEN=[92m
set YELLOW=[93m
set RED=[91m
set BLUE=[94m
set NC=[0m

:: Function to display help
:show_help
echo %BLUE%======================= TEST RUNNER SCRIPT =======================%NC%
echo Usage: run-tests.bat [OPTIONS]
echo.
echo Options:
echo   %GREEN%-a, --all%NC%                  Run all tests
echo   %GREEN%-c, --class%NC% CLASS_NAME     Run a specific test class (e.g., ProductControllerTest)
echo   %GREEN%-t, --test%NC% TEST_NAME       Run a specific test method (use with -c)
echo   %GREEN%-p, --package%NC% PACKAGE      Run tests in a specific package
echo   %GREEN%-s, --skip-tests%NC%           Skip tests during build
echo   %GREEN%-d, --debug%NC%                Run tests in debug mode
echo   %GREEN%-h, --help%NC%                 Show this help message
echo.
echo Examples:
echo   run-tests.bat --all                    # Run all tests
echo   run-tests.bat -c ProductControllerTest # Run ProductControllerTest class only
echo   run-tests.bat -c ProductControllerTest -t shouldGetAllProducts  # Run specific test method
echo.
goto :eof

:: Function to check if Maven wrapper exists, otherwise use maven
:use_maven
if exist "mvnw.cmd" (
    echo %YELLOW%Using Maven Wrapper%NC%
    set MVN_CMD=mvnw.cmd
) else (
    echo %YELLOW%Using Maven%NC%
    set MVN_CMD=mvn
)
goto :eof

:: Check if no arguments provided
if "%~1"=="" (
    call :show_help
    exit /b 1
)

:: Process command line arguments
:parse_args
if "%~1"=="" goto :end_parse_args

if /i "%~1"=="-h" (
    call :show_help
    exit /b 0
) else if /i "%~1"=="--help" (
    call :show_help
    exit /b 0
) else if /i "%~1"=="-a" (
    echo %GREEN%Running all tests...%NC%
    call :use_maven
    %MVN_CMD% test
    exit /b %errorlevel%
) else if /i "%~1"=="--all" (
    echo %GREEN%Running all tests...%NC%
    call :use_maven
    %MVN_CMD% test
    exit /b %errorlevel%
) else if /i "%~1"=="-c" (
    set TEST_CLASS=%~2
    shift
) else if /i "%~1"=="--class" (
    set TEST_CLASS=%~2
    shift
) else if /i "%~1"=="-t" (
    set TEST_METHOD=%~2
    shift
) else if /i "%~1"=="--test" (
    set TEST_METHOD=%~2
    shift
) else if /i "%~1"=="-p" (
    set TEST_PACKAGE=%~2
    shift
) else if /i "%~1"=="--package" (
    set TEST_PACKAGE=%~2
    shift
) else if /i "%~1"=="-s" (
    echo %YELLOW%Skipping tests...%NC%
    call :use_maven
    %MVN_CMD% package -DskipTests
    exit /b %errorlevel%
) else if /i "%~1"=="--skip-tests" (
    echo %YELLOW%Skipping tests...%NC%
    call :use_maven
    %MVN_CMD% package -DskipTests
    exit /b %errorlevel%
) else if /i "%~1"=="-d" (
    set DEBUG_MODE=true
) else if /i "%~1"=="--debug" (
    set DEBUG_MODE=true
) else (
    echo %RED%Unknown option: %~1%NC%
    call :show_help
    exit /b 1
)

shift
goto :parse_args

:end_parse_args

:: Run specific test class
if defined TEST_CLASS (
    call :use_maven
    
    :: Check if a specific test method was specified
    if defined TEST_METHOD (
        echo %GREEN%Running test method: %BLUE%!TEST_METHOD!%GREEN% in class: %BLUE%!TEST_CLASS!%NC%
        set TEST_COMMAND=%MVN_CMD% -Dtest=!TEST_CLASS!#!TEST_METHOD! test
    ) else (
        echo %GREEN%Running test class: %BLUE%!TEST_CLASS!%NC%
        set TEST_COMMAND=%MVN_CMD% -Dtest=!TEST_CLASS! test
    )
    
    :: Add debug if requested
    if "!DEBUG_MODE!"=="true" (
        echo %YELLOW%Running in debug mode%NC%
        set TEST_COMMAND=!TEST_COMMAND! -Dmaven.surefire.debug
    )
    
    :: Execute the test command
    !TEST_COMMAND!
    exit /b %errorlevel%
)

:: Run tests in a specific package
if defined TEST_PACKAGE (
    call :use_maven
    echo %GREEN%Running tests in package: %BLUE%!TEST_PACKAGE!%NC%
    
    set TEST_COMMAND=%MVN_CMD% -Dtest=!TEST_PACKAGE!.** test
    
    :: Add debug if requested
    if "!DEBUG_MODE!"=="true" (
        echo %YELLOW%Running in debug mode%NC%
        set TEST_COMMAND=!TEST_COMMAND! -Dmaven.surefire.debug
    )
    
    :: Execute the test command
    !TEST_COMMAND!
    exit /b %errorlevel%
)

:: If we got here, the user probably provided invalid arguments
echo %RED%Invalid arguments. Please see help below:%NC%
call :show_help
exit /b 1 