export const ENTITY_TYPE_LABELS: Record<string, string> = {
  SOLE_PROP: 'Sole Proprietor',
  LLC: 'LLC',
  S_CORP: 'S-Corp',
  C_CORP: 'C-Corp',
}

export const ENTITY_TYPE_OPTIONS = [
  { value: 'SOLE_PROP', label: 'Sole Proprietor' },
  { value: 'LLC', label: 'LLC' },
  { value: 'S_CORP', label: 'S-Corp' },
  { value: 'C_CORP', label: 'C-Corp' },
] as const

export function formatEntityType(entityType: string): string {
  return ENTITY_TYPE_LABELS[entityType] ?? entityType
}

export function formatDateTime(iso: string | null): string {
  if (!iso) return 'Never'
  return new Date(iso).toLocaleString()
}

export function defaultSyncDateRange(): { fromDate: string; toDate: string } {
  const to = new Date()
  const from = new Date()
  from.setMonth(from.getMonth() - 3)

  return {
    fromDate: from.toISOString().slice(0, 10),
    toDate: to.toISOString().slice(0, 10),
  }
}
