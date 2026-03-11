[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /home/runner/work/AI-SDLC/AI-SDLC/backend/src/main/java/be/ap/student/tickets/service/TicketService.java:[44,36] cannot find symbol
  symbol:   method getUserId()
  location: variable req of type be.ap.student.tickets.dto.CreateTicketRequest
[INFO] 1 error
--
[ERROR]   symbol:   method getUserId()
[ERROR]   location: variable req of type be.ap.student.tickets.dto.CreateTicketRequest
[ERROR] 
[ERROR] -> [Help 1]
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile (default-compile) on project backend: Compilation failure
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
FAIL src/ui/TicketForm.test.tsx
  ● Test suite failed to run

    [96msrc/ui/TicketForm.tsx[0m:[93m29[0m:[93m46[0m - [91merror[0m[90m TS2339: [0mProperty 'handleSubmit' does not exist on type 'Props'.
    [7m29[0m export function TicketForm({ loading, error, handleSubmit }: Props) {
    [7m  [0m [91m                                             ~~~~~~~~~~~~[0m
    [96msrc/ui/TicketForm.tsx[0m:[93m50[0m:[93m24[0m - [91merror[0m[90m TS2552: [0mCannot find name 'onSubmit'. Did you mean 'canSubmit'?
    [7m50[0m         if (canSubmit) onSubmit(values);
    [7m  [0m [91m                       ~~~~~~~~[0m
      [96msrc/ui/TicketForm.tsx[0m:[93m37[0m:[93m9[0m
        [7m37[0m   const canSubmit = Object.keys(clientErrors).length === 0 && !loading;
--
FAIL src/ui/__generated__/feature-001-support-ticket.TicketForm.test.tsx
/home/runner/work/AI-SDLC/AI-SDLC/frontend/src/App.tsx
/home/runner/work/AI-SDLC/AI-SDLC/frontend/src/ui/TicketForm.tsx
  4:7   error  'unusedHelper' is assigned a value but never used  @typescript-eslint/no-unused-vars
  4:26  error  Unexpected any. Specify a different type           @typescript-eslint/no-explicit-any
  29:46  error  'handleSubmit' is defined but never used  @typescript-eslint/no-unused-vars
✖ 3 problems (3 errors, 0 warnings)
