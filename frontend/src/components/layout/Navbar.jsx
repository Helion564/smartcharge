import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Zap, MapPin, Calendar, Clock, CreditCard, User, LogOut, BarChart2, Shield } from 'lucide-react'
import useAuthStore from '../../store/authStore'

export default function Navbar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  const navLinks = [
    { label: 'Map', path: '/map', icon: MapPin },
    { label: 'Bookings', path: '/bookings', icon: Calendar },
    { label: 'Sessions', path: '/sessions', icon: Clock },
    { label: 'Wallet', path: '/wallet', icon: CreditCard },
  ]

  if (user?.role === 'OPERATOR') {
    navLinks.push({ label: 'Station Hub', path: '/dashboard/operator', icon: Zap })
  }

  if (user?.role === 'ADMIN') {
    navLinks.push({ label: 'Admin Hub', path: '/dashboard/admin', icon: Shield })
    navLinks.push({ label: 'Energy AI', path: '/energy', icon: BarChart2 })
  }

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo */}
          <Link to="/map" className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center text-white shadow-md shadow-emerald-500/20">
              <Zap className="w-6 h-6 fill-white" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-slate-900">
                Smart<span className="text-emerald-600">Charge</span>
              </span>
              <span className="block text-[10px] uppercase font-semibold text-emerald-600 tracking-wider">
                India EV Network
              </span>
            </div>
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-1">
            {navLinks.map((link) => {
              const Icon = link.icon
              const active = isActive(link.path)
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                    active
                      ? 'bg-emerald-50 text-emerald-700 font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${active ? 'text-emerald-600' : 'text-slate-400'}`} />
                  {link.label}
                </Link>
              )
            })}
          </nav>

          {/* User Controls */}
          <div className="flex items-center gap-3">
            <Link
              to="/profile"
              className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-slate-50 transition"
              title="Profile"
            >
              <div className="w-8 h-8 rounded-full bg-emerald-100 text-emerald-800 font-bold flex items-center justify-center text-sm">
                {user?.first_name ? user.first_name[0].toUpperCase() : 'U'}
              </div>
              <span className="hidden sm:inline text-sm font-medium text-slate-700">
                {user?.first_name || 'My Account'}
              </span>
            </Link>

            <button
              onClick={handleLogout}
              className="p-2 text-slate-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition"
              title="Logout"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}

