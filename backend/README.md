## Development comments

The backend is using postgresql database with docker compose and docker initdb process to schema and seeds.

I used the https://start.spring.io/ for basic start project with the dependencies needed to use web and security. For the integration test I used the https://testcontainers.com/ that is very good to embeded containers in integration test cycle, because we can have a clean environment for each test to reset the schenarios.

I used the Model and Controller as a structure to provide data to frontend. I did not include more complexity using Design Patterns because it was not needed as it's a simple application. I used interceptors to make sure all the endpoints receive an authentication token as security reasons. And too, I did not included authorization validation because the requirements was only to have a super user. I had to change CORs to run application locally to not have CORs blocking.
