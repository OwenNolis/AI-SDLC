# Error Analysis Report
Generated: Fri Mar  6 12:22:28 UTC 2026

## SDLC Flow Errors
2026-03-06T12:22:25.517Z  INFO 3141 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
RestTemplate injected: true
Test endpoint result: Test endpoint working
[ERROR] Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.317 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
[ERROR] be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint -- Time elapsed: 0.070 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:40327/api/nonexistent": "{"correlationId":"f3c6eb56-992c-47ab-9556-d1803fbe5824","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint(TestControllerIT.java:31)

[ERROR] be.ap.student.tickets.TestControllerIT.testBrokenEndpoint -- Time elapsed: 0.013 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:40327/api/broken": "{"correlationId":"68c3eece-9515-4581-b3a1-aa69671eed4c","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
--
[ERROR]   TestControllerIT.testNonExistentEndpoint:31 » InternalServer 500  on GET request for "http://localhost:40327/api/nonexistent": "{"correlationId":"f3c6eb56-992c-47ab-9556-d1803fbe5824","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
[INFO] 
[ERROR] Tests run: 7, Failures: 0, Errors: 2, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.088 s
[INFO] Finished at: 2026-03-06T12:22:26Z
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
## HTTP Server Errors
2026-03-06T12:22:25.517Z  INFO 3141 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
RestTemplate injected: true
Test endpoint result: Test endpoint working
[ERROR] Tests run: 3, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.317 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
[ERROR] be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint -- Time elapsed: 0.070 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:40327/api/nonexistent": "{"correlationId":"f3c6eb56-992c-47ab-9556-d1803fbe5824","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testNonExistentEndpoint(TestControllerIT.java:31)

[ERROR] be.ap.student.tickets.TestControllerIT.testBrokenEndpoint -- Time elapsed: 0.013 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:40327/api/broken": "{"correlationId":"68c3eece-9515-4581-b3a1-aa69671eed4c","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677)
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:304)
	at be.ap.student.tickets.TestControllerIT.testBrokenEndpoint(TestControllerIT.java:38)

[INFO] Running be.ap.student.tickets.integration.BrokenIntegrationTest
2026-03-06T12:22:25.701Z  INFO 3141 --- [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [be.ap.student.tickets.integration.BrokenIntegrationTest]: BrokenIntegrationTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
