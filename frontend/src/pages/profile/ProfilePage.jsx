import { useState } from 'react'
import { User, Mail, Phone, Shield, Lock, Save } from 'lucide-react'
import useAuthStore from '../../store/authStore'
import { Button, Input, Card } from '../../components/ui'

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore()
  const [firstName, setFirstName] = useState(user?.first_name || '')
  const [lastName, setLastName] = useState(user?.last_name || '')
  const [saved, setSaved] = useState(false)

  const handleSave = (e) => {
    e.preventDefault()
    updateUser({ first_name: firstName, last_name: lastName })
    setSaved(true)
    setTimeout(() => setSaved(false), 3000)
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-black text-slate-900">User Profile</h1>
        <p className="text-slate-500 text-sm mt-1">Manage your account details and security settings</p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200/80 p-8 shadow-sm">
        <div className="flex items-center gap-4 border-b border-slate-100 pb-6 mb-6">
          <div className="w-16 h-16 rounded-2xl bg-emerald-100 text-emerald-800 font-black text-2xl flex items-center justify-center">
            {firstName ? firstName[0].toUpperCase() : 'U'}
          </div>
          <div>
            <h2 className="text-lg font-bold text-slate-900">{firstName} {lastName}</h2>
            <div className="flex items-center gap-2 mt-1">
              <span className="text-xs px-2.5 py-0.5 rounded-full font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                {user?.role || 'EV_OWNER'}
              </span>
              <span className="text-xs text-slate-400">{user?.email}</span>
            </div>
          </div>
        </div>

        <form onSubmit={handleSave} className="space-y-4">
          {saved && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-sm text-emerald-700 font-semibold">
              Profile updated successfully!
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="First Name"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
            />
            <Input
              label="Last Name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </div>

          <Input
            label="Email Address"
            disabled
            value={user?.email || 'user@smartcharge.in'}
          />

          <Button type="submit" className="mt-4">
            Save Profile
          </Button>
        </form>
      </div>
    </div>
  )
}

