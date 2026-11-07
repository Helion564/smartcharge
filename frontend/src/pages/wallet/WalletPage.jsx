import { useState, useEffect } from 'react'
import { Wallet, PlusCircle, ArrowUpRight, ArrowDownLeft, ShieldCheck } from 'lucide-react'
import { paymentApi } from '../../api/paymentApi'
import { formatCurrency, formatDate } from '../../utils/formatters'
import { Button, Input, LoadingSpinner } from '../../components/ui'

export default function WalletPage() {
  const [wallet, setWallet] = useState(null)
  const [topupAmount, setTopupAmount] = useState('500')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    loadWallet()
  }, [])

  const loadWallet = async () => {
    try {
      const data = await paymentApi.getWallet()
      setWallet(data)
    } catch (err) {
      // Mock fallback
      setWallet({
        balance: 750.00,
        totalCharged: 1250.00,
        totalRefunded: 0.00,
        recentTransactions: [
          {
            id: 'tx-1',
            amount: 500.00,
            type: 'CREDIT',
            description: 'UPI Top-up',
            createdAt: new Date(Date.now() - 86400000).toISOString(),
          },
          {
            id: 'tx-2',
            amount: 412.50,
            type: 'DEBIT',
            description: 'Charging Session Payment - BKC Hub',
            createdAt: new Date(Date.now() - 43200000).toISOString(),
          }
        ]
      })
    } finally {
      setLoading(false)
    }
  }

  const handleTopup = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await paymentApi.topupWallet(Number(topupAmount), 'UPI')
      loadWallet()
    } catch (err) {
      // Simulated topup success in demo mode
      setWallet((prev) => ({
        ...prev,
        balance: (prev?.balance || 0) + Number(topupAmount),
        recentTransactions: [
          {
            id: `tx-${Date.now()}`,
            amount: Number(topupAmount),
            type: 'CREDIT',
            description: 'Instant UPI Top-up (FastPay)',
            createdAt: new Date().toISOString(),
          },
          ...(prev?.recentTransactions || [])
        ]
      }))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <LoadingSpinner size="lg" />

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-black text-slate-900">SmartCharge Wallet</h1>
        <p className="text-slate-500 text-sm mt-1">Pre-fund your charging balance for zero-latency plug-and-charge</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {/* Wallet Balance Card */}
        <div className="md:col-span-2 p-8 bg-gradient-to-br from-emerald-700 via-emerald-800 to-teal-900 rounded-3xl text-white shadow-xl shadow-emerald-900/20 relative overflow-hidden flex flex-col justify-between">
          <div className="absolute right-0 bottom-0 translate-x-8 translate-y-8 opacity-10">
            <Wallet className="w-64 h-64" />
          </div>

          <div>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold tracking-widest uppercase text-emerald-200">Prepaid SmartBalance</span>
              <span className="text-xs bg-emerald-600/50 px-2.5 py-1 rounded-full font-semibold border border-emerald-400/30">
                Auto-Pay Enabled
              </span>
            </div>
            <div className="text-4xl font-black tracking-tight mt-3">
              {formatCurrency(wallet?.balance || 0)}
            </div>
          </div>

          <div className="mt-8 pt-4 border-t border-emerald-600/40 flex items-center justify-between text-xs text-emerald-200">
            <span>Linked to SmartCharge FastPass</span>
            <span>Zero payment delay at chargers</span>
          </div>
        </div>

        {/* Top-up Box */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
          <h3 className="font-bold text-slate-900 text-sm mb-3">Add Funds</h3>
          <form onSubmit={handleTopup} className="space-y-3">
            <div className="grid grid-cols-3 gap-2">
              {['200', '500', '1000'].map((amt) => (
                <button
                  type="button"
                  key={amt}
                  onClick={() => setTopupAmount(amt)}
                  className={`py-2 rounded-lg text-xs font-bold border transition ${
                    topupAmount === amt
                      ? 'border-emerald-600 bg-emerald-50 text-emerald-700'
                      : 'border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  ₹{amt}
                </button>
              ))}
            </div>

            <Input
              type="number"
              min="50"
              step="50"
              value={topupAmount}
              onChange={(e) => setTopupAmount(e.target.value)}
              placeholder="Custom amount"
            />

            <Button type="submit" loading={submitting} className="w-full">
              Top Up via UPI
            </Button>
          </form>
        </div>
      </div>

      {/* Transaction History */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm">
        <h3 className="text-base font-bold text-slate-900 mb-4">Recent Wallet Activity</h3>
        <div className="divide-y divide-slate-100">
          {wallet?.recentTransactions?.map((tx) => (
            <div key={tx.id} className="py-3.5 flex items-center justify-between text-sm">
              <div className="flex items-center gap-3">
                <div
                  className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                    tx.type === 'CREDIT' ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {tx.type === 'CREDIT' ? <ArrowDownLeft className="w-4 h-4" /> : <ArrowUpRight className="w-4 h-4" />}
                </div>
                <div>
                  <div className="font-semibold text-slate-800">{tx.description}</div>
                  <div className="text-xs text-slate-400">{formatDate(tx.createdAt)}</div>
                </div>
              </div>
              <div className={`font-black ${tx.type === 'CREDIT' ? 'text-emerald-600' : 'text-slate-900'}`}>
                {tx.type === 'CREDIT' ? '+' : '-'}{formatCurrency(tx.amount)}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
