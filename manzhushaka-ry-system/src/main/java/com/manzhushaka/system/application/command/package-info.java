/**
 * 系统域 - 应用层 - 命令
 *
 * <p>封装写操作的请求参数，代表用户的"意图"（intent）。
 * 每个 Command 对应一个用例（use case），由应用服务处理。
 * 使用 {@link jakarta.validation.Valid} 注解进行参数校验。</p>
 */
package com.manzhushaka.system.application.command;