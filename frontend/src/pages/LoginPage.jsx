import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Bot, 
  Lock, 
  Mail, 
  User, 
  ArrowRight, 
  ShieldCheck, 
  Sparkles, 
  AlertCircle,
  Loader2
} from 'lucide-react';

export const LoginPage = () => {
  const { login, register, demoLogin, loading } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (isRegister) {
      if (!fullName.trim() || !email.trim() || !password.trim()) {
        setError('Please fill in all fields.');
        return;
      }
      const res = await register(email, password, fullName);
      if (!res.success) setError(res.message);
    } else {
      if (!email.trim() || !password.trim()) {
        setError('Please enter your email and password.');
        return;
      }
      const res = await login(email, password);
      if (!res.success) setError(res.message);
    }
  };

  const handleDemoClick = async (role) => {
    setError('');
    const res = await demoLogin(role);
    if (!res.success) setError(res.message);
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      
      {/* Background Glows */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10 text-center space-y-2">
        <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-tr from-indigo-500 via-blue-600 to-cyan-400 items-center justify-center text-white shadow-xl shadow-indigo-500/30">
          <Bot className="w-8 h-8" />
        </div>
        <h2 className="text-3xl font-extrabold text-white tracking-tight">
          Support<span className="text-indigo-400">AI</span> Platform
        </h2>
        <p className="text-sm text-slate-400">
          Enterprise Multi-Modal AI Customer Support & Helpdesk
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-white py-8 px-6 shadow-2xl rounded-3xl sm:px-10 border border-slate-100">
          
          {/* Tabs */}
          <div className="flex border-b border-slate-200 mb-6">
            <button
              onClick={() => { setIsRegister(false); setError(''); }}
              className={`flex-1 pb-3 text-sm font-semibold text-center border-b-2 transition ${
                !isRegister ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-400 hover:text-slate-600'
              }`}
            >
              Sign In
            </button>
            <button
              onClick={() => { setIsRegister(true); setError(''); }}
              className={`flex-1 pb-3 text-sm font-semibold text-center border-b-2 transition ${
                isRegister ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-400 hover:text-slate-600'
              }`}
            >
              Create Account
            </button>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 text-red-700 text-xs rounded-xl border border-red-200 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {isRegister && (
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Full Name
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="e.g. Alex Johnson"
                    className="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  />
                </div>
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Email Address
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm shadow-md shadow-indigo-200 transition flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <>
                  <span>{isRegister ? 'Register Account' : 'Sign In'}</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Demo 1-Click Logins */}
          <div className="mt-6 pt-6 border-t border-slate-100">
            <div className="text-center mb-3">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center justify-center gap-1.5">
                <Sparkles className="w-3 h-3 text-indigo-600" />
                Instant Recruiter & Demo Logins
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => handleDemoClick('CUSTOMER')}
                disabled={loading}
                className="p-2.5 rounded-xl bg-slate-50 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-200 text-left transition group"
              >
                <div className="text-xs font-bold text-slate-800 group-hover:text-indigo-600">
                  Alex Customer
                </div>
                <div className="text-[10px] text-slate-500">Customer Portal</div>
              </button>

              <button
                type="button"
                onClick={() => handleDemoClick('AGENT')}
                disabled={loading}
                className="p-2.5 rounded-xl bg-slate-50 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-200 text-left transition group"
              >
                <div className="text-xs font-bold text-slate-800 group-hover:text-indigo-600">
                  Sarah Agent
                </div>
                <div className="text-[10px] text-slate-500">Agent AI Dashboard</div>
              </button>
            </div>

          </div>

        </div>
      </div>

    </div>
  );
};
