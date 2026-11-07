import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

const useAuthStore = create(
  persist(
    (set, get) => ({
      user:            null,
      accessToken:     null,
      isAuthenticated: false,

      login: (user, token) =>
        set({ user, accessToken: token, isAuthenticated: true }),

      logout: () =>
        set({ user: null, accessToken: null, isAuthenticated: false }),

      updateUser: (updates) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...updates } : null,
        })),

      hasRole: (role) => {
        const { user } = get()
        if (!user) return false
        const roles = user.roles ?? []
        return roles.includes(role)
      },
    }),
    {
      name:    'smartcharge-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user:            state.user,
        accessToken:     state.accessToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)

export default useAuthStore
