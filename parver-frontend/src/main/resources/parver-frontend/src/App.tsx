import { Routes, Route } from "react-router-dom"
import ParkingPage from "@/pages/ParkingPage"
import ErrorPage from "@/pages/ErrorPage"

function App() {
  return (
    <Routes>
      <Route path="/" element={<ParkingPage />} />
      <Route path="*" element={<ErrorPage />} />
    </Routes>
  )
}

export default App
