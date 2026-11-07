import { useState, useEffect } from 'react'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  BarChart,
  Bar,
} from 'recharts'
import { Zap, TrendingUp, Leaf, Fuel, Cpu, AlertTriangle, ShieldCheck } from 'lucide-react'
import { energyApi } from '../../api/energyApi'
import { Card, Badge, Button, LoadingSpinner } from '../../components/ui'

export default function EnergyDashboardPage() {
  const [demandCurve, setDemandCurve] = useState([])
  const [analytics, setAnalytics] = useState(null)
  const [carbonReport, setCarbonReport] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [curve, savings] = await Promise.all([
        energyApi.getDemandCurve('a1111111-1111-1111-1111-111111111101'),
        energyApi.getCarbonSavings(),
      ])
      setDemandCurve(curve)
      setCarbonReport(savings)
    } catch (err) {
      // Fallback mock
      setDemandCurve([
        { hour: '00:00', predicted_kw: 12, actual_kw: 11 },
        { hour: '02:00', predicted_kw: 8, actual_kw: 7 },
        { hour: '04:00', predicted_kw: 9, actual_kw: 9 },
        { hour: '06:00', predicted_kw: 35, actual_kw: 32 },
        { hour: '08:00', predicted_kw: 94, actual_kw: 91 },
        { hour: '10:00', predicted_kw: 86, actual_kw: 89 },
        { hour: '12:00', predicted_kw: 48, actual_kw: 50 },
        { hour: '14:00', predicted_kw: 52, actual_kw: 55 },
        { hour: '16:00', predicted_kw: 68, actual_kw: 64 },
        { hour: '18:00', predicted_kw: 105, actual_kw: 102 },
        { hour: '20:00', predicted_kw: 90, actual_kw: 93 },
        { hour: '22:00', predicted_kw: 34, actual_kw: 36 },
      ])
      setCarbonReport({
        co2_saved_kg: 61632.0,
        trees_equivalent: 2831.0,
        petrol_saved_litres: 23968.0,
      })
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <LoadingSpinner size="lg" />

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs px-2.5 py-0.5 rounded-full font-bold bg-emerald-100 text-emerald-800 flex items-center gap-1">
              <Cpu className="w-3.5 h-3.5 text-emerald-700" /> Random Forest Regression AI
            </span>
            <Badge variant="success">Optimization Active</Badge>
          </div>
          <h1 className="text-2xl font-black text-slate-900">AI Energy Optimization Engine</h1>
          <p className="text-slate-500 text-sm mt-1">
            Predictive load forecasting, grid peak shaving, and dynamic multi-vehicle energy balancing
          </p>
        </div>

        <Button onClick={loadData}>
          Recalculate Grid Model
        </Button>
      </div>

      {/* Carbon & Environmental Offset Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="p-6 bg-gradient-to-tr from-emerald-600 to-teal-500 rounded-2xl text-white shadow-lg shadow-emerald-600/20">
          <div className="flex items-center justify-between opacity-80 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">CO2 Emissions Avoided</span>
            <Leaf className="w-5 h-5" />
          </div>
          <div className="text-3xl font-black">{carbonReport?.co2_saved_kg?.toLocaleString() || '61,632'} kg</div>
          <p className="text-xs text-emerald-100 mt-2">Versus internal combustion vehicles in India</p>
        </div>

        <div className="p-6 bg-gradient-to-tr from-teal-600 to-cyan-500 rounded-2xl text-white shadow-lg shadow-teal-600/20">
          <div className="flex items-center justify-between opacity-80 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Equivalent Trees Planted</span>
            <Leaf className="w-5 h-5" />
          </div>
          <div className="text-3xl font-black">{carbonReport?.trees_equivalent?.toLocaleString() || '2,831'}</div>
          <p className="text-xs text-teal-100 mt-2">Annual carbon absorption equivalence</p>
        </div>

        <div className="p-6 bg-gradient-to-tr from-blue-600 to-indigo-500 rounded-2xl text-white shadow-lg shadow-blue-600/20">
          <div className="flex items-center justify-between opacity-80 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Petrol / Diesel Replaced</span>
            <Fuel className="w-5 h-5" />
          </div>
          <div className="text-3xl font-black">{carbonReport?.petrol_saved_litres?.toLocaleString() || '23,968'} L</div>
          <p className="text-xs text-blue-100 mt-2">Clean energy consumed across hubs</p>
        </div>
      </div>

      {/* Demand Curve Chart */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm mb-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-base font-bold text-slate-900">24-Hour AI Demand Prediction vs Actual Load</h2>
            <p className="text-xs text-slate-400 mt-0.5">Tata Power EZ Charge - BKC Hub (Mumbai)</p>
          </div>
          <div className="flex items-center gap-4 text-xs font-semibold">
            <span className="flex items-center gap-1.5 text-emerald-600">
              <span className="w-3 h-3 rounded-full bg-emerald-500 inline-block" /> Predicted Demand (kW)
            </span>
            <span className="flex items-center gap-1.5 text-blue-600">
              <span className="w-3 h-3 rounded-full bg-blue-500 inline-block" /> Actual Grid Draw (kW)
            </span>
          </div>
        </div>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={demandCurve} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="predGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#059669" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#059669" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="actGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#2563eb" stopOpacity={0.2} />
                  <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="hour" stroke="#94a3b8" fontSize={12} />
              <YAxis stroke="#94a3b8" fontSize={12} unit=" kW" />
              <Tooltip />
              <Area type="monotone" dataKey="predicted_kw" stroke="#059669" strokeWidth={2.5} fillOpacity={1} fill="url(#predGrad)" />
              <Area type="monotone" dataKey="actual_kw" stroke="#2563eb" strokeWidth={2} fillOpacity={1} fill="url(#actGrad)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Grid Peak Hours & Dynamic Strategy */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
          <h3 className="text-base font-bold text-slate-900 mb-3 flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" /> Identified Indian Peak Tariff Windows
          </h3>
          <p className="text-xs text-slate-500 mb-4">
            The AI engine automatically throttles or reschedules flexible charging during peak hours to protect local substation transformers.
          </p>
          <div className="space-y-3">
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl flex items-center justify-between text-xs">
              <span className="font-bold text-amber-900">Morning Office Peak: 08:00 AM – 10:30 AM</span>
              <span className="font-semibold text-amber-700">High Demand (85 kW+)</span>
            </div>
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl flex items-center justify-between text-xs">
              <span className="font-bold text-amber-900">Evening Commute Peak: 06:00 PM – 09:30 PM</span>
              <span className="font-semibold text-amber-700">Peak Load (105 kW+)</span>
            </div>
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl flex items-center justify-between text-xs">
              <span className="font-bold text-emerald-900">Night Off-Peak Window: 11:00 PM – 06:00 AM</span>
              <span className="font-semibold text-emerald-700">Optimal Tariffs (₹10/kWh)</span>
            </div>
          </div>
        </div>

        <div className="p-6 bg-white rounded-2xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900 mb-3 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-600" /> Real-time Power Balancing Strategy
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Select how available substation kW is dynamically proportioned among simultaneous vehicles.
            </p>
            <div className="space-y-2 text-xs">
              <label className="p-3 rounded-xl border border-emerald-200 bg-emerald-50/50 flex items-center gap-3 cursor-pointer">
                <input type="radio" name="strategy" defaultChecked className="text-emerald-600" />
                <div>
                  <div className="font-bold text-slate-900">Balanced Fair-Share (Default)</div>
                  <div className="text-slate-500">Every plugged-in vehicle receives equal proportional kW up to rated maximum.</div>
                </div>
              </label>
              <label className="p-3 rounded-xl border border-slate-200 hover:bg-slate-50 flex items-center gap-3 cursor-pointer">
                <input type="radio" name="strategy" className="text-emerald-600" />
                <div>
                  <div className="font-bold text-slate-900">Fastest Turnaround</div>
                  <div className="text-slate-500">Prioritizes EVs nearest to 80% SoC to free up parking stalls quickly.</div>
                </div>
              </label>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-100 text-xs text-slate-400">
            Current station efficiency score: <strong className="text-emerald-600">94.2%</strong>
          </div>
        </div>
      </div>
    </div>
  )
}
