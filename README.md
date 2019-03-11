# geekhalo-ddd
> geekhalo ddd 项目，主要对 DDD 相关理念进行抽象，并提供多种基础服务。

项目文档以及项目主地址见：https://gitee.com/litao851025/geekhalo-ddd

整个项目的设计理念遵循：
1. 梳理 DDD 相关理念，推进 DDD 的学习；
2. 寻求 DDD 最佳实践，分解、抽象并对其进行封装，将重用性最大化；
3. 使用技术手段，提供对最佳实践的支持，最大程度的降低代码量。使程序员回归业务本质。

#### 介绍
针对 DDD 不同层次，对整体项目进行划分。

1. DDD Lite

DDD Lite 部分，聚焦于战术层，对实体、值对象、领域服务、领域事件、模块、聚合、工厂、仓库的落地提供支撑。
该部分主要由：

a. **gh-ddd-lite**

该模块，主要完成对DDD的战术封装。

b. **gh-ddd-lite-spring**

该模块，主要提供 spring 的支持。

c. **gh-ddd-lite-codegen**

该模块，完成 DDD 样板代码的自动生成。

d. **gh-ddd-lite-demo**

demo 部分，用于展示整个模块的正确使用方式。


#### 使用说明
