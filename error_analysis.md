# Error Analysis Report
Generated: Fri Mar  6 08:31:00 UTC 2026

## SDLC Flow Errors
2026-03-06T08:30:57.497Z  INFO 3211 --- [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-03-06T08:30:57.497Z  INFO 3211 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-03-06T08:30:57.498Z  INFO 3211 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.260 s <<< FAILURE! -- in be.ap.student.tickets.TestControllerIT
[ERROR] be.ap.student.tickets.TestControllerIT.testEndpoint -- Time elapsed: 0.247 s <<< ERROR!
org.springframework.web.client.HttpServerErrorException$InternalServerError: 500  on GET request for "http://localhost:33143/api/test": "{"correlationId":"0c30de15-8d0f-4e78-8308-231b15f9ee44","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
	at org.springframework.web.client.HttpServerErrorException.create(HttpServerErrorException.java:103)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:148)
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:120)
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:807)
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:756)
--
[ERROR]   TestControllerIT.testEndpoint:23 » InternalServer 500  on GET request for "http://localhost:33143/api/test": "{"correlationId":"0c30de15-8d0f-4e78-8308-231b15f9ee44","code":"INTERNAL_ERROR","message":"Something went wrong","fieldErrors":[]}"
[INFO] 
[ERROR] Tests run: 5, Failures: 0, Errors: 1, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.511 s
[INFO] Finished at: 2026-03-06T08:30:58Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.4:test (default-test) on project backend: 
