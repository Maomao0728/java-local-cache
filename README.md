# Java Local Cache

一个基于 `Java` 实现的本地缓存组件项目，支持线程安全的 Key-Value 存储、TTL 过期控制、LRU 淘汰、空值缓存以及基础运行指标统计。

## 项目简介

本项目面向高并发读场景下数据库重复查询和热点 key 回源压力问题，设计并实现了一个轻量级本地缓存组件。项目重点围绕缓存设计中的几个常见问题展开，包括：

- 本地 Key-Value 缓存存储
- TTL 过期控制
- LRU 淘汰策略
- 空值缓存防止缓存穿透
- key 级并发控制缓解缓存击穿
- 命中次数、未命中次数和回源次数统计

## 技术栈

- Java 17
- Spring Boot
- ConcurrentHashMap
- Maven
- JUnit 5

## 核心功能

- 基于 `ConcurrentHashMap` 实现线程安全的本地缓存存储
- 支持缓存条目过期时间（TTL）
- 支持最近最少使用（LRU）淘汰策略
- 支持空值缓存，缓解缓存穿透问题
- 支持 key 级并发控制与双重检查，减少热点 key 并发回源
- 提供命中率、未命中次数和回源次数等基础统计能力
- 提供简单的 REST 接口用于缓存读写和状态查看

## 项目结构

```text
src
├─ main
│  ├─ java
│  │  └─ com.example.highconcurrencycache
│  │     ├─ cache
│  │     │  ├─ CacheEntry.java
│  │     │  └─ LocalCache.java
│  │     ├─ config
│  │     │  └─ CacheConfig.java
│  │     ├─ controller
│  │     │  └─ CacheController.java
│  │     ├─ dto
│  │     │  └─ CachePutRequest.java
│  │     ├─ model
│  │     │  └─ UserData.java
│  │     ├─ service
│  │     │  ├─ CacheService.java
│  │     │  └─ MockDatabaseService.java
│  │     └─ HighConcurrencyCacheApplication.java
│  └─ resources
│     └─ application.yml
└─ test
   └─ java
      └─ com.example.highconcurrencycache
         └─ HighConcurrencyCacheApplicationTests.java
