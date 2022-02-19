# 规则平台

开发规则平台的意义：
>1. 低代码的，可视化的规则配置平台  
>2. 快速集成:在依赖其它系统接口的时候，不再需要额外工作量，只需要注册接口，即可完成集成  
>3. 在保证运行结果正确的情况下，优化数据结构，减少内存开销，提升运行速度  

*试用链接：数据每天24点会重置* http://39.103.133.197/rule-front/

**[规则引擎文档](https://github.com/zjb-it/rule-engine/blob/master/README.md)**

**[规则平台前端文档](https://github.com/zjb-it/rule-platform-front/blob/master/README.md)**

**[更多开源软件 https://www.gamewar.cn/](https://www.gamewar.cn/)**

## 基础概念

**[规则引擎文档](https://github.com/zjb-it/rule-engine/blob/master/README.md)**

## 功能架构

![功能架构](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/module.png)

1. 基础组件：主要被不同形式的规则引用
2. 规则配置：提供包括规则，规则集，决策表等可视化规则配置方式
3. 规则执行：主要提供发布和执行规则的入口

## 设计原则
### 规则配置主要面向非技术用户  
1. 所有条件表达式都通过UI配置，支持常用二元操作符

2. 作为能力补偿，函数分为系统函数和注册函数，作为基础计算能力
>a. 系统函数需要开发人员在规则平台进行二次开发，注册函数把注册现有的接口封装为函数  
>b. 注册函数需要开发人员提供接口的调用方式，在规则平台内进行注册，注册后封装为函数即可进行规则计算

3. 自研规则引擎，率先支持规则集与复杂决策表，用于可配置型规则解析、执行，保障可扩展性

4. 规则平台的可视化配置围绕规则引擎能力展开，采用“一次配置，多次部署”的思想

### 规则计算服务的高稳定性

1. 规则计算服务与规则配置服务分离，计算服务为无状态黑盒服务，输入上下文，输出规则结果(todo)

2. 规则配置服务围绕除规则计算外的其他生命周期环节开展工作，不影响规则计算服务(todo)

3. 规则配置可以进行持久化存储，方便通过UI进行配置，但是规则引擎脱离配置(todo)

## 技术架构

![技术架构](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/design.png)

1. 业务层：提供规则管理系统的功能
3. 逻辑层：完成规则逻辑运算，其中规则引擎是计算核心，函数是计算依赖的资源
4. 基础服务：管理用户及用户权限
5. 基础设施：mysql负责持久化

## 时序图

### 规则发布时序图
![规则发布时序图](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/configTime.png)

### 规则调用时序图

![规则调用时序图](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/exeTime.png)

## 数据库设计

### 基础组件

![基础组件](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/basic.png)

### 规则

![基础组件](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/rule.png)

### 规则集

![基础组件](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/ruleset.png)

### 决策表

![基础组件](https://github.com/zjb-it/rule-platform-server/blob/master/screenshot/decision.png)
