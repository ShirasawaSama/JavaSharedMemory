#pragma once
#ifndef JAVA_SHARED_MEMORY_H
#define JAVA_SHARED_MEMORY_H

#if (defined (_WIN32)) || (defined (_WIN64))
#define JSHM_IS_WIN
#define WIN32_LEAN_AND_MEAN
#elif (defined (_linux)) || (defined (_linux_)) || (defined (__linux__))
#define JSHM_IS_LINUX
#define JSHM_IS_NIX
#elif (defined (_unix)) || (defined (_unix_))
#define JSHM_IS_UNIX
#define JSHM_IS_NIX
#endif

#include <type_traits>
#include <string>
#include <cstdint>
#include <cstddef>
#include <utility>

#ifdef JSHM_IS_WIN
#include <Windows.h>
#endif

namespace jshm
{
	enum class SharedMemoryOpenMethod
	{
		Create,
		Open
	};

	class SharedMemory
	{
	private:
		using Self = SharedMemory;

		std::int64_t size_;
		std::string name_;
		void *mem_;

#ifdef JSHM_IS_WIN
		HANDLE map_;
#endif

		static std::pair<HANDLE,void *> Init(const char *name,std::int64_t size,bool isCreate)
		{
#ifdef JSHM_IS_WIN
			LARGE_INTEGER li;
			li.QuadPart = size;
			HANDLE mapFile = nullptr;
			if(isCreate)
			{
				mapFile = ::CreateFileMappingA(INVALID_HANDLE_VALUE,nullptr,PAGE_READWRITE|SEC_COMMIT,li.HighPart,li.LowPart,name);
			}
			else
			{
				mapFile = ::OpenFileMappingA(SECTION_MAP_WRITE|SECTION_MAP_READ,FALSE,name);
			}
			if(!mapFile)
			{
				return {nullptr,nullptr};
			}
			void *p = ::MapViewOfFile(mapFile,SECTION_MAP_WRITE | SECTION_MAP_READ,0,0,static_cast<std::size_t>(size));
			if(!p)
			{
				::CloseHandle(mapFile);
				return {nullptr,nullptr};
			}
			return {mapFile,p};
#endif
		}
	public:

		inline std::int64_t GetSize() const noexcept
		{
			return this->size_;
		}

		inline const std::string &Name() const noexcept
		{
			return this->name_;
		}

		SharedMemory(std::string name,std::int64_t size,SharedMemoryOpenMethod method)
			:size_(size)
			,name_(std::move(name))
			,mem_(nullptr)
#ifdef JSHM_IS_WIN
			,map_(nullptr)
#endif
		{
			auto pair = this->Init(this->name_.c_str(),size,method == SharedMemoryOpenMethod::Create);
			if(!pair.second)
			{
				this->size_ = 0;
			}
			this->mem_ = pair.second;
			this->map_ = pair.first;
		}

		SharedMemory(const Self &other) = delete;

		SharedMemory(Self &&other) noexcept
			:size_(other.size_)
			,name_(std::move(other.name_))
			,mem_(other.mem_)
#ifdef JSHM_IS_WIN
			,map_(other.map_)
#endif
		{
			other.size_ = 0;
			other.mem_ = nullptr;
			other.map_ = nullptr;
		}

		inline Self &operator=(Self &&other) noexcept
		{
			if(this != std::addressof(other))
			{
#ifdef JSHM_IS_WIN
				if(this->mem_)
				{
					::UnmapViewOfFile(this->mem_);
					::CloseHandle(this->map_);
				}
				this->size_ = other.size_;
				this->name_ = std::move(other.name_);
				this->mem_ = other.mem_;
				this->map_ = other.map_;
				other.size_ = 0;
				other.mem_ = nullptr;
				other.map_ = nullptr;
#endif
			}
			return *this;
		}

		Self &operator=(const Self &other) = delete;

		~SharedMemory() noexcept
		{
#ifdef JSHM_IS_WIN
			if(this->mem_)
			{
				::UnmapViewOfFile(this->mem_);
				::CloseHandle(this->map_);
			}
#endif
		}

		operator void *() const noexcept
		{
			return this->mem_;
		}

		template<typename _T,typename _Check = decltype(reinterpret_cast<_T>(std::declval<void*>()))>
		inline _T AsPtr() const noexcept
		{
			return reinterpret_cast<_T>(this->mem_);
		}
	};
}

#endif
