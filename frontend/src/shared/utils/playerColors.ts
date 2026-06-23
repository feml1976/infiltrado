const PALETTE = [
  '#6366f1',
  '#ec4899',
  '#f59e0b',
  '#10b981',
  '#3b82f6',
  '#f97316',
  '#8b5cf6',
  '#14b8a6',
  '#ef4444',
]

export function getPlayerColor(ordenTurno: number): string {
  return PALETTE[(ordenTurno - 1) % PALETTE.length]
}
