import clsx from 'clsx'
import { useId, type ComponentProps } from 'react'

type AreaTextoProps = Omit<ComponentProps<'textarea'>, 'id'> & {
  etiqueta: string
  error?: string
}

export function AreaTexto({ etiqueta, error, className, ...props }: AreaTextoProps) {
  const id = useId()
  const errorId = `${id}-error`

  return (
    <label className="block" htmlFor={id}>
      <span className="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-300">
        {etiqueta}
      </span>
      <textarea
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={clsx(
          'block min-h-24 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900',
          'focus:border-marca-500 focus:ring-2 focus:ring-marca-200 focus:outline-none',
          'dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:focus:ring-marca-900',
          error && 'border-red-500 focus:border-red-500 focus:ring-red-200',
          className,
        )}
        {...props}
      />
      {error && (
        <span id={errorId} role="alert" className="mt-1 block text-sm text-red-600">
          {error}
        </span>
      )}
    </label>
  )
}
