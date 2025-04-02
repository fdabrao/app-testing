# This is the project test for QIMA hiring process

This project are based in test requirements to create a fullstack product management with frontend, backend and database.

## 1. Setup Environment

Make sure you have Java 17+ and a [compatible Docker environment](https://www.testcontainers.org/supported_docker_environment/) installed.

For example:

```shell
$ java --version
openjdk version "17.0.4" 2022-07-19
OpenJDK Runtime Environment Temurin-17.0.4+8 (build 17.0.4+8)
OpenJDK 64-Bit Server VM Temurin-17.0.4+8 (build 17.0.4+8, mixed mode, sharing)
$ docker version
...
Server: Docker Desktop 4.12.0 (85629)
 Engine:
  Version:          20.10.17
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.17.11
...
```

## 2. Setup Project

* Clone the repository

```shell
git clone https://github.com/fdbrao/app-testing.git
cd app-testing
```

## 3. Run Tests

Run the command to run all the tests.

```shell
$ ./run-tests.sh --all
```

The tests should pass with end up this:

> [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.530 s -- in com.fdabrao.app.controller.AuthControllerIntegrationTest
> [INFO]
> [INFO] Results:
> [INFO]
> [INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
> [INFO]
> [INFO] ------------------------------------------------------------------------
> [INFO] BUILD SUCCESS
> [INFO] -----------------------------------------------------------------------
> [INFO] Total time: XX s
> [INFO] Finished at: XXXX-XX-XXTXX:XX:XX-XX:XX
> [INFO] ------------------------------------------------------------------------

## 4. Run System

Run the command to run the system.

```shell
$ ./start-system.sh
```

CTRL+C to stop

To access the system, open http://localhost:4200 in browser.

Use this credential: username: admin / password: admin

# Development minor informations

The project has only the integration test because we do not have business validation. So, no unit test.
