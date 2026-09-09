import React, { useState, useEffect, useMemo } from 'react';
import api from '../api/axios';
import { 
  X, 
  Sparkles, 
  AlertTriangle, 
  Tag, 
  Clock, 
  Flame, 
  Smile, 
  Frown, 
  Meh, 
  Send,
  Loader2
} from 'lucide-react';

export const TicketCreateModal = ({ isOpen, onClose, onCreated }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [priority, setPriority] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  // Real-time AI preview calculation
  const aiPreview = useMemo(() => {
    const text = (title + ' ' + description).toLowerCase();

    // 1. Sentiment preview
    let sentiment = 'NEUTRAL';
    let score = 0.0;
    if (text.includes('furious') || text.includes('unacceptable') || text.includes('charged twice') || text.includes('scam') || text.includes('refund immediately')) {
      sentiment = 'FRUSTRATED';
      score = -0.8;
    } else if (text.includes('bad') || text.includes('fail') || text.includes('error') || text.includes('not working') || text.includes('broken') || text.includes('slow')) {
      sentiment = 'NEGATIVE';
      score = -0.4;
    } else if (text.includes('great') || text.includes('thanks') || text.includes('awesome') || text.includes('love')) {
      sentiment = 'POSITIVE';
      score = 0.6;
    }

    // 2. Category preview
    let detectedCategory = 'GENERAL_INQUIRY';
    if (text.includes('bill') || text.includes('charge') || text.includes('refund') || text.includes('invoice') || text.includes('payment') || text.includes('subscription')) {
      detectedCategory = 'BILLING';
    } else if (text.includes('password') || text.includes('login') || text.includes('2fa') || text.includes('account') || text.includes('auth')) {
      detectedCategory = 'ACCOUNT_ACCESS';
    } else if (text.includes('api') || text.includes('webhook') || text.includes('error') || text.includes('bug') || text.includes('crash') || text.includes('500') || text.includes('code')) {
      detectedCategory = 'TECHNICAL_SUPPORT';
    } else if (text.includes('feature') || text.includes('suggest') || text.includes('add') || text.includes('enhancement')) {
      detectedCategory = 'FEATURE_REQUEST';
    }

    // 3. Priority preview
    let detectedPriority = 'MEDIUM';
    if (sentiment === 'FRUSTRATED' || text.includes('production down') || text.includes('charged twice') || text.includes('outage') || text.includes('security')) {
      detectedPriority = 'URGENT';
    } else if (sentiment === 'NEGATIVE' || text.includes('cannot login') || text.includes('refund') || text.includes('blocked')) {
      detectedPriority = 'HIGH';
    } else if (detectedCategory === 'FEATURE_REQUEST') {
      detectedPriority = 'LOW';
    }

    return { sentiment, score, detectedCategory, detectedPriority };
  }, [title, description]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !description.trim()) {
      setError('Title and description are required.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const payload = {
        title: title.trim(),
        description: description.trim(),
        category: category ? category : null,
        priority: priority ? priority : null,
      };

      await api.post('/api/tickets', payload);
      onCreated();
      onClose();
      setTitle('');
      setDescription('');
      setCategory('');
      setPriority('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create ticket. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const getSentimentIcon = () => {
    switch (aiPreview.sentiment) {
      case 'FRUSTRATED':
        return <Flame className="w-4 h-4 text-red-500 animate-pulse" />;
      case 'NEGATIVE':
        return <Frown className="w-4 h-4 text-amber-500" />;
      case 'POSITIVE':
        return <Smile className="w-4 h-4 text-emerald-500" />;
      default:
        return <Meh className="w-4 h-4 text-slate-400" />;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-slate-100 overflow-hidden animate-in fade-in zoom-in duration-200">
        
        {/* Header */}
        <div className="px-6 py-4 bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              <Sparkles className="w-5 h-5 text-indigo-400" />
            </div>
            <div>
              <h2 className="text-lg font-bold">Create New Support Ticket</h2>
              <p className="text-xs text-slate-300">AI automatically classifies category, sentiment & priority</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {error && (
            <div className="p-3 bg-red-50 text-red-700 text-xs rounded-lg border border-red-200 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-500 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
              Ticket Subject / Issue Title *
            </label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Charged twice on credit card, or API Webhook failing..."
              className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
              Detailed Description *
            </label>
            <textarea
              required
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe your issue with error messages, steps taken, or transaction reference..."
              className="w-full px-3.5 py-2.5 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
            />
          </div>

          {/* Real-time AI Analysis Card */}
          {(title || description) && (
            <div className="bg-indigo-50/70 border border-indigo-100 rounded-xl p-3.5 transition-all">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-indigo-900 flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
                  Real-Time AI Insight Preview:
                </span>
                <span className="text-[10px] text-indigo-600 uppercase font-semibold bg-indigo-100/60 px-2 py-0.5 rounded-full">
                  Auto-Calculated
                </span>
              </div>
              <div className="grid grid-cols-3 gap-2 text-xs">
                <div className="bg-white p-2 rounded-lg border border-indigo-50 flex items-center gap-2">
                  <Tag className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <span className="text-[10px] text-slate-500 block">Category</span>
                    <span className="font-semibold text-slate-800">{aiPreview.detectedCategory.replace('_', ' ')}</span>
                  </div>
                </div>

                <div className="bg-white p-2 rounded-lg border border-indigo-50 flex items-center gap-2">
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <span className="text-[10px] text-slate-500 block">Priority</span>
                    <span className={`font-semibold ${aiPreview.detectedPriority === 'URGENT' ? 'text-red-600' : 'text-slate-800'}`}>
                      {aiPreview.detectedPriority}
                    </span>
                  </div>
                </div>

                <div className="bg-white p-2 rounded-lg border border-indigo-50 flex items-center gap-2">
                  {getSentimentIcon()}
                  <div>
                    <span className="text-[10px] text-slate-500 block">Sentiment</span>
                    <span className="font-semibold text-slate-800">{aiPreview.sentiment}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Manual Override Controls (Optional) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-1">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Category Override (Optional)
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
              >
                <option value="">Auto-Detect via AI</option>
                <option value="BILLING">Billing</option>
                <option value="TECHNICAL_SUPPORT">Technical Support</option>
                <option value="ACCOUNT_ACCESS">Account Access</option>
                <option value="FEATURE_REQUEST">Feature Request</option>
                <option value="GENERAL_INQUIRY">General Inquiry</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Priority Override (Optional)
              </label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
              >
                <option value="">Auto-Detect via AI</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm shadow-md shadow-indigo-200 transition flex items-center gap-2 disabled:opacity-50"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Submitting & Analyzing...</span>
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  <span>Submit Ticket</span>
                </>
              )}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
