import { formatEntityType } from './utils'

interface EntityTypeBadgeProps {
  entityType: string
}

export default function EntityTypeBadge({ entityType }: EntityTypeBadgeProps) {
  return (
    <span className="inline-flex rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700">
      {formatEntityType(entityType)}
    </span>
  )
}
