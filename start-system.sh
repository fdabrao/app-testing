#!/bin/bash

# ANSI color codes for output formatting
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}===================================================================${NC}"
echo -e "${BLUE}           STARTING FULL SYSTEM (PostgreSQL, Backend, Frontend)     ${NC}"
echo -e "${BLUE}===================================================================${NC}"

# Function to check if a port is in use
check_port() {
  if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
    return 0
  else
    return 1
  fi
}

# Function to wait for a service to be ready on a specific port
wait_for_service() {
  local port=$1
  local service_name=$2
  local max_attempts=$3
  local attempt=1

  echo -e "${YELLOW}Waiting for $service_name to start on port $port...${NC}"
  
  while ! check_port $port; do
    if [ $attempt -gt $max_attempts ]; then
      echo -e "${RED}$service_name did not start within the expected time. Exiting.${NC}"
      exit 1
    fi
    echo -n "."
    sleep 1
    attempt=$((attempt+1))
  done
  
  echo -e "\n${GREEN}$service_name is running on port $port.${NC}"
}

# Step 1: Start PostgreSQL with Docker Compose
echo -e "\n${YELLOW}STEP 1: Starting PostgreSQL database with Docker Compose...${NC}"
cd backend
docker-compose down -v
docker-compose up -d
cd ..

sleep 5

# Step 2: Start the backend
echo -e "\n${YELLOW}STEP 2: Starting Backend (Spring Boot)...${NC}"
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for the backend to be ready (typically runs on port 8080)
wait_for_service 8080 "Backend" 60

# Step 3: Start the frontend
echo -e "\n${YELLOW}STEP 3: Starting Frontend (Angular)...${NC}"
cd frontend
npm install
npm start &
FRONTEND_PID=$!
cd ..

# Wait for the frontend to be ready (typically runs on port 4200)
wait_for_service 4200 "Frontend" 60

echo -e "\n${GREEN}===================================================================${NC}"
echo -e "${GREEN}           SYSTEM IS UP AND RUNNING                                 ${NC}"
echo -e "${GREEN}===================================================================${NC}"
echo -e "${BLUE}Frontend URL: http://localhost:4200${NC}"
echo -e "${BLUE}Backend API URL: http://localhost:8080${NC}"
echo -e "${BLUE}PostgreSQL: localhost:5432${NC}"
echo -e "\n${YELLOW}Process IDs:${NC}"
echo -e "Backend PID: $BACKEND_PID"
echo -e "Frontend PID: $FRONTEND_PID"
echo -e "\n${YELLOW}To stop all services, press CTRL+C or run:${NC}"
echo -e "kill $BACKEND_PID $FRONTEND_PID && cd backend && docker-compose down"

curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"admin","email":"admin@local.com","passwordHash":"admin", "firstName":"Admin", "lastName":"User"}}' > /dev/null 2>&1

# Wait for user to press Ctrl+C
trap "echo -e '\n${RED}Shutting down services...${NC}'; kill $BACKEND_PID $FRONTEND_PID; cd backend && docker-compose down; echo -e '${GREEN}All services stopped.${NC}'" SIGINT
wait 
