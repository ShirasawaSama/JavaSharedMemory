# JavaSharedMemory [![](https://www.jitpack.io/v/ShirasawaSama/JavaSharedMemory.svg)](https://www.jitpack.io/#ShirasawaSama/JavaSharedMemory)

Shared memory between processes. (Java, C++, Rust, etc) Based on Java 19 [Foreign Function & Memory API](https://openjdk.org/jeps/434)

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

Just include `jshm.h`.

```cpp
#include <jshm.h>

int main() {
    jshm::SharedMemory shm{"Hello",4096,jshm::SharedMemoryOpenMethod::Create};
    // jshm::SharedMemory shm{"Hello",4096,jshm::SharedMemoryOpenMethod::Open};
    char *buf{shm.GetPtr<char*>()};
    strcpy(buf, "Hello, World!");
}
```

## Author

Shirasawa

## License

[MIT](LICENSE)
