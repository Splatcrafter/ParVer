import { motion } from "framer-motion"

export type SpotStatus = "INACTIVE" | "OCCUPIED" | "AVAILABLE" | "BOOKED"

interface ParkingSpotProps {
  number: number
  x: number
  y: number
  width: number
  height: number
  rotation?: number
  delay?: number
  status?: SpotStatus
  onClick?: () => void
}

const STATUS_COLORS: Record<SpotStatus, { fill: string; stroke: string; text: string }> = {
  INACTIVE: { fill: "#e5e5e5", stroke: "#d4d4d4", text: "#a3a3a3" },
  OCCUPIED: { fill: "#fecaca", stroke: "#f87171", text: "#dc2626" },
  AVAILABLE: { fill: "#bbf7d0", stroke: "#4ade80", text: "#16a34a" },
  BOOKED: { fill: "#fecaca", stroke: "#f87171", text: "#dc2626" },
}

export function ParkingSpot({
  number,
  x,
  y,
  width,
  height,
  rotation = 0,
  delay = 0,
  status = "INACTIVE",
  onClick,
}: ParkingSpotProps) {
  const cx = x + width / 2
  const cy = y + height / 2
  const colors = STATUS_COLORS[status]

  return (
    <motion.g
      transform={`rotate(${rotation}, ${cx}, ${cy})`}
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.4, delay, ease: "easeOut" }}
      onClick={onClick}
      style={{ cursor: onClick ? "pointer" : "default" }}
    >
      <rect
        x={x}
        y={y}
        width={width}
        height={height}
        rx={3}
        fill={colors.fill}
        stroke={colors.stroke}
        strokeWidth={1.5}
      />
      <text
        x={cx}
        y={cy}
        textAnchor="middle"
        dominantBaseline="central"
        fontSize={13}
        fontWeight={600}
        fontFamily="system-ui, -apple-system, sans-serif"
        fill={colors.text}
      >
        {number}
      </text>
    </motion.g>
  )
}
