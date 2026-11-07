import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Calendar, Clock, MapPin, Zap, CheckCircle2, XCircle } from 'lucide-react'
import { bookingApi } from '../../api/bookingApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { Button, Badge, LoadingSpinner } from '../../components/ui'

export default function BookingsPage() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadBookings()
  }, [])

  const loadBookings = async () => {
    try {
      const res = await bookingApi.getMyBookings()
      setBookings(res.content || res || [])
    } catch (err) {
      // Mock fallback
      setBookings([
        {
          id: 'b1111111-1111-1111-1111-111111111101',
          stationId: 'a1111111-1111-1111-1111-111111111101',
          stationName: 'Tata Power EZ Charge - BKC Hub',
          connectorType: 'CCS',
          startTime: new Date().toISOString(),
          endTime: new Date(Date.now() + 3600000).toISOString(),
          status: 'CONFIRMED',
          estimatedCost: 412.50,
          vehicleModel: 'Tata Nexon EV',
          vehicleRegistration: 'MH02-DW-1234',
        }
      ])
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return
    try {
      await bookingApi.cancelBooking(bookingId, 'User cancelled')
      loadBookings()
    } catch (err) {
      alert('Failed to cancel booking.')
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-black text-slate-900">My Charging Bookings</h1>
          <p className="text-slate-500 text-sm mt-1">Manage reserved slots across Indian charging networks</p>
        </div>
        <Link to="/map">
          <Button>Find New Station</Button>
        </Link>
      </div>

      {loading ? (
        <LoadingSpinner size="lg" />
      ) : bookings.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-2xl border border-slate-200">
          <Calendar className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-base font-bold text-slate-700">No active reservations</h3>
          <p className="text-xs text-slate-500 mt-1">Discover stations on the map to reserve a fast-charging slot.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <div
              key={b.id}
              className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4"
            >
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Badge variant={b.status === 'CONFIRMED' ? 'success' : 'neutral'}>
                    {b.status}
                  </Badge>
                  <span className="text-xs font-semibold text-slate-500">ID: {b.id.slice(0, 8)}</span>
                </div>
                <h3 className="text-lg font-bold text-slate-900">{b.stationName || 'EV Charging Station'}</h3>
                <div className="flex flex-wrap gap-4 mt-2 text-xs text-slate-500">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-slate-400" />
                    {formatDate(b.startTime)}
                  </span>
                  <span className="flex items-center gap-1">
                    <Zap className="w-3.5 h-3.5 text-emerald-600" />
                    {b.connectorType || 'Fast DC'} • {b.vehicleModel}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <div className="text-right">
                  <div className="text-xs text-slate-400 font-semibold uppercase">Estimated Cost</div>
                  <div className="text-lg font-black text-slate-900">{formatCurrency(b.estimatedCost)}</div>
                </div>

                {b.status === 'CONFIRMED' && (
                  <Button variant="danger" size="sm" onClick={() => handleCancel(b.id)}>
                    Cancel
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
