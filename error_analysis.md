# Error Analysis Report
Generated: Fri Mar  6 11:00:33 UTC 2026

## SDLC Flow Errors
[INFO] 
[INFO] --- compiler:3.14.1:compile (default-compile) @ backend ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 17 source files with javac [debug parameters release 21] to target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
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
[INFO] 11 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.104 s
[INFO] Finished at: 2026-03-06T11:00:31Z
[INFO] ------------------------------------------------------------------------
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
