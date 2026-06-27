import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Package, ShoppingCart,
  Truck, FileText, Bell, LogOut
} from 'lucide-react';
import useAuthStore from '../store/authStore';

export default function Sidebar() {
  const { user, logout } = useAuthStore();

  const links = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/inventory', icon: Package, label: 'Inventory' },
    { to: '/billing', icon: ShoppingCart, label: 'Billing' },
    { to: '/suppliers', icon: Truck, label: 'Suppliers' },
    { to: '/reports', icon: FileText, label: 'Reports' },
    { to: '/alerts', icon: Bell, label: 'Alerts' },
  ];

  return (
    <div className="w-64 bg-white border-r border-gray-200 h-screen flex flex-col">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-xl font-bold text-blue-600">MedStock Pro</h1>
        <p className="text-xs text-gray-500 mt-1">Pharmacy Management</p>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition ${
                isActive
                  ? 'bg-blue-50 text-blue-600'
                  : 'text-gray-600 hover:bg-gray-50'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center gap-3 px-2 mb-3">
          <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-semibold">
            {user?.name?.charAt(0) || 'U'}
          </div>
          <div className="overflow-hidden">
            <p className="text-sm font-medium text-gray-800 truncate">{user?.name}</p>
            <p className="text-xs text-gray-500">{user?.role?.replace('ROLE_', '')}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition"
        >
          <LogOut size={16} />
          Logout
        </button>
      </div>
    </div>
  );
}