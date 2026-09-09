import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { TicketCreateModal } from '../components/TicketCreateModal';
import { TicketDetailModal } from '../components/TicketDetailModal';
import { ChatWidget } from '../components/ChatWidget';
import { 
  Plus, 
  Search, 
  Filter, 
  MessageSquare, 
  Clock, 
  CheckCircle2, 
  AlertTriangle, 
  Flame, 
  Smile, 
  Frown, 
  Meh, 
  ArrowRight,
  Sparkles,
  BookOpen,
  LifeBuoy
} from 'lucide-react';

export const CustomerPortal = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedTicketId, setSelectedTicketId] = useState(null);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/tickets');
      setTickets(res.data);
    } catch (err) {
      console.error('Failed to load tickets', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const filteredTickets = tickets.filter((t) => {
    const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          t.ticketNumber.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getSentimentBadge = (label, score) => {
    switch (label) {
      case 'FRUSTRATED':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold text-red-700 bg-red-50 border border-red-200 px-2 py-0.5 rounded-full">
            <Flame className="w-3 h-3 text-red-500 animate-pulse" />
            Frustrated ({score})
          </span>
        );
      case 'NEGATIVE':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full">
            <Frown className="w-3 h-3 text-amber-500" />
            Negative ({score})
          </span>
        );
      case 'POSITIVE':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
            <Smile className="w-3 h-3 text-emerald-500" />
            Positive ({score})
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-slate-600 bg-slate-100 border border-slate-200 px-2 py-0.5 rounded-full">
            <Meh className="w-3 h-3 text-slate-400" />
            Neutral
          </span>
        );
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'RESOLVED':
      case 'CLOSED':
        return <span className="bg-emerald-100 text-emerald-800 text-xs font-semibold px-2.5 py-1 rounded-full">Resolved</span>;
      case 'ESCALATED':
        return <span className="bg-red-100 text-red-800 text-xs font-bold px-2.5 py-1 rounded-full">Escalated to Agent</span>;
      case 'IN_PROGRESS':
        return <span className="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-1 rounded-full">In Progress</span>;
      default:
        return <span className="bg-slate-100 text-slate-700 text-xs font-semibold px-2.5 py-1 rounded-full">Open</span>;
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Hero Section */}
        <div className="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 rounded-3xl p-6 sm:p-10 text-white shadow-xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6 border border-slate-800">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-1.5 bg-indigo-500/20 text-indigo-300 text-xs font-semibold px-3 py-1 rounded-full border border-indigo-500/30">
              <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
              <span>AI-Powered Helpdesk & Assistant</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
              Welcome back, {user?.fullName || 'Customer'}!
            </h1>
            <p className="text-slate-300 text-sm max-w-xl">
              Track your tickets in real-time, submit new inquiries with instant AI classification, or chat live with our intelligent AI support agent.
            </p>
          </div>

          <button
            onClick={() => setIsCreateOpen(true)}
            className="px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm shadow-lg shadow-indigo-600/30 hover:scale-102 transition flex items-center gap-2 shrink-0"
          >
            <Plus className="w-5 h-5" />
            <span>Create New Ticket</span>
          </button>
        </div>

        {/* Search & Filter Bar */}
        <div className="bg-white p-4 rounded-2xl shadow-xs border border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search tickets by ID or title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 focus:ring-2 focus:ring-indigo-500 outline-none"
            />
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
            <span className="text-xs text-slate-500 font-medium flex items-center gap-1">
              <Filter className="w-3.5 h-3.5" /> Status:
            </span>
            {['ALL', 'OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED'].map((st) => (
              <button
                key={st}
                onClick={() => setStatusFilter(st)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                  statusFilter === st
                    ? 'bg-indigo-600 text-white shadow-xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {st.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {/* Tickets Grid / List */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900">Your Support Tickets</h2>
            <span className="text-xs text-slate-500 font-medium">
              Showing {filteredTickets.length} of {tickets.length} tickets
            </span>
          </div>

          {loading ? (
            <div className="text-center py-16 bg-white rounded-2xl border border-slate-200">
              <div className="inline-block w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
              <p className="text-sm text-slate-500 mt-3">Loading your tickets...</p>
            </div>
          ) : filteredTickets.length === 0 ? (
            <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 p-8 space-y-3">
              <LifeBuoy className="w-12 h-12 text-slate-300 mx-auto" />
              <h3 className="text-base font-semibold text-slate-700">No support tickets found</h3>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">
                You don't have any tickets matching your search. Create a new one or chat with SupportAI assistant.
              </p>
              <button
                onClick={() => setIsCreateOpen(true)}
                className="mt-2 px-4 py-2 bg-indigo-600 text-white rounded-lg text-xs font-semibold hover:bg-indigo-700 transition"
              >
                Create Your First Ticket
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredTickets.map((ticket) => (
                <div
                  key={ticket.id}
                  onClick={() => setSelectedTicketId(ticket.id)}
                  className="bg-white rounded-2xl p-5 border border-slate-200 hover:border-indigo-300 hover:shadow-md transition-all cursor-pointer flex flex-col justify-between group"
                >
                  <div>
                    {/* Header: ID, Date, Status */}
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-xs font-mono text-indigo-600 font-semibold bg-indigo-50 px-2 py-0.5 rounded">
                        {ticket.ticketNumber}
                      </span>
                      <div className="flex items-center gap-2">
                        {getStatusBadge(ticket.status)}
                      </div>
                    </div>

                    {/* Title & Description */}
                    <h3 className="font-bold text-slate-900 text-base group-hover:text-indigo-600 transition line-clamp-1 mb-1">
                      {ticket.title}
                    </h3>
                    <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed mb-4">
                      {ticket.description}
                    </p>
                  </div>

                  {/* Footer: Badges & View */}
                  <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="bg-slate-100 text-slate-700 font-medium px-2 py-0.5 rounded text-[11px]">
                        {ticket.category?.replace('_', ' ')}
                      </span>
                      {getSentimentBadge(ticket.sentimentLabel, ticket.sentimentScore)}
                    </div>

                    <span className="text-indigo-600 font-semibold flex items-center gap-1 group-hover:translate-x-1 transition text-xs">
                      <span>View</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </span>
                  </div>

                </div>
              ))}
            </div>
          )}
        </div>

      </div>

      {/* Ticket Creation Modal */}
      <TicketCreateModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onCreated={fetchTickets}
      />

      {/* Ticket Detail Modal */}
      {selectedTicketId && (
        <TicketDetailModal
          ticketId={selectedTicketId}
          onClose={() => setSelectedTicketId(null)}
          onUpdated={fetchTickets}
        />
      )}

      {/* Floating AI Chatbot */}
      <ChatWidget isFloating={true} />

    </div>
  );
};
