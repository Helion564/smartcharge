import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/layout/ProtectedRoute.jsx'

// Auth
import LoginPage    from './pages/auth/LoginPage.jsx'
import RegisterPage from './pages/auth/RegisterPage.jsx'

// Main pages
import MapPage           from './pages/map/MapPage.jsx'
import StationDetailPage from './pages/stations/StationDetailPage.jsx'
import BookingsPage      from './pages/bookings/BookingsPage.jsx'
import BookingDetailPage from './pages/bookings/BookingDetailPage.jsx'
import SessionsPage      from './pages/sessions/SessionsPage.jsx'
import WalletPage        from './pages/wallet/WalletPage.jsx'
import ProfilePage       from './pages/profile/ProfilePage.jsx'

// Dashboards
import OperatorDashboardPage from './pages/dashboard/OperatorDashboardPage.jsx'
import AdminDashboardPage    from './pages/dashboard/AdminDashboardPage.jsx'
import EnergyDashboardPage   from './pages/energy/EnergyDashboardPage.jsx'

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Root redirect */}
      <Route path="/" element={<Navigate to="/map" replace />} />

      {/* Protected – any authenticated user */}
      <Route element={<ProtectedRoute />}>
        <Route path="/map"                       element={<MapPage />} />
        <Route path="/stations/:stationId"       element={<StationDetailPage />} />
        <Route path="/bookings"                  element={<BookingsPage />} />
        <Route path="/bookings/:bookingId"       element={<BookingDetailPage />} />
        <Route path="/sessions"                  element={<SessionsPage />} />
        <Route path="/wallet"                    element={<WalletPage />} />
        <Route path="/profile"                   element={<ProfilePage />} />
      </Route>

      {/* Protected – OPERATOR role */}
      <Route element={<ProtectedRoute requiredRole="OPERATOR" />}>
        <Route path="/dashboard/operator" element={<OperatorDashboardPage />} />
      </Route>

      {/* Protected – ADMIN role */}
      <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
        <Route path="/dashboard/admin" element={<AdminDashboardPage />} />
        <Route path="/energy"          element={<EnergyDashboardPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/map" replace />} />
    </Routes>
  )
}
