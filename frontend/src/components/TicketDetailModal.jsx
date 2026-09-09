import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { 
  X, 
  Send, 
  Sparkles, 
  AlertCircle, 
  Clock, 
  CheckCircle2, 
  User, 
  Bot, 
  ShieldAlert, 
  Flame, 
  Smile, 
  Frown, 
  Meh, 
  ArrowUpRight,
  Loader2
} from 'lucide-react';

export const TicketDetailModal = ({ ticketId, onClose, onUpdated }) => {
  const { user } = useAuth();
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reply, setReply] = useState('');
  const [submittingReply, setSubmittingReply] = useState(false);
  const [generatingDraft, setGeneratingDraft] = useState(false);
  const [error, setError] = useState('');

  const isAgentOrAdmin = user?.role === 'ROLE_AGENT' || user?.role === 'ROLE_ADMIN';

  const loadTicket = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/api/tickets/${ticketId}`);
      setTicket(res.data);
    } catch (err) {
      setError('Failed to load ticket details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (ticketId) {
      loadTicket();
    }
  }, [ticketId]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!reply.trim()) return;

    setSubmittingReply(true);
    try {
      const res = await api.post(`/api/tickets/${ticketId}/messages`, { message: reply.trim() });
      setTicket(res.data);
      setReply('');
      if (onUpdated) onUpdated();
    } catch (err) {
      setError('Failed to send reply.');
    } finally {
      setSubmittingReply(false);
    }
  };

  const handleAiCopilot = async () => {
    setGeneratingDraft(true);
    try {
      const res = await api.get(`/api/tickets/${ticketId}/ai-suggestion`);
      if (res.data && res.data.suggestedReply) {
        setReply(res.data.suggestedReply);
      }
    } catch (err) {
      setError('Could not generate AI draft.');
    } finally {
      setGeneratingDraft(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    try {
      const res = await api.patch(`/api/tickets/${ticketId}/status`, { status: newStatus });
      setTicket(res.data);
      if (onUpdated) onUpdated();
    } catch (err) {
      setError('Failed to update status.');
    }
  };

  const handleEscalate = async () => {
    try {
      const res = await api.post(`/api/tickets/${ticketId}/escalate`);
      setTicket(res.data);
      if (onUpdated) onUpdated();
    } catch (err) {
      setError('Failed to escalate ticket.');
    }
  };

  if (!ticketId) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-xs">
      <div className="bg-white rounded-2xl max-w-4xl w-full h-[90vh] shadow-2xl border border-slate-100 flex flex-col overflow-hidden animate-in fade-in zoom-in duration-150">
        
        {/* Header */}
        <div className="px-6 py-4 bg-slate-900 text-white flex items-center justify-between border-b border-slate-800">
          <div className="flex items-center gap-3">
            <span className="text-xs font-mono bg-indigo-500/20 text-indigo-300 px-2.5 py-1 rounded-md border border-indigo-500/30">
              {ticket?.ticketNumber || 'Loading...'}
            </span>
            <h2 className="text-base sm:text-lg font-bold truncate max-w-md sm:max-w-xl">
              {ticket?.title}
            </h2>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition">
            <X className="w-5 h-5" />
          </button>
        </div>

        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" />
          </div>
        ) : (
          <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">
            
            {/* Main Chat / Message Thread */}
            <div className="flex-1 flex flex-col h-full bg-slate-50 border-r border-slate-200">
              
              {/* Messages list */}
              <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4">
                {ticket?.messages?.map((msg) => {
                  const isBot = msg.senderType === 'AI_BOT';
                  const isAgent = msg.senderType === 'AGENT';
                  const isCustomer = msg.senderType === 'CUSTOMER';
                  const isSystem = msg.senderType === 'SYSTEM';

                  if (isSystem) {
                    return (
                      <div key={msg.id} className="flex justify-center my-2">
                        <span className="text-xs bg-amber-100 text-amber-800 px-3 py-1 rounded-full border border-amber-200 flex items-center gap-1.5 font-medium">
                          <ShieldAlert className="w-3.5 h-3.5 text-amber-600" />
                          {msg.message}
                        </span>
                      </div>
                    );
                  }

                  return (
                    <div
                      key={msg.id}
                      className={`flex gap-3 max-w-2xl ${isCustomer ? 'mr-auto' : 'ml-auto flex-row-reverse'}`}
                    >
                      {/* Avatar */}
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white shrink-0 text-xs font-bold ${
                        isBot 
                          ? 'bg-gradient-to-tr from-cyan-500 to-blue-600 shadow-xs' 
                          : isAgent 
                          ? 'bg-indigo-600' 
                          : 'bg-slate-700'
                      }`}>
                        {isBot ? <Bot className="w-4 h-4" /> : isAgent ? <ShieldAlert className="w-4 h-4" /> : <User className="w-4 h-4" />}
                      </div>

                      {/* Bubble */}
                      <div>
                        <div className="flex items-center gap-2 mb-1 text-[11px] text-slate-500">
                          <span className="font-semibold text-slate-700">{msg.senderName}</span>
                          <span>•</span>
                          <span>{new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                          {msg.sentiment && msg.sentiment !== 'NEUTRAL' && (
                            <span className={`text-[10px] px-1.5 py-0.2 rounded font-medium ${
                              msg.sentiment === 'FRUSTRATED' ? 'bg-red-100 text-red-700' :
                              msg.sentiment === 'NEGATIVE' ? 'bg-amber-100 text-amber-700' :
                              'bg-emerald-100 text-emerald-700'
                            }`}>
                              {msg.sentiment}
                            </span>
                          )}
                        </div>

                        <div className={`p-3.5 rounded-2xl text-sm leading-relaxed whitespace-pre-line shadow-xs ${
                          isBot
                            ? 'bg-white border border-cyan-200 text-slate-800'
                            : isAgent
                            ? 'bg-indigo-600 text-white rounded-tr-xs'
                            : 'bg-white border border-slate-200 text-slate-800 rounded-tl-xs'
                        }`}>
                          {msg.message}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Reply Box */}
              <div className="p-4 bg-white border-t border-slate-200">
                {isAgentOrAdmin && (
                  <div className="flex items-center justify-between mb-2">
                    <button
                      type="button"
                      onClick={handleAiCopilot}
                      disabled={generatingDraft}
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 px-3 py-1.5 rounded-lg border border-indigo-200 transition disabled:opacity-50"
                    >
                      {generatingDraft ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
                      )}
                      <span>AI Copilot: Generate Agent Draft</span>
                    </button>
                    <span className="text-[11px] text-slate-400">Press Send to reply to customer</span>
                  </div>
                )}

                <form onSubmit={handleSendMessage} className="flex gap-2">
                  <textarea
                    rows={2}
                    value={reply}
                    onChange={(e) => setReply(e.target.value)}
                    placeholder={isAgentOrAdmin ? "Type an agent reply or click 'AI Copilot'..." : "Type your reply to support..."}
                    className="flex-1 px-3.5 py-2 text-sm rounded-xl border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none resize-none"
                  />
                  <button
                    type="submit"
                    disabled={submittingReply || !reply.trim()}
                    className="px-4 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-medium text-sm flex items-center justify-center transition disabled:opacity-50 shadow-md shadow-indigo-100"
                  >
                    {submittingReply ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
                  </button>
                </form>
              </div>

            </div>

            {/* Right Sidebar - AI Metadata & Status Controls */}
            <div className="w-full lg:w-80 bg-white p-5 space-y-6 overflow-y-auto border-t lg:border-t-0">
              
              {/* Ticket Status */}
              <div>
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2">
                  Ticket Status
                </span>
                {isAgentOrAdmin ? (
                  <div className="grid grid-cols-2 gap-2">
                    {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map((st) => (
                      <button
                        key={st}
                        onClick={() => handleStatusChange(st)}
                        className={`text-xs font-semibold py-1.5 px-2 rounded-lg border transition ${
                          ticket?.status === st
                            ? 'bg-indigo-600 text-white border-indigo-600 shadow-xs'
                            : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                        }`}
                      >
                        {st.replace('_', ' ')}
                      </button>
                    ))}
                  </div>
                ) : (
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold ${
                    ticket?.status === 'RESOLVED' ? 'bg-emerald-100 text-emerald-800' :
                    ticket?.status === 'ESCALATED' ? 'bg-red-100 text-red-800' :
                    ticket?.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                    'bg-slate-100 text-slate-800'
                  }`}>
                    {ticket?.status}
                  </span>
                )}
              </div>

              {/* AI Analysis Cards */}
              <div className="bg-slate-50 rounded-xl p-4 border border-slate-200 space-y-3">
                <div className="flex items-center gap-1.5 text-xs font-bold text-slate-900 border-b border-slate-200 pb-2">
                  <Sparkles className="w-4 h-4 text-indigo-600" />
                  <span>AI Diagnostics & NLP</span>
                </div>

                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-500">Category:</span>
                  <span className="font-semibold text-slate-800 bg-white px-2 py-0.5 rounded border border-slate-200">
                    {ticket?.category?.replace('_', ' ')}
                  </span>
                </div>

                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-500">Priority:</span>
                  <span className={`font-bold px-2 py-0.5 rounded ${
                    ticket?.priority === 'URGENT' ? 'bg-red-100 text-red-700' :
                    ticket?.priority === 'HIGH' ? 'bg-amber-100 text-amber-700' :
                    ticket?.priority === 'MEDIUM' ? 'bg-blue-100 text-blue-700' :
                    'bg-slate-100 text-slate-600'
                  }`}>
                    {ticket?.priority}
                  </span>
                </div>

                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-500">Sentiment:</span>
                  <span className={`font-bold flex items-center gap-1 ${
                    ticket?.sentimentLabel === 'FRUSTRATED' ? 'text-red-600' :
                    ticket?.sentimentLabel === 'NEGATIVE' ? 'text-amber-600' :
                    ticket?.sentimentLabel === 'POSITIVE' ? 'text-emerald-600' :
                    'text-slate-600'
                  }`}>
                    {ticket?.sentimentLabel === 'FRUSTRATED' && <Flame className="w-3.5 h-3.5 text-red-500" />}
                    {ticket?.sentimentLabel} ({ticket?.sentimentScore})
                  </span>
                </div>

                {ticket?.status === 'ESCALATED' && (
                  <div className="p-2.5 bg-red-50 text-red-800 rounded-lg border border-red-200 text-xs flex items-center gap-2">
                    <ShieldAlert className="w-4 h-4 text-red-600 shrink-0" />
                    <span>In Human Escalation Queue</span>
                  </div>
                )}
              </div>

              {/* Customer & Assignment details */}
              <div className="text-xs space-y-2.5 border-t border-slate-100 pt-3 text-slate-600">
                <div>
                  <span className="text-slate-400 block mb-0.5">Customer</span>
                  <p className="font-semibold text-slate-800">{ticket?.customerName}</p>
                  <p className="text-slate-500">{ticket?.customerEmail}</p>
                </div>

                <div>
                  <span className="text-slate-400 block mb-0.5">Assigned Agent</span>
                  <p className="font-semibold text-slate-800">
                    {ticket?.assignedAgentName || 'Unassigned (In Queue)'}
                  </p>
                </div>

                <div>
                  <span className="text-slate-400 block mb-0.5">Created At</span>
                  <p className="text-slate-700">
                    {ticket?.createdAt && new Date(ticket.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>

              {/* Escalate action */}
              {ticket?.status !== 'ESCALATED' && (
                <button
                  type="button"
                  onClick={handleEscalate}
                  className="w-full py-2 px-3 text-xs font-semibold text-red-700 bg-red-50 hover:bg-red-100 rounded-lg border border-red-200 transition flex items-center justify-center gap-1.5"
                >
                  <ArrowUpRight className="w-3.5 h-3.5" />
                  <span>Escalate to Tier-2 Agent</span>
                </button>
              )}

            </div>

          </div>
        )}

      </div>
    </div>
  );
};
