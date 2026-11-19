import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (fallos, error) => {
        if (error instanceof ApiError && error.status >= 500) return fallos < 1
        if (error instanceof ApiError && error.status === 0) return fallos < 1
        return false
      },
      refetchOnWindowFocus: false,
    },
    mutations: { retry: false },
  },
})
