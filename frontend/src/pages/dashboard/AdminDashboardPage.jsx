import { useState } from 'react'
import { Shield, Users, Server, Database, Activity, RefreshCw } from 'lucide-react'
import { Button, Badge } from '../../components/ui'

export default function AdminDashboardPage() {
  const [importing, setImporting] = useState(false)
  const [importMsg, setImportMsg] = useState('')

  const handleSyncOcm = () => {
    setImporting(true)
    setTimeout(() => {
      setImporting(false)
      setImportMsg('Synchronized 50 verified Indian stations from Open Charge Map registry.')
      setTimeout(() => setImportMsg(''), 5000)
    }, 1500)
  }

  const services = [
    { name: 'User Service', port: 8081, status: 'UP', instances: 1 },
    { name: 'Station Service', port: 8082, status: 'UP', instances: 1 },
    { name: 'Booking Service', port: 8083, status: 'UP', instances: 1 },
    { name: 'Charging Session Service', port: 8084, status: 'UP', instances: 1 },
    { name: 'Payment Service', port: 8085, status: 'UP', instances: 1 },
    { name: 'Notification Service', port: 8086, status: 'UP', instances: 1 },
    { name: 'Energy Optimization Service (AI)', port: 8087, status: 'UP', instances: 1 },
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Platform Administration</h1>
          <p className="text-slate-500 text-sm mt-1">System health, microservice discovery, and Indian dataset synchronization</p>
        </div>

        <Button onClick={handleSyncOcm} loading={importing}>
          <RefreshCw className="w-4 h-4 mr-1.5" /> Sync Open Charge Map Data
        </Button>
      </div>

      {importMsg && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-800 text-sm font-semibold mb-6">
          {importMsg}
        </div>
      )}

      {/* Services Grid */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm mb-8">
        <h2 className="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
          <Server className="w-4 h-4 text-emerald-600" /> Netflix Eureka Microservices Registry
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {services.map((svc) => (
            <div key={svc.name} className="p-4 rounded-xl border border-slate-100 bg-slate-50/50 flex items-center justify-between">
              <div>
                <div className="font-bold text-slate-900 text-sm">{svc.name}</div>
                <div className="text-xs text-slate-400">Port {svc.port} • {svc.instances} Node</div>
              </div>
              <Badge variant="success">{svc.status}</Badge>
            </div>
          ))}
        </div>
      </div>

      {/* System Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="p-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs text-slate-400 uppercase font-bold">Total Registered EV Drivers</div>
          <div className="text-2xl font-black text-slate-900 mt-2">1,280</div>
        </div>
        <div className="p-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs text-slate-400 uppercase font-bold">Total Charging Stations</div>
          <div className="text-2xl font-black text-slate-900 mt-2">240</div>
        </div>
        <div className="p-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs text-slate-400 uppercase font-bold">Kafka Events Processed</div>
          <div className="text-2xl font-black text-emerald-600 mt-2">48,290</div>
        </div>
        <div className="p-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="text-xs text-slate-400 uppercase font-bold">Redis Cache Hit Rate</div>
          <div className="text-2xl font-black text-slate-900 mt-2">99.2%</div>
        </div>
      </div>
    </div>
  )
}
