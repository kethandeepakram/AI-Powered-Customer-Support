import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { TicketDetailModal } from '../components/TicketDetailModal';
import { 
  LayoutDashboard, 
  LifeBuoy, 
  ShieldAlert, 
  CheckCircle2, 
  Clock, 
  Flame, 
  Smile, 
  Frown, 
  Meh, 
  Sparkles, 
  Search, 
  Filter, 
  ArrowUpRight, 
  Layers, 
  BarChart3,
  UserCheck,
  RefreshCw,
  Loader2
} from 'lucide-react';

export const AgentDashboard = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedTicketId, setSelectedTicketId] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      const [ticketsRes, analyticsRes] = await Promise.all([
        api.get('/api/tickets'),
        api.get('/api/analytics/summary')
      ]);
      setTickets(ticketsRes.data);
      setAnalytics(analyticsRes.data);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredTickets = tickets.filter((t) => {
    const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          t.ticketNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (t.customerName && t.customerName.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
    const matchesPriority = priorityFilter === 'ALL' || t.priority === priorityFilter;
    return matchesSearch && matchesStatus && matchesPriority;
  });

  const escalatedTickets = tickets.filter((t) => t.status === 'ESCALATED');

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

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'URGENT':
        return <span className="bg-red-100 text-red-800 text-[11px] font-bold px-2 py-0.5 rounded">URGENT</span>;
      case 'HIGH':
        return <span className="bg-amber-100 text-amber-800 text-[11px] font-bold px-2 py-0.5 rounded">HIGH</span>;
      case 'MEDIUM':
        return <span className="bg-blue-100 text-blue-800 text-[11px] font-medium px-2 py-0.5 rounded">MEDIUM</span>;
      default:
        return <span className="bg-slate-100 text-slate-600 text-[11px] font-medium px-2 py-0.5 rounded">LOW</span>;
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Top Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
                Support Agent AI Workspace
              </h1>
              <span className="bg-indigo-100 text-indigo-800 text-xs font-bold px-2.5 py-0.5 rounded-full">
                Live Copilot Active
              </span>
            </div>
            <p className="text-xs sm:text-sm text-slate-500">
              Manage incoming tickets, review AI sentiment & classification, and generate AI response drafts.
            </p>
          </div>

          <button
            onClick={loadData}
            disabled={loading}
            className="px-3.5 py-2 bg-white hover:bg-slate-100 border border-slate-200 text-slate-700 rounded-xl text-xs font-semibold shadow-xs flex items-center gap-2 self-start sm:self-auto transition"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh Feed</span>
          </button>
        </div>

        {/* KPI Metrics Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
          
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-slate-500">Total Inquiries</span>
              <Layers className="w-4 h-4 text-indigo-600" />
            </div>
            <p className="text-2xl font-extrabold text-slate-900">{analytics?.totalTickets ?? 0}</p>
            <p className="text-[11px] text-slate-400 mt-1">Across all categories</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-slate-500">In Progress</span>
              <Clock className="w-4 h-4 text-blue-600" />
            </div>
            <p className="text-2xl font-extrabold text-blue-600">{analytics?.inProgressTickets ?? 0}</p>
            <p className="text-[11px] text-slate-400 mt-1">Active investigations</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-red-200 bg-red-50/20 shadow-xs">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-red-700">Escalated Queue</span>
              <ShieldAlert className="w-4 h-4 text-red-600 animate-pulse" />
            </div>
            <p className="text-2xl font-extrabold text-red-600">{analytics?.escalatedTickets ?? 0}</p>
            <p className="text-[11px] text-red-500 mt-1">Requires human handoff</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-slate-500">Resolved Rate</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            </div>
            <p className="text-2xl font-extrabold text-emerald-600">{analytics?.resolutionRate ?? 0}%</p>
            <p className="text-[11px] text-slate-400 mt-1">{analytics?.resolvedTickets ?? 0} tickets closed</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs col-span-2 lg:col-span-1">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-slate-500">Avg Sentiment</span>
              <Sparkles className="w-4 h-4 text-purple-600" />
            </div>
            <p className={`text-2xl font-extrabold ${
              (analytics?.averageSentimentScore ?? 0) < 0 ? 'text-amber-600' : 'text-emerald-600'
            }`}>
              {analytics?.averageSentimentScore ?? 0.0}
            </p>
            <p className="text-[11px] text-slate-400 mt-1">Polarity (-1.0 to +1.0)</p>
          </div>

        </div>

        {/* Escalated Queue Alert Banner */}
        {escalatedTickets.length > 0 && (
          <div className="bg-gradient-to-r from-red-600 to-rose-600 text-white p-4 rounded-2xl shadow-md flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-white/20 rounded-xl">
                <Flame className="w-5 h-5 text-white animate-bounce" />
              </div>
              <div>
                <h3 className="font-bold text-sm">
                  {escalatedTickets.length} Critical Escalated {escalatedTickets.length === 1 ? 'Ticket' : 'Tickets'} Awaiting Action
                </h3>
                <p className="text-xs text-red-100">
                  Customers expressing frustration or high SLA urgency require live agent responses.
                </p>
              </div>
            </div>
            <button
              onClick={() => setStatusFilter('ESCALATED')}
              className="px-3.5 py-1.5 bg-white text-red-700 hover:bg-red-50 rounded-lg text-xs font-bold transition shadow-xs"
            >
              Filter Escalated
            </button>
          </div>
        )}

        {/* Tickets Filter and Search */}
        <div className="bg-white p-4 rounded-2xl shadow-xs border border-slate-200 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by ticket #, customer, or title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm rounded-xl border border-slate-200 focus:ring-2 focus:ring-indigo-500 outline-none"
            />
          </div>

          <div className="flex items-center gap-3 w-full md:w-auto flex-wrap">
            {/* Status Filter */}
            <div className="flex items-center gap-1">
              <span className="text-xs text-slate-500 font-medium">Status:</span>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white font-medium outline-none"
              >
                <option value="ALL">All Statuses</option>
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="ESCALATED">Escalated</option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </div>

            {/* Priority Filter */}
            <div className="flex items-center gap-1">
              <span className="text-xs text-slate-500 font-medium">Priority:</span>
              <select
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value)}
                className="px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 bg-white font-medium outline-none"
              >
                <option value="ALL">All Priorities</option>
                <option value="URGENT">Urgent</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </div>
          </div>
        </div>

        {/* Tickets Table */}
        <div className="bg-white rounded-2xl shadow-xs border border-slate-200 overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <span>All Tickets</span>
              <span className="text-xs font-normal text-slate-400">({filteredTickets.length} found)</span>
            </h3>
          </div>

          {loading ? (
            <div className="p-12 text-center text-slate-500">
              <Loader2 className="w-8 h-8 text-indigo-600 animate-spin mx-auto mb-2" />
              <p className="text-xs">Loading ticket queue...</p>
            </div>
          ) : filteredTickets.length === 0 ? (
            <div className="p-12 text-center text-slate-500 space-y-2">
              <LifeBuoy className="w-10 h-10 text-slate-300 mx-auto" />
              <p className="text-sm font-medium">No tickets match current filters</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 text-[11px] font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                    <th className="py-3 px-4">Ticket</th>
                    <th className="py-3 px-4">Customer</th>
                    <th className="py-3 px-4">AI Category</th>
                    <th className="py-3 px-4">Priority</th>
                    <th className="py-3 px-4">AI Sentiment</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {filteredTickets.map((t) => (
                    <tr 
                      key={t.id} 
                      className={`hover:bg-slate-50/80 transition ${t.status === 'ESCALATED' ? 'bg-red-50/30' : ''}`}
                    >
                      <td className="py-3 px-4 font-medium">
                        <div className="font-mono text-[11px] text-indigo-600 font-semibold">{t.ticketNumber}</div>
                        <div className="font-bold text-slate-800 line-clamp-1 max-w-xs">{t.title}</div>
                      </td>

                      <td className="py-3 px-4">
                        <div className="font-semibold text-slate-700">{t.customerName || 'Customer'}</div>
                        <div className="text-[10px] text-slate-400">{t.customerEmail}</div>
                      </td>

                      <td className="py-3 px-4">
                        <span className="bg-slate-100 text-slate-700 font-medium px-2 py-0.5 rounded text-[11px]">
                          {t.category?.replace('_', ' ')}
                        </span>
                      </td>

                      <td className="py-3 px-4">
                        {getPriorityBadge(t.priority)}
                      </td>

                      <td className="py-3 px-4">
                        {getSentimentBadge(t.sentimentLabel, t.sentimentScore)}
                      </td>

                      <td className="py-3 px-4">
                        <span className={`px-2.5 py-0.5 rounded-full font-semibold text-[11px] ${
                          t.status === 'ESCALATED' ? 'bg-red-100 text-red-800' :
                          t.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                          t.status === 'RESOLVED' ? 'bg-emerald-100 text-emerald-800' :
                          'bg-slate-100 text-slate-700'
                        }`}>
                          {t.status?.replace('_', ' ')}
                        </span>
                      </td>

                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => setSelectedTicketId(t.id)}
                          className="px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-lg font-semibold text-xs border border-indigo-200 transition inline-flex items-center gap-1.5 shadow-2xs"
                        >
                          <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
                          <span>AI Copilot & Reply</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>

      {/* Ticket Detail & Copilot Modal */}
      {selectedTicketId && (
        <TicketDetailModal
          ticketId={selectedTicketId}
          onClose={() => setSelectedTicketId(null)}
          onUpdated={loadData}
        />
      )}
    </div>
  );
};
