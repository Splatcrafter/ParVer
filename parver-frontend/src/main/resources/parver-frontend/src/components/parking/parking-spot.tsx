import { motion } from "framer-motion"

interface ParkingSpotProps {
  number: number
  x: number
  y: number
  width: number
  height: number
  rotation?: number
  delay?: number
}

export function ParkingSpot({
  number,
  x,
  y,
  width,
  height,
  rotation = 0,
  delay = 0,
}: ParkingSpotProps) {
  const cx = x + width / 2
  const cy = y + height / 2

  return (
    <motion.g
      transform={`rotate(${rotation}, ${cx}, ${cy})`}
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.4, delay, ease: "easeOut" }}
    >
      <rect
        x={x}
        y={y}
        width={width}
        height={height}
        rx={3}
        fill="#f5f5f5"
        stroke="#d4d4d4"
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
        fill="#404040"
      >
        {number}
      </text>
    </motion.g>
  )
}
