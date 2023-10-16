#include <cstring>
#include <cstdio>

#ifdef _WIN32
#include <fcntl.h>
#include <io.h>
#endif
#include "../include/jshm.h"

int main(int argc, char** argv) {
#ifdef _WIN32
    auto fromStdIn = false;
	for (int i = 0; i < argc; i++) if (strcmp(argv[i], "STDIN") == 0) fromStdIn = true;
	if (fromStdIn) {
		freopen(nullptr, "rb", stdin);
		freopen(nullptr, "wb", stdout);
#ifdef _WIN32
		_setmode(_fileno(stdin), _O_BINARY);
		_setmode(_fileno(stdout), _O_BINARY);
#endif
		char buf[1024];
		while (fread(buf, 1, 1024, stdin) > 0);
		return 0;
	}
#endif
	
	auto shm = jshm::shared_memory::create("/JSHM_TEST", 1024);
	char buf[1024];
	strcpy((char*)shm->address(), "Hello World!");
	strcpy(buf, (char*)shm->address());
	printf("%s\n", buf);
	return 0;
}
