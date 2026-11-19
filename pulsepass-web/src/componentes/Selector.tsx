import clsx from 'clsx'
import { useId, type ComponentProps, type ReactNode } from 'react'

type SelectorProps = Omit<ComponentProps<'select'>, 'id'> & {
  etiqueta: string
  error?: string
  children: ReactNode
}

export function Selector({
  etiqueta,
  error,
  children,
  className,
  required,
  ...props
}: SelectorProps) {
  const id = useId()
  const errorId = `${id}-error`

  return (
    <label className="block" htmlFor={id}>
      <span className="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-300">
        {etiqueta}
        {required && <span className="ml-1 text-red-600">*</span>}
      </span>
      <select
        id={id}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={clsx(
          'mt-0 block min-h-11 w-full rounded-lg border bg-white px-3 py-2 text-slate-900',
          'focus:border-marca-500 focus:ring-2 focus:ring-marca-200 focus:outline-none',
          'dark:bg-slate-900 dark:text-slate-100 dark:focus:ring-marca-900',
          error
            ? 'border-red-500 focus:border-red-500 focus:ring-red-200'
            : 'border-slate-300 dark:border-slate-700',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      {error && (
        <span id={errorId} role="alert" className="mt-1 block text-sm text-red-600">
          {error}
        </span>
      )}
    </label>
  )
}
