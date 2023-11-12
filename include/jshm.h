#pragma once
#ifndef JAVA_SHARED_MEMORY_H
#define JAVA_SHARED_MEMORY_H

#ifdef _WIN32
#include <windows.h>
#elif __linux__ || __APPLE__
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#endif

namespace jshm {
    class shared_memory {
    public:
        [[nodiscard]] int size() const noexcept { return _size; }
        [[nodiscard]] const char* name() const noexcept { return _name; }
        
        ~shared_memory() {
#ifdef _WIN32
            UnmapViewOfFile(_pBuf);
            CloseHandle(hMapFile);
#elif __linux__ || __APPLE__
            if (_pBuf) munmap(_pBuf, (size_t) _size);
            if (_isCreate) shm_unlink(_name);
#endif
            delete[] _name;
        }

        [[nodiscard]] void* address() const noexcept { return _pBuf; }

        [[nodiscard]] static shared_memory* create(const char* name, int size) { return init(name, size, true); }
        [[nodiscard]] static shared_memory* open(const char* name, int size) { return init(name, size, false); }

    private:
        int _size;
        const char* _name;

#ifdef _WIN32
        HANDLE hMapFile;
        LPTSTR _pBuf;

        shared_memory(HANDLE hMapFile, LPTSTR pBuf, int size, const char* name) : hMapFile(hMapFile), _pBuf(pBuf), _size(size) {
#elif __linux__ || __APPLE__
        void* _pBuf = nullptr;
        bool _isCreate;
        shared_memory(void* pBuf, int size, const char* name, bool isCreate) : _size(size), _pBuf(pBuf), _isCreate(isCreate) {
#endif
            _name = new char[strlen(name) + 1];
            strcpy((char*) _name, name);
        }

        static shared_memory* init(const char* name, int size, bool isCreate) {
#ifdef _WIN32
            auto hMapFile = isCreate ? CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE | SEC_COMMIT, 0, size, name)
                : OpenFileMapping(SECTION_MAP_WRITE | SECTION_MAP_READ, FALSE, name);
            if (hMapFile == NULL) return nullptr;
            auto pBuf = (LPTSTR)MapViewOfFile(hMapFile, SECTION_MAP_WRITE | SECTION_MAP_READ, 0, 0, size);
            if (pBuf == NULL) {
                CloseHandle(hMapFile);
                throw nullptr;
            }
            return new shared_memory(hMapFile, pBuf, size, name);
#elif __linux__ || __APPLE__
            auto mode = O_RDWR;
            if (isCreate) mode |= O_CREAT | O_EXCL;
            auto fd = shm_open(name, mode, S_IRUSR | S_IWUSR);
            if (fd == -1) return nullptr;
            if (isCreate && ftruncate(fd, size) == -1) {
                close(fd);
                return nullptr;
            }
            auto pBuf = mmap(nullptr, (size_t) size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
            if (pBuf == MAP_FAILED) {
                close(fd);
                return nullptr;
            }
            return new shared_memory(pBuf, size, name, isCreate);
#endif
        }
    };
}

#endif
