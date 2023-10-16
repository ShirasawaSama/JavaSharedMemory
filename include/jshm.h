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
			UnmapViewOfFile(pBuf);
			CloseHandle(hMapFile);
#elif __linux__ || __APPLE__
            if (pBuf) munmap(pBuf, _size);
            if (_isCreate) shm_unlink(_name);
#endif
		}

		[[nodiscard]] void* address() const noexcept { return pBuf; }

        [[nodiscard]] static shared_memory* create(const char* name, int size) { return init(name, size, true); }
        [[nodiscard]] static shared_memory* open(const char* name, int size) { return init(name, size, false); }
		
	private:
		int _size;
		const char* _name;
		
#ifdef _WIN32
		HANDLE hMapFile;
		LPTSTR pBuf;
		
		shared_memory(HANDLE hMapFile, LPTSTR pBuf, int size, const char* name) : hMapFile(hMapFile), pBuf(pBuf), _size(size), _name(name) { }
#elif __linux__ || __APPLE__
        void* pBuf = nullptr;
        bool _isCreate;
        shared_memory(void* pBuf, int size, const char* name, bool isCreate) : pBuf(pBuf), _size(size), _name(name), _isCreate(isCreate) { }
#endif

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
            auto fd = isCreate ? shm_open(name, O_CREAT | O_RDWR, S_IRUSR | S_IWUSR) : shm_open(name, O_RDWR, S_IRUSR | S_IWUSR);
            if (fd == -1) return nullptr;
            if (isCreate && ftruncate(fd, size) == -1) {
                close(fd);
                return nullptr;
            }
            auto pBuf = mmap(nullptr, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
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
