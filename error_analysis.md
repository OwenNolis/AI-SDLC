# Error Analysis Report
Generated: Fri Mar  6 13:44:43 UTC 2026

## SDLC Flow Errors
2026-03-06T13:44:40.517Z  INFO 3165 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
RestTemplate injected: true
Test endpoint result: Test endpoint working
[ERROR] Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.319 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
[ERROR] be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint -- Time elapsed: 0.069 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/nonexistent": "{"correlationId":"9a4c98d2-8501-4e81-8165-a5158bffe307","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint(TestControllerIT.java:31)

[ERROR] be.ap.student.tickets.TestControllerIT.testBrokenEndpoint -- Time elapsed: 0.012 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/broken": "{"correlationId":"df910b9d-9087-444d-a9d4-62d8cf0759f3","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
--
[ERROR]   TestControllerIT.testNonExistentEndpoint:31 » InternalServer 500  on GET request for "http://localhost:38899/api/nonexistent": "{"correlationId":"9a4c98d2-8501-4e81-8165-a5158bffe307","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
[INFO] 
[ERROR] Tests run: 7, Failures: 0, Errors: 2, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.438 s
[INFO] Finished at: 2026-03-06T13:44:41Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.4:test (default-test) on project backend: 
## Direct Compilation Errors
[ERROR] COMPILATION ERROR : 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[19,12] cannot find symbol
  symbol:   class ResponseEntity
  location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[17,19] cannot find symbol
  symbol:   class UndefinedService
  location: class be.ap.student.tickets.controller.TicketController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[32,12] cannot find symbol
  symbol:   class List
  location: class be.ap.student.tickets.controller.TicketController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[10,12] cannot find symbol
  symbol:   class UndefinedType
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[15,12] cannot find symbol
  symbol:   class List
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[21,31] cannot find symbol
  symbol:   class NonExistentParameter
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[20,16] cannot find symbol
  symbol:   variable ResponseEntity
  location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[26,16] cannot find symbol
  symbol:   variable UndefinedClass
  location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[11,20] cannot find symbol
  symbol:   class UndefinedType
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[16,9] cannot find symbol
  symbol:   class MissingClass
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[16,35] cannot find symbol
  symbol:   class MissingClass
  location: class be.ap.student.tickets.service.BrokenService
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project backend: Compilation failure: Compilation failure: 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[19,12] cannot find symbol
[ERROR]   symbol:   class ResponseEntity
[ERROR]   location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[17,19] cannot find symbol
[ERROR]   symbol:   class UndefinedService
[ERROR]   location: class be.ap.student.tickets.controller.TicketController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[32,12] cannot find symbol
[ERROR]   symbol:   class List
[ERROR]   location: class be.ap.student.tickets.controller.TicketController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[10,12] cannot find symbol
[ERROR]   symbol:   class UndefinedType
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[15,12] cannot find symbol
[ERROR]   symbol:   class List
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[21,31] cannot find symbol
[ERROR]   symbol:   class NonExistentParameter
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[20,16] cannot find symbol
[ERROR]   symbol:   variable ResponseEntity
[ERROR]   location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TestController.java:[26,16] cannot find symbol
[ERROR]   symbol:   variable UndefinedClass
[ERROR]   location: class be.ap.student.tickets.controller.TestController
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[11,20] cannot find symbol
[ERROR]   symbol:   class UndefinedType
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[16,9] cannot find symbol
[ERROR]   symbol:   class MissingClass
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[16,35] cannot find symbol
[ERROR]   symbol:   class MissingClass
[ERROR]   location: class be.ap.student.tickets.service.BrokenService
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
## Test Failure Details
-------------------------------------------------------------------------------
Test set: be.ap.student.tickets.PersistentBrokenTest
-------------------------------------------------------------------------------
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.017 s -- in be.ap.student.tickets.PersistentBrokenTest
-------------------------------------------------------------------------------
Test set: be.ap.student.tickets.integration.BrokenIntegrationTest
-------------------------------------------------------------------------------
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.511 s -- in be.ap.student.tickets.integration.BrokenIntegrationTest
-------------------------------------------------------------------------------
Test set: be.ap.student.tickets.NewBrokenTest
-------------------------------------------------------------------------------
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in be.ap.student.tickets.NewBrokenTest
-------------------------------------------------------------------------------
Test set: be.ap.student.tickets.BrokenTest
-------------------------------------------------------------------------------
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.521 s -- in be.ap.student.tickets.BrokenTest
-------------------------------------------------------------------------------
Test set: be.ap.student.tickets.TestControllerIT
-------------------------------------------------------------------------------
Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.319 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint -- Time elapsed: 0.069 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/nonexistent": "{"correlationId":"9a4c98d2-8501-4e81-8165-a5158bffe307","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint(TestControllerIT.java:31)

be.ap.student.tickets.TestControllerIT.testBrokenEndpoint -- Time elapsed: 0.012 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/broken": "{"correlationId":"df910b9d-9087-444d-a9d4-62d8cf0759f3","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testBrokenEndpoint(TestControllerIT.java:38)

## HTTP Server Errors
2026-03-06T13:44:40.517Z  INFO 3165 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
RestTemplate injected: true
Test endpoint result: Test endpoint working
[ERROR] Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.319 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
[ERROR] be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint -- Time elapsed: 0.069 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/nonexistent": "{"correlationId":"9a4c98d2-8501-4e81-8165-a5158bffe307","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint(TestControllerIT.java:31)

[ERROR] be.ap.student.tickets.TestControllerIT.testBrokenEndpoint -- Time elapsed: 0.012 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:38899/api/broken": "{"correlationId":"df910b9d-9087-444d-a9d4-62d8cf0759f3","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testBrokenEndpoint(TestControllerIT.java:38)

[INFO] Running be.ap.student.tickets.integration.BrokenIntegrationTest
2026-03-06T13:44:40.706Z  INFO 3165 --- [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [be.ap.student.tickets.integration.BrokenIntegrationTest]: BrokenIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
