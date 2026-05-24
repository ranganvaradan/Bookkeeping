interface StatusDotProps {
  connected: boolean
  label?: string
}

export default function StatusDot({ connected, label }: StatusDotProps) {
  return (
    <span className="inline-flex items-center gap-2 text-sm text-gray-600">
      <span
        className={`h-2.5 w-2.5 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`}
        aria-hidden
      />
      {label ?? (connected ? 'Connected' : 'Not connected')}
    </span>
  )
}
