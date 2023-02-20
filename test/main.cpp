#include <jshm.h>

int main(int argc, char const *argv[])
{
    jshm::SharedMemory memory{"Hello",4096,jshm::SharedMemoryOpenMethod::Create};
    std::memcpy(memory,"Hello",6);
    std::puts(memory.AsPtr<char*>()); 
    jshm::SharedMemory other{"Hello",4096,jshm::SharedMemoryOpenMethod::Open};
    std::puts(other.AsPtr<char*>());  
    return 0;
}