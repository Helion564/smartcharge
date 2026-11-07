import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { MapPin, Zap, Clock, ShieldCheck, Phone, Mail, Globe, ArrowLeft, CheckCircle2 } from 'lucide-react'
import { stationApi } from '../../api/stationApi'
import { formatCurrency } from '../../utils/formatters'
import { Button, Badge, LoadingSpinner } from '../../components/ui'
import BookingModal from './components/BookingModal'

export default function StationDetailPage() {
  const { stationId } = useParams()
  const [station, setStation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [selectedConnector, setSelectedConnector] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)

  useEffect(() => {
    loadStation()
  }, [stationId])

  const loadStation = async () => {
    try {
      const data = await stationApi.getStation(stationId)
      setStation(data)
    } catch (err) {
      // Fallback mock
      setStation({
        id: stationId,
        name: 'Tata Power EZ Charge - BKC Hub',
        operatorName: 'Tata Power',
        addressLine1: 'G Block, Bandra Kurla Complex',
        city: 'Mumbai',
        state: 'Maharashtra',
        pincode: '400051',
        is24Hours: true,
        contactPhone: '+91-22-66658282',
        contactEmail: 'customercare@tatapower.com',
        dataSource: 'BEE Indian National Registry',
        connectors: [
          {
            id: 'c1111111-1111-1111-1111-111111111101',
            connectorCode: 'TP-BKC-01',
            connectorType: 'CCS',
            powerLevel: 'DC_FAST',
            powerKw: 60.0,
            status: 'AVAILABLE',
            pricePerKwh: 16.50,
          },
          {
            id: 'c1111111-1111-1111-1111-111111111102',
            connectorCode: 'TP-BKC-02',
            connectorType: 'CHADEMO',
            powerLevel: 'DC_FAST',
            powerKw: 50.0,
            status: 'AVAILABLE',
            pricePerKwh: 16.50,
          },
          {
            id: 'c1111111-1111-1111-1111-111111111103',
            connectorCode: 'TP-BKC-03',
            connectorType: 'TYPE2',
            powerLevel: 'LEVEL_2',
            powerKw: 22.0,
            status: 'AVAILABLE',
            pricePerKwh: 12.00,
          }
        ]
      })
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <LoadingSpinner size="lg" />
  if (!station) return <div className="p-8 text-center">Station not found.</div>

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <Link to="/map" className="inline-flex items-center gap-1.5 text-sm font-semibold text-slate-500 hover:text-slate-900 mb-6">
        <ArrowLeft className="w-4 h-4" /> Back to Map
      </Link>

      <div className="bg-white rounded-2xl border border-slate-200/80 p-8 shadow-sm">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-100 pb-6">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Badge variant="success">Verified Station</Badge>
              <Badge variant="neutral">{station.dataSource || 'OCM'}</Badge>
            </div>
            <h1 className="text-2xl font-black text-slate-900">{station.name}</h1>
            <p className="text-slate-500 text-sm flex items-center gap-1 mt-1">
              <MapPin className="w-4 h-4 text-slate-400" />
              {station.addressLine1}, {station.city}, {station.state} - {station.pincode}
            </p>
          </div>

          <div className="text-right">
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Operated by</div>
            <div className="text-lg font-bold text-emerald-700 flex items-center gap-1 md:justify-end">
              <Zap className="w-5 h-5 fill-emerald-600 text-emerald-600" />
              {station.operatorName}
            </div>
          </div>
        </div>

        {/* Connectors Table */}
        <div className="mt-8">
          <h2 className="text-lg font-bold text-slate-900 mb-4">Available Charging Connectors</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {station.connectors?.map((conn) => {
              const isFree = conn.status === 'AVAILABLE'
              return (
                <div
                  key={conn.id}
                  className="p-5 rounded-xl border border-slate-200 bg-slate-50/50 flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-bold text-slate-800 text-sm">{conn.connectorCode}</span>
                      <Badge variant={isFree ? 'success' : 'danger'}>
                        {conn.status}
                      </Badge>
                    </div>

                    <div className="text-xs text-slate-500 mb-1">Standard: {conn.connectorType}</div>
                    <div className="text-xs text-slate-500 mb-3">Power: {conn.powerKw} kW ({conn.powerLevel})</div>

                    <div className="text-base font-extrabold text-slate-900">
                      {formatCurrency(conn.pricePerKwh)}
                      <span className="text-xs font-normal text-slate-500"> / kWh</span>
                    </div>
                  </div>

                  <Button
                    onClick={() => {
                      setSelectedConnector(conn)
                      setModalOpen(true)
                    }}
                    disabled={!isFree}
                    className="w-full mt-4"
                  >
                    {isFree ? 'Book This Slot' : 'Slot In Use'}
                  </Button>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      <BookingModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        station={station}
        connector={selectedConnector}
      />
    </div>
  )
}
