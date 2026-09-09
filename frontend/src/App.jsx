import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { CustomerPortal } from './pages/CustomerPortal';
import { AgentDashboard } from './pages/AgentDashboard';
import { KnowledgeBasePage } from './pages/KnowledgeBasePage';
import { LoginPage } from './pages/LoginPage';

const MainLayout = () => {
  const { user } = useAuth();
  const [currentView, setCurrentView] = useState(() => {
    return user?.role === 'ROLE_AGENT' || user?.role === 'ROLE_ADMIN' ? 'agent' : 'customer';
  });

  if (!user) {
    return <LoginPage />;
  }

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar currentView={currentView} setCurrentView={setCurrentView} />

      <main className="flex-1">
        {currentView === 'customer' && <CustomerPortal />}
        {currentView === 'agent' && <AgentDashboard />}
        {currentView === 'faq' && <KnowledgeBasePage />}
      </main>
    </div>
  );
};

export function App() {
  return (
    <AuthProvider>
      <MainLayout />
    </AuthProvider>
  );
}

export default App;
