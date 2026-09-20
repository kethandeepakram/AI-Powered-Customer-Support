import React, { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { CustomerPortal } from './pages/CustomerPortal';
import { AgentDashboard } from './pages/AgentDashboard';
import { KnowledgeBasePage } from './pages/KnowledgeBasePage';
import { LoginPage } from './pages/LoginPage';

const MainLayout = () => {
  const { user } = useAuth();
  const isAgentOrAdmin = user?.role === 'ROLE_AGENT' || user?.role === 'ROLE_ADMIN';

  const [currentView, setCurrentView] = useState(
    isAgentOrAdmin ? 'agent' : 'customer'
  );

  // Reset the dashboard whenever the authenticated role changes.
  useEffect(() => {
    setCurrentView(isAgentOrAdmin ? 'agent' : 'customer');
  }, [isAgentOrAdmin]);

  if (!user) {
    return <LoginPage />;
  }

  // Customers can never render the agent dashboard, even if the view
  // state is changed accidentally or by stale client state.
  const safeView =
    currentView === 'agent' && !isAgentOrAdmin
      ? 'customer'
      : currentView;

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar currentView={safeView} setCurrentView={setCurrentView} />

      <main className="flex-1">
        {safeView === 'customer' && <CustomerPortal />}
        {safeView === 'agent' && isAgentOrAdmin && <AgentDashboard />}
        {safeView === 'faq' && <KnowledgeBasePage />}
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
