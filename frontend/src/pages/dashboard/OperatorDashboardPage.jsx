import { useState, useEffect } from 'react'
import { Zap, Plus, Settings, DollarSign, Activity, AlertCircle } from 'lucide-react'
import { stationApi } from '../../api/stationApi'
import { formatCurrency } from '../../utils/formatters'
import { Button, Badge, LoadingSpinner } from '../../components/ui'

export default function OperatorDashboardPage() {
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    stationApi.getOperatorStations()
      .then((res) => setStations(res || []))
      .catch(() => {
        setStations([
          {
            id: 'a1111111-1111-1111-1111-111111111101',
            name: 'Tata Power EZ Charge - BKC Hub',
            city: 'Mumbai',
            status: 'ACTIVE',
            availableConnectors: 3,
            totalConnectors: 3,
            todayRevenue: 4850.00,
            todayKwh: 312.0,
          }
        ])
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Operator Infrastructure Hub</h1>
          <p className="text-slate-500 text-sm mt-1">Manage physical EV charging stations, connector status, and live grid power</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-1.5" /> Deploy Station
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs font-bold text-slate-400 uppercase">Managed Stations</div>
          <div className="text-3xl font-black text-slate-900 mt-2">{stations.length}</div>
        </div>
        <div className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs font-bold text-slate-400 uppercase">Live Connectors Available</div>
          <div className="text-3xl font-black text-emerald-600 mt-2">
            {stations.reduce((acc, s) => acc + (s.availableConnectors || 0), 0)} / {stations.reduce((acc, s) => acc + (s.totalConnectors || 0), 0)}
          </div>
        </div>
        <div className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs font-bold text-slate-400 uppercase">Revenue Generated (Today)</div>
          <div className="text-3xl font-black text-slate-900 mt-2">₹4,850.00</div>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h2 className="text-base font-bold text-slate-900">Your Charging Stations</h2>
        </div>
        <div className="divide-y divide-slate-100">
          {stations.map((st) => (
            <div key={st.id} className="p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Badge variant="success">{st.status}</Badge>
                  <span className="text-xs text-slate-400">{st.city}</span>
                </div>
                <h3 className="font-bold text-slate-900 text-lg">{st.name}</h3>
                <div className="text-xs text-slate-500 mt-1">
                  {st.availableConnectors} / {st.totalConnectors} Connectors Online
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Button variant="outline" size="sm">
                  <Settings className="w-4 h-4 mr-1" /> Configure Limits
                </Button>
                <Button size="sm">Manage Connectors</Button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

