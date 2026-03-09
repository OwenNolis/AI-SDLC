# Error Analysis Report
Generated: Mon Mar  9 10:57:38 UTC 2026

## SDLC Flow Errors
[INFO] 
[INFO] --- compiler:3.14.1:compile (default-compile) @ backend ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 18 source files with javac [debug parameters release 21] to target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[31,71] cannot find symbol
  symbol:   method getFormattedStatus()
  location: variable saved of type be.ap.student.tickets.domain.SupportTicket
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[51,36] cannot find symbol
  symbol:   method countByStatus(be.ap.student.tickets.domain.TicketStatus)
  location: variable repository of type be.ap.student.tickets.repo.SupportTicketRepository
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[17,23] incompatible types: inference variable E has incompatible bounds
    equality constraints: java.lang.Integer
    lower bounds: java.lang.String
[INFO] 3 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.541 s
[INFO] Finished at: 2026-03-09T10:57:35Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project backend: Compilation failure: Compilation failure: 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[31,71] cannot find symbol
[ERROR]   symbol:   method getFormattedStatus()
[ERROR]   location: variable saved of type be.ap.student.tickets.domain.SupportTicket
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[51,36] cannot find symbol
[ERROR]   symbol:   method countByStatus(be.ap.student.tickets.domain.TicketStatus)
[ERROR]   location: variable repository of type be.ap.student.tickets.repo.SupportTicketRepository
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[17,23] incompatible types: inference variable E has incompatible bounds
[ERROR]     equality constraints: java.lang.Integer
[ERROR]     lower bounds: java.lang.String
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
## Direct Compilation Errors
[ERROR] COMPILATION ERROR : 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[31,71] cannot find symbol
  symbol:   method getFormattedStatus()
  location: variable saved of type be.ap.student.tickets.domain.SupportTicket
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[51,36] cannot find symbol
  symbol:   method countByStatus(be.ap.student.tickets.domain.TicketStatus)
  location: variable repository of type be.ap.student.tickets.repo.SupportTicketRepository
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[17,23] incompatible types: inference variable E has incompatible bounds
    equality constraints: java.lang.Integer
    lower bounds: java.lang.String
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project backend: Compilation failure: Compilation failure: 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/controller/TicketController.java:[31,71] cannot find symbol
[ERROR]   symbol:   method getFormattedStatus()
[ERROR]   location: variable saved of type be.ap.student.tickets.domain.SupportTicket
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[51,36] cannot find symbol
[ERROR]   symbol:   method countByStatus(be.ap.student.tickets.domain.TicketStatus)
[ERROR]   location: variable repository of type be.ap.student.tickets.repo.SupportTicketRepository
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/BrokenService.java:[17,23] incompatible types: inference variable E has incompatible bounds
[ERROR]     equality constraints: java.lang.Integer
[ERROR]     lower bounds: java.lang.String
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
## Test Failure Output
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------------< be.ap.student:backend >------------------------
[INFO] Building AI-SDLC Backend 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ backend ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.14.1:compile (default-compile) @ backend ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 18 source files with javac [debug parameters release 21] to target/classes
[INFO] Some messages have been simplified; recompile with -Xdiags:verbose to get full output
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[44,31] incompatible types: java.lang.String cannot be converted to java.util.UUID
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[66,34] incompatible types: java.lang.String cannot be converted to java.util.UUID
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[88,34] incompatible types: java.lang.String cannot be converted to java.util.UUID
[INFO] 3 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.886 s
[INFO] Finished at: 2026-03-09T10:11:28Z
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project backend: Compilation failure: Compilation failure: 
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[44,31] incompatible types: java.lang.String cannot be converted to java.util.UUID
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[66,34] incompatible types: java.lang.String cannot be converted to java.util.UUID
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[88,34] incompatible types: java.lang.String cannot be converted to java.util.UUID
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
