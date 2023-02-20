#pragma once
#ifndef JAVA_SHARED_MEMORY_H
#define JAVA_SHARED_MEMORY_H

#ifdef _WIN32
#include <windows.h>
#endif

namespace jshm {
	class shared_memory {
	public:
		int size() const noexcept { return _size; }
		const char* name() const noexcept { return _name; }
		
		~shared_memory() {
#ifdef _WIN32
			UnmapViewOfFile(pBuf);
			CloseHandle(hMapFile);
#endif
		}

		void* address() const noexcept {
#ifdef _WIN32
			return pBuf;
#endif
		}

		static shared_memory* create(const char* name, int size) { return init(name, size, true); }
		static shared_memory* open(const char* name, int size) { return init(name, size, false); }
		
	private:
		int _size;
		const char* _name;
		
#ifdef _WIN32
		HANDLE hMapFile;
		LPTSTR pBuf;
		
		shared_memory(HANDLE hMapFile, LPTSTR pBuf, int size, const char* name) : hMapFile(hMapFile), pBuf(pBuf), _size(size), _name(name) { }
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
#endif
		}
	};
}

#endif
