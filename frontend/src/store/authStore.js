import { create } from 'zustand';

const useAuthStore = create((set) => ({
  user: JSON.parse(localStorage.getItem('user')) || null,
  isAuthenticated: !!localStorage.getItem('accessToken'),

  login: (loginResponse) => {
    localStorage.setItem('accessToken', loginResponse.accessToken);
    localStorage.setItem('refreshToken', loginResponse.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      email: loginResponse.email,
      name: loginResponse.name,
      role: loginResponse.role,
    }));
    set({
      user: { email: loginResponse.email, name: loginResponse.name, role: loginResponse.role },
      isAuthenticated: true,
    });
  },

  logout: () => {
    localStorage.clear();
    set({ user: null, isAuthenticated: false });
  },
}));

export default useAuthStore;