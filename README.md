# JavaSharedMemory [![Jitpack](https://www.jitpack.io/v/ShirasawaSama/JavaSharedMemory.svg)](https://www.jitpack.io/#ShirasawaSama/JavaSharedMemory)

Shared memory between processes. (Java, C++, Rust, etc.) Based on Java 19 [Foreign Function & Memory API](https://openjdk.org/jeps/434)

Support Windows *(CreateFileMapping)*, Linux and macOS *(mmap and shmget)*.

> **Note**
> Compared with standard input and output (anonymous pipes) on Windows, more than 2x performance improvement.

## Usage

### Java

```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.ShirasawaSama:JavaSharedMemory:<version>'
}

tasks.withType(JavaCompile).each {
    it.options.compilerArgs.add('--enable-preview')
}

// kotlin script
// tasks.withType<JavaCompile> {
//     options.compilerArgs = options.compilerArgs + listOf("--enable-preview")
// }
```

```java
import cn.apisium.shm;

public class Main {
    public static void main(String[] args) {
        // var shm = SharedMemory.open("test", 1024)
        try (var shm = SharedMemory.create("test", 1024)) {
            var buf = shm.toByteBuffer();
            buf.put("Hello, World!".getBytes());
        }
    }
}
```

### C++

```bash
git submodule add https://github.com/ShirasawaSama/JavaSharedMemory.git
```

CMakeLists.txt:

```CMakeLists.txt
add_subdirectory(JavaSharedMemory)
target_link_libraries(${PROJECT_NAME} java_shared_memory)
```

main.cpp:

```cpp
#include <jshm.h>

int main() {
    auto shm = jshm::shared_memory::create("test", 1024);
    // auto shm = jshm::shared_memory::open("test", 1024);
    strcpy((char*)shm->address(), "Hello, World!");
    delete shm;
}
```

## Author

Shirasawa

## License

[MIT](LICENSE)
