import { useCallback, useEffect, useState } from "react"
import { ParkingSpot, type SpotStatus } from "@/components/parking/parking-spot"
import { parkingApi } from "@/lib/api"
import type { components } from "@/lib/api-types"

type ParkingSpace = components["schemas"]["ParkingSpace"]

// Parking spot dimensions
const SPOT_W = 90
const SPOT_H = 36
const SPOT_GAP = 4
const ANGLE = 25

// Layout: small gap between spots and lane
const PAD = 8
const LANE_GAP = 4
const LANE_W = 50
const LANE_X = PAD + SPOT_W + LANE_GAP
const RIGHT_X = LANE_X + LANE_W + LANE_GAP
const SVG_W = RIGHT_X + SPOT_W + PAD
const TOP_Y = 20
const CONTENT_Y = TOP_Y + 6

// Left column: flush against left edge of lane
const leftSpots = [53, 54, 55, 56, 57, 58, 59].map((num, i) => ({
  number: num,
  x: PAD,
  y: CONTENT_Y + i * (SPOT_H + SPOT_GAP),
}))

const TOTAL_H = 7 * SPOT_H + 6 * SPOT_GAP
const SVG_H = CONTENT_Y + TOTAL_H + PAD

// Right column: flush against right edge of lane, bottom
const rightStartY = CONTENT_Y + 5 * (SPOT_H + SPOT_GAP)
const rightSpots = [60, 61].map((num, i) => ({
  number: num,
  x: RIGHT_X,
  y: rightStartY + i * (SPOT_H + SPOT_GAP),
}))

// Green area: from top to a few px above spot 60
const GREEN_H = rightStartY - CONTENT_Y - 6

interface SmallParkingLotProps {
  onSpotClick?: (spot: ParkingSpace) => void
  refreshKey?: number
  spaces?: ParkingSpace[]
}

export function SmallParkingLot({ onSpotClick, refreshKey, spaces: externalSpaces }: SmallParkingLotProps) {
  const [internalSpaces, setInternalSpaces] = useState<ParkingSpace[]>([])

  const fetchSpaces = useCallback(async () => {
    try {
      const response = await parkingApi.getParkingSpaces()
      if (response.ok) {
        setInternalSpaces(await response.json())
      }
    } catch {
      // silently ignore - spots will show as INACTIVE
    }
  }, [])

  useEffect(() => {
    if (!externalSpaces) {
      fetchSpaces()
    }
  }, [fetchSpaces, refreshKey, externalSpaces])

  const spaces = externalSpaces ?? internalSpaces

  const getStatus = (spotNumber: number): SpotStatus => {
    const space = spaces.find((s) => s.spotNumber === spotNumber)
    return (space?.status as SpotStatus) ?? "INACTIVE"
  }

  const getSpace = (spotNumber: number): ParkingSpace | undefined => {
    return spaces.find((s) => s.spotNumber === spotNumber)
  }

  const handleSpotClick = (spotNumber: number) => {
    if (!onSpotClick) return
    const space = getSpace(spotNumber)
    if (space) onSpotClick(space)
  }

  return (
    <svg
      viewBox={`0 0 ${SVG_W} ${SVG_H}`}
      className="mx-auto h-full w-full max-w-md"
      preserveAspectRatio="xMidYMid meet"
    >
      {/* Background */}
      <rect width={SVG_W} height={SVG_H} rx={10} fill="#e5e5e5" />

      {/* Gate / entrance marker - close to lane */}
      <rect
        x={LANE_X - 2}
        y={CONTENT_Y - 16}
        width={LANE_W + 4}
        height={14}
        rx={3}
        fill="#a3a3a3"
      />
      <text
        x={LANE_X + LANE_W / 2}
        y={CONTENT_Y - 9}
        textAnchor="middle"
        dominantBaseline="central"
        fontSize={8}
        fontFamily="system-ui, -apple-system, sans-serif"
        fill="#525252"
        fontWeight={600}
      >
        Tor / Eingang
      </text>

      {/* Driving lane - full height of content area */}
      <rect
        x={LANE_X}
        y={CONTENT_Y}
        width={LANE_W}
        height={TOTAL_H}
        rx={2}
        fill="#d4d4d4"
      />
      {/* Lane center dashes */}
      {Array.from({ length: 9 }, (_, i) => (
        <rect
          key={i}
          x={LANE_X + LANE_W / 2 - 2}
          y={CONTENT_Y + 8 + i * (TOTAL_H / 9)}
          width={4}
          height={TOTAL_H / 22}
          rx={2}
          fill="#a3a3a3"
        />
      ))}

      {/* Green area - flush right of lane, top section */}
      <rect
        x={RIGHT_X}
        y={CONTENT_Y}
        width={SPOT_W}
        height={GREEN_H}
        rx={4}
        fill="#bbf7d0"
      />
      <text
        x={RIGHT_X + SPOT_W / 2}
        y={CONTENT_Y + GREEN_H / 2}
        textAnchor="middle"
        dominantBaseline="central"
        fontSize={11}
        fontFamily="system-ui, -apple-system, sans-serif"
        fill="#16a34a"
        fontWeight={500}
      >
        Gr&uuml;nfl&auml;che
      </text>

      {/* Left parking spots (53-59, angled) */}
      {leftSpots.map((spot, i) => (
        <ParkingSpot
          key={spot.number}
          number={spot.number}
          x={spot.x}
          y={spot.y}
          width={SPOT_W}
          height={SPOT_H}
          rotation={ANGLE}
          delay={0.05 * i}
          status={getStatus(spot.number)}
          onClick={() => handleSpotClick(spot.number)}
        />
      ))}

      {/* Right parking spots (60-61, angled opposite) */}
      {rightSpots.map((spot, i) => (
        <ParkingSpot
          key={spot.number}
          number={spot.number}
          x={spot.x}
          y={spot.y}
          width={SPOT_W}
          height={SPOT_H}
          rotation={-ANGLE}
          delay={0.05 * (leftSpots.length + i)}
          status={getStatus(spot.number)}
          onClick={() => handleSpotClick(spot.number)}
        />
      ))}
    </svg>
  )
}
