import React from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Bot, 
  LifeBuoy, 
  ShieldCheck, 
  User as UserIcon, 
  LogOut, 
  LayoutDashboard, 
  BookOpen, 
  Sparkles,
  ArrowRightLeft
} from 'lucide-react';

export const Navbar = ({ currentView, setCurrentView }) => {
  const { user, demoLogin, logout } = useAuth();

  const isAgentOrAdmin = user?.role === 'ROLE_AGENT' || user?.role === 'ROLE_ADMIN';

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Brand */}
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => setCurrentView('customer')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-blue-600 to-cyan-500 flex items-center justify-center text-white shadow-md shadow-indigo-200">
              <Bot className="w-6 h-6" />
            </div>
            <div>
              <span className="font-bold text-xl text-slate-900 tracking-tight flex items-center gap-1.5">
                Support<span className="text-indigo-600">AI</span>
                <span className="text-[10px] uppercase font-extrabold bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded-full border border-indigo-200">
                  v1.0
                </span>
              </span>
              <p className="text-[11px] text-slate-500 hidden sm:block">Intelligent Multi-Modal Customer Support</p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="flex items-center gap-1 sm:gap-2">
            <button
              onClick={() => setCurrentView('customer')}
              className={`px-3.5 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-2 ${
                currentView === 'customer'
                  ? 'bg-indigo-50 text-indigo-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <LifeBuoy className="w-4 h-4" />
              <span>Customer Portal</span>
            </button>

            {isAgentOrAdmin && (
              <button
                onClick={() => setCurrentView('agent')}
                className={`px-3.5 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-2 ${
                  currentView === 'agent'
                    ? 'bg-indigo-50 text-indigo-700 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`}
              >
                <LayoutDashboard className="w-4 h-4" />
                <span>Agent Dashboard</span>
              </button>
            )}

            <button
              onClick={() => setCurrentView('faq')}
              className={`px-3.5 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-2 ${
                currentView === 'faq'
                  ? 'bg-indigo-50 text-indigo-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              <span>Knowledge Base</span>
            </button>
          </nav>

          {/* Right Action & Role Switcher */}
          <div className="flex items-center gap-3">
            {/* Quick Demo Switcher */}
            <div className="hidden md:flex items-center bg-slate-100 rounded-lg p-1 text-xs border border-slate-200">
              <span className="text-slate-500 px-2 flex items-center gap-1">
                <ArrowRightLeft className="w-3 h-3" /> Quick Switch:
              </span>
              <button
                onClick={() => {
                  demoLogin('CUSTOMER');
                  setCurrentView('customer');
                }}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  user?.role === 'ROLE_CUSTOMER' ? 'bg-white text-indigo-600 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Customer
              </button>
              <button
                onClick={() => {
                  demoLogin('AGENT');
                  setCurrentView('agent');
                }}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  user?.role === 'ROLE_AGENT' ? 'bg-white text-indigo-600 shadow-xs' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Agent
              </button>
            </div>

            {/* User Profile / Logout */}
            <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
              <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-700 flex items-center justify-center font-bold text-xs">
                {user?.fullName ? user.fullName[0] : 'U'}
              </div>
              <div className="hidden lg:block text-left">
                <p className="text-xs font-semibold text-slate-800 leading-tight">{user?.fullName}</p>
                <p className="text-[10px] text-slate-500 capitalize">
                  {user?.role ? user.role.replace('ROLE_', '').toLowerCase() : 'User'}
                </p>
              </div>
              <button
                onClick={logout}
                title="Sign Out"
                className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors ml-1"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>

          </div>

        </div>
      </div>
    </header>
  );
};
