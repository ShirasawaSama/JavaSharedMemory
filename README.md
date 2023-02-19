# JavaSharedMemory [![](https://www.jitpack.io/v/ShirasawaSama/JavaSharedMemory.svg)](https://www.jitpack.io/#ShirasawaSama/JavaSharedMemory)

Shared memory between processes. (Java, C++, Rust, etc)

> **Warning**
> Currently only Windows is supported. Support for other platforms will be added later.

## Usage

### Java

```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.ShirasawaSama:JavaSharedMemory:<version>'
}
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
    auto buf = (char*)shm;
    strcpy(buf, "Hello, World!");
    delete shm;
}
```

## Author

Shirasawa

## License

[MIT](LICENSE)
