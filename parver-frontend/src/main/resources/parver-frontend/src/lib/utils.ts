import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Converts a `datetime-local` input value (e.g. "2026-03-04T10:00")
 * to an ISO 8601 string with the local timezone offset (e.g. "2026-03-04T10:00:00+01:00").
 *
 * Unlike `new Date(value).toISOString()` this preserves the user's local time
 * instead of converting to UTC.
 */
export function toLocalISOString(datetimeLocal: string): string {
  const date = new Date(datetimeLocal)
  const offsetMin = -date.getTimezoneOffset() // positive for UTC+
  const sign = offsetMin >= 0 ? "+" : "-"
  const h = String(Math.floor(Math.abs(offsetMin) / 60)).padStart(2, "0")
  const m = String(Math.abs(offsetMin) % 60).padStart(2, "0")
  const base = datetimeLocal.length <= 16 ? `${datetimeLocal}:00` : datetimeLocal
  return `${base}${sign}${h}:${m}`
}
