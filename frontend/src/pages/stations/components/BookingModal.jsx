import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Calendar, Clock, Zap, IndianRupee, AlertCircle } from 'lucide-react'
import { bookingApi } from '../../../api/bookingApi'
import { formatCurrency } from '../../../utils/formatters'
import { Button, Input, Modal } from '../../../components/ui'

export default function BookingModal({ isOpen, onClose, station, connector }) {
  const navigate = useNavigate()
  const [startTime, setStartTime] = useState(() => {
    const d = new Date()
    d.setMinutes(d.getMinutes() + 15)
    return d.toISOString().slice(0, 16)
  })
  const [durationHours, setDurationHours] = useState(1.0)
  const [estimatedEnergyKwh, setEstimatedEnergyKwh] = useState(25.0)
  const [vehicleModel, setVehicleModel] = useState('Tata Nexon EV')
  const [vehicleRegistration, setVehicleRegistration] = useState('MH02-DW-1234')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!connector) return null

  const pricePerKwh = connector.pricePerKwh || 16.5
  const estimatedCost = estimatedEnergyKwh * pricePerKwh

  const handleBooking = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    const start = new Date(startTime)
    const end = new Date(start.getTime() + durationHours * 3600 * 1000)

    try {
      await bookingApi.createBooking({
        stationId: station.id,
        connectorId: connector.id,
        startTime: start.toISOString().slice(0, 19),
        endTime: end.toISOString().slice(0, 19),
        estimatedEnergyKwh: Number(estimatedEnergyKwh),
        vehicleModel,
        vehicleRegistration,
      })
      onClose()
      navigate('/bookings')
    } catch (err) {
      setError(err.response?.data?.message || 'Slot reservation failed. Please choose another time.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Reserve Charging Slot">
      <form onSubmit={handleBooking} className="space-y-4">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-sm text-red-700">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500">Connector</div>
            <div className="text-sm font-bold text-slate-800">
              {connector.connectorCode} ({connector.connectorType})
            </div>
          </div>
          <div className="text-right">
            <div className="text-xs text-slate-500">Rate</div>
            <div className="text-sm font-bold text-emerald-600">
              {formatCurrency(pricePerKwh)}/kWh
            </div>
          </div>
        </div>

        <Input
          label="Start Time"
          type="datetime-local"
          required
          value={startTime}
          onChange={(e) => setStartTime(e.target.value)}
        />

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1">
              Duration (Hours)
            </label>
            <select
              className="w-full px-3.5 py-2 bg-white border border-slate-300 rounded-lg text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              value={durationHours}
              onChange={(e) => setDurationHours(Number(e.target.value))}
            >
              <option value={0.5}>30 minutes</option>
              <option value={1.0}>1 hour</option>
              <option value={1.5}>1.5 hours</option>
              <option value={2.0}>2 hours</option>
              <option value={3.0}>3 hours</option>
            </select>
          </div>

          <Input
            label="Estimated kWh"
            type="number"
            min="1"
            max="150"
            required
            value={estimatedEnergyKwh}
            onChange={(e) => setEstimatedEnergyKwh(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="EV Model"
            required
            value={vehicleModel}
            onChange={(e) => setVehicleModel(e.target.value)}
          />
          <Input
            label="Registration No."
            required
            value={vehicleRegistration}
            onChange={(e) => setVehicleRegistration(e.target.value)}
          />
        </div>

        <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500">Estimated Total</div>
            <div className="text-lg font-bold text-slate-900">{formatCurrency(estimatedCost)}</div>
          </div>
          <Button type="submit" loading={loading} className="px-6">
            Confirm Booking
          </Button>
        </div>
      </form>
    </Modal>
  )
}

