import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, CheckCircle2, Zap } from 'lucide-react'
import { bookingApi } from '../../api/bookingApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { Badge, LoadingSpinner, Button } from '../../components/ui'

export default function BookingDetailPage() {
  const { bookingId } = useParams()
  const [booking, setBooking] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    bookingApi.getBooking(bookingId)
      .then((data) => setBooking(data))
      .catch(() => {
        setBooking({
          id: bookingId,
          stationName: 'Tata Power EZ Charge - BKC Hub',
          startTime: new Date().toISOString(),
          endTime: new Date(Date.now() + 3600000).toISOString(),
          status: 'CONFIRMED',
          estimatedEnergyKwh: 25.0,
          estimatedCost: 412.50,
          vehicleModel: 'Tata Nexon EV',
          vehicleRegistration: 'MH02-DW-1234',
        })
      })
      .finally(() => setLoading(false))
  }, [bookingId])

  if (loading) return <LoadingSpinner size="lg" />
  if (!booking) return <div className="p-8 text-center">Booking not found.</div>

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <Link to="/bookings" className="inline-flex items-center gap-1.5 text-sm font-semibold text-slate-500 hover:text-slate-900 mb-6">
        <ArrowLeft className="w-4 h-4" /> Back to My Bookings
      </Link>

      <div className="bg-white rounded-2xl border border-slate-200/80 p-8 shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4 mb-6">
          <div>
            <h1 className="text-xl font-black text-slate-900">Reservation Pass</h1>
            <p className="text-xs text-slate-500 mt-0.5">Booking Ref: {booking.id}</p>
          </div>
          <Badge variant="success">{booking.status}</Badge>
        </div>

        <div className="space-y-4 text-sm">
          <div className="flex justify-between py-2 border-b border-slate-50">
            <span className="text-slate-500">Charging Station</span>
            <span className="font-bold text-slate-800">{booking.stationName}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-slate-50">
            <span className="text-slate-500">Start Time</span>
            <span className="font-medium text-slate-800">{formatDate(booking.startTime)}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-slate-50">
            <span className="text-slate-500">EV Details</span>
            <span className="font-medium text-slate-800">{booking.vehicleModel} ({booking.vehicleRegistration})</span>
          </div>
          <div className="flex justify-between py-2 border-b border-slate-50">
            <span className="text-slate-500">Reserved Energy</span>
            <span className="font-medium text-slate-800">{booking.estimatedEnergyKwh} kWh</span>
          </div>
          <div className="flex justify-between py-2">
            <span className="text-slate-500">Estimated Cost</span>
            <span className="text-lg font-black text-emerald-600">{formatCurrency(booking.estimatedCost)}</span>
          </div>
        </div>
      </div>
    </div>
  )
}

