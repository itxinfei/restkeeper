/** mock 路由上下文与处理器类型（B 端 / C 端共用） */

export interface MockCtx {
  url: string
  params: Record<string, unknown>
  body: any
}

export type MockHandler = (ctx: MockCtx) => unknown

export interface MockRoute {
  method: string
  pattern: RegExp
  handler: MockHandler
}
