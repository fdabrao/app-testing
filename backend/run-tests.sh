#!/bin/bash

# Colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to display help
show_help() {
    echo -e "${BLUE}======================= TEST RUNNER SCRIPT =======================${NC}"
    echo -e "Usage: ./run-tests.sh [OPTIONS]"
    echo -e ""
    echo -e "Options:"
    echo -e "  ${GREEN}-a, --all${NC}                  Run all tests"
    echo -e "  ${GREEN}-c, --class${NC} CLASS_NAME     Run a specific test class (e.g., ProductControllerTest)"
    echo -e "  ${GREEN}-t, --test${NC} TEST_NAME       Run a specific test method (use with -c)"
    echo -e "  ${GREEN}-p, --package${NC} PACKAGE      Run tests in a specific package"
    echo -e "  ${GREEN}-s, --skip-tests${NC}           Skip tests during build"
    echo -e "  ${GREEN}-d, --debug${NC}                Run tests in debug mode"
    echo -e "  ${GREEN}-h, --help${NC}                 Show this help message"
    echo -e ""
    echo -e "Examples:"
    echo -e "  ./run-tests.sh --all                    # Run all tests"
    echo -e "  ./run-tests.sh -c ProductControllerTest # Run ProductControllerTest class only"
    echo -e "  ./run-tests.sh -c ProductControllerTest -t shouldGetAllProducts  # Run specific test method"
    echo -e ""
}

# Function to check if Maven wrapper exists, otherwise use maven
use_maven() {
    if [ -f "./mvnw" ]; then
        echo -e "${YELLOW}Using Maven Wrapper${NC}"
        MVN_CMD="./mvnw"
    else
        echo -e "${YELLOW}Using Maven${NC}"
        MVN_CMD="mvn"
    fi
}

# Check if no arguments provided
if [ $# -eq 0 ]; then
    show_help
    exit 1
fi

# Process command line arguments
while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        -a|--all)
            echo -e "${GREEN}Running all tests...${NC}"
            use_maven
            $MVN_CMD test
            exit $?
            ;;
        -c|--class)
            TEST_CLASS="$2"
            shift
            ;;
        -t|--test)
            TEST_METHOD="$2"
            shift
            ;;
        -p|--package)
            TEST_PACKAGE="$2"
            shift
            ;;
        -s|--skip-tests)
            echo -e "${YELLOW}Skipping tests...${NC}"
            use_maven
            $MVN_CMD package -DskipTests
            exit $?
            ;;
        -d|--debug)
            DEBUG_MODE=true
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
    shift
done

# Run specific test class
if [ ! -z "$TEST_CLASS" ]; then
    use_maven
    
    # Check if a specific test method was specified
    if [ ! -z "$TEST_METHOD" ]; then
        echo -e "${GREEN}Running test method: ${BLUE}$TEST_METHOD${GREEN} in class: ${BLUE}$TEST_CLASS${NC}"
        TEST_COMMAND="$MVN_CMD -Dtest=$TEST_CLASS#$TEST_METHOD test"
    else
        echo -e "${GREEN}Running test class: ${BLUE}$TEST_CLASS${NC}"
        TEST_COMMAND="$MVN_CMD -Dtest=$TEST_CLASS test"
    fi
    
    # Add debug if requested
    if [ "$DEBUG_MODE" = true ]; then
        echo -e "${YELLOW}Running in debug mode${NC}"
        TEST_COMMAND="$TEST_COMMAND -Dmaven.surefire.debug"
    fi
    
    # Execute the test command
    eval $TEST_COMMAND
    exit $?
fi

# Run tests in a specific package
if [ ! -z "$TEST_PACKAGE" ]; then
    use_maven
    echo -e "${GREEN}Running tests in package: ${BLUE}$TEST_PACKAGE${NC}"
    
    TEST_COMMAND="$MVN_CMD -Dtest=$TEST_PACKAGE.** test"
    
    # Add debug if requested
    if [ "$DEBUG_MODE" = true ]; then
        echo -e "${YELLOW}Running in debug mode${NC}"
        TEST_COMMAND="$TEST_COMMAND -Dmaven.surefire.debug"
    fi
    
    # Execute the test command
    eval $TEST_COMMAND
    exit $?
fi

# If we got here, the user probably provided invalid arguments
echo -e "${RED}Invalid arguments. Please see help below:${NC}"
show_help
exit 1 