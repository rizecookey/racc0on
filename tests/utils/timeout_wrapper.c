#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <stddef.h>
#include <signal.h>

// This program acts as a wrapper for the actual binary that exits after a timeout of 1 minute.
// It otherwise replicates the exit behaviour of the child process.

int main(int argc, char *argv[]) {
  pid_t program_pid = fork();
  if (program_pid == 0) {
    execl(BIN, BIN, (char *) NULL);
    return 0;
  }

  pid_t timeout_killer_pid = fork();
  if (timeout_killer_pid == 0) {
    sleep(60);
    kill(program_pid, SIGINT);
    return 0;
  }

  int program_status;
  waitpid(program_pid, &program_status, 0);
  kill(timeout_killer_pid, SIGINT);
  waitpid(timeout_killer_pid, NULL, 0);

  if (WIFSIGNALED(program_status)) {
    kill(0, WTERMSIG(program_status));
    return 0;
  }

  return WEXITSTATUS(program_status);
}