import type { HttpResponse } from './interceptor';

export type QueryParams = Record<string, unknown>;

export interface TableResponse<T> extends HttpResponse<T> {
  rows?: T[];
  total?: number;
}

export interface UserAuthRolesResponse {
  user?: Record<string, unknown>;
  roles?: Record<string, unknown>[];
}
