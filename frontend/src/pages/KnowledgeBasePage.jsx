import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { 
  BookOpen, 
  Search, 
  ThumbsUp, 
  Eye, 
  Tag, 
  ChevronDown, 
  ChevronUp, 
  HelpCircle,
  Sparkles,
  Loader2
} from 'lucide-react';

export const KnowledgeBasePage = () => {
  const [faqs, setFaqs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [expandedId, setExpandedId] = useState(null);
  const [helpfulVoted, setHelpfulVoted] = useState({});

  const loadFaqs = async () => {
    try {
      setLoading(true);
      const params = {};
      if (search.trim()) params.q = search.trim();
      if (selectedCategory !== 'ALL') params.category = selectedCategory;

      const res = await api.get('/api/faq', { params });
      setFaqs(res.data);
    } catch (err) {
      console.error('Failed to load FAQs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      loadFaqs();
    }, 300);

    return () => clearTimeout(delayDebounce);
  }, [search, selectedCategory]);

  const toggleExpand = (id) => {
    if (expandedId === id) {
      setExpandedId(null);
    } else {
      setExpandedId(id);
      api.post(`/api/faq/${id}/view`).catch(() => {});
    }
  };

  const handleHelpful = async (e, id) => {
    e.stopPropagation();
    if (helpfulVoted[id]) return;

    try {
      await api.post(`/api/faq/${id}/helpful`);
      setHelpfulVoted((prev) => ({ ...prev, [id]: true }));
      setFaqs((prev) =>
        prev.map((item) => (item.id === id ? { ...item, helpfulCount: item.helpfulCount + 1 } : item))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const categories = [
    { label: 'All Articles', value: 'ALL' },
    { label: 'Billing & Refunds', value: 'BILLING' },
    { label: 'Technical Support', value: 'TECHNICAL_SUPPORT' },
    { label: 'Account & Security', value: 'ACCOUNT_ACCESS' },
    { label: 'General Guides', value: 'GENERAL_INQUIRY' },
  ];

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        
        {/* Header Banner */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center gap-1.5 bg-indigo-50 border border-indigo-200 text-indigo-700 text-xs font-semibold px-3 py-1 rounded-full">
            <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
            <span>SupportAI Knowledge Base</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            How can we help you today?
          </h1>
          <p className="text-sm text-slate-500 max-w-lg mx-auto">
            Search our curated technical guides, billing FAQs, and security documentation for instant answers.
          </p>

          {/* Search Box */}
          <div className="relative max-w-xl mx-auto pt-2">
            <Search className="w-5 h-5 text-slate-400 absolute left-4 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by keywords (e.g. refund, password reset, webhooks)..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-12 pr-4 py-3.5 rounded-2xl border border-slate-200 bg-white shadow-sm focus:ring-2 focus:ring-indigo-500 outline-none text-sm transition"
            />
          </div>
        </div>

        {/* Category Pills */}
        <div className="flex items-center justify-center gap-2 flex-wrap">
          {categories.map((cat) => (
            <button
              key={cat.value}
              onClick={() => setSelectedCategory(cat.value)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition ${
                selectedCategory === cat.value
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-100'
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Articles List */}
        <div className="space-y-3">
          {loading ? (
            <div className="text-center py-12 bg-white rounded-2xl border border-slate-200">
              <Loader2 className="w-6 h-6 text-indigo-600 animate-spin mx-auto mb-2" />
              <p className="text-xs text-slate-500">Searching knowledge base...</p>
            </div>
          ) : faqs.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-2xl border border-slate-200 space-y-2">
              <HelpCircle className="w-10 h-10 text-slate-300 mx-auto" />
              <p className="text-sm font-semibold text-slate-700">No articles found matching your query</p>
              <p className="text-xs text-slate-400">Try searching for broader terms like "billing" or "api".</p>
            </div>
          ) : (
            faqs.map((faq) => {
              const isExpanded = expandedId === faq.id;

              return (
                <div
                  key={faq.id}
                  className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs hover:border-indigo-200 transition"
                >
                  <div
                    onClick={() => toggleExpand(faq.id)}
                    className="p-5 flex items-center justify-between cursor-pointer select-none"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] uppercase font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100">
                          {faq.category?.replace('_', ' ')}
                        </span>
                        {faq.tags && (
                          <span className="text-[11px] text-slate-400 flex items-center gap-1">
                            <Tag className="w-3 h-3" />
                            {faq.tags.split(',').slice(0, 2).join(', ')}
                          </span>
                        )}
                      </div>
                      <h3 className="text-base font-bold text-slate-900">{faq.title}</h3>
                    </div>

                    <div className="flex items-center gap-3 text-slate-400">
                      {isExpanded ? <ChevronUp className="w-5 h-5 text-indigo-600" /> : <ChevronDown className="w-5 h-5" />}
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="px-5 pb-5 pt-1 border-t border-slate-100 text-sm text-slate-700 leading-relaxed bg-slate-50/50">
                      <p className="whitespace-pre-line">{faq.content}</p>

                      {/* Footer Actions */}
                      <div className="mt-4 pt-4 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
                        <div className="flex items-center gap-4">
                          <span className="flex items-center gap-1">
                            <Eye className="w-3.5 h-3.5" />
                            <span>{faq.viewCount} views</span>
                          </span>
                          <span className="flex items-center gap-1">
                            <ThumbsUp className="w-3.5 h-3.5 text-indigo-600" />
                            <span>{faq.helpfulCount} people found this helpful</span>
                          </span>
                        </div>

                        <button
                          onClick={(e) => handleHelpful(e, faq.id)}
                          disabled={helpfulVoted[faq.id]}
                          className={`px-3 py-1.5 rounded-lg border text-xs font-semibold flex items-center gap-1.5 transition ${
                            helpfulVoted[faq.id]
                              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                              : 'bg-white hover:bg-slate-100 text-slate-700 border-slate-300'
                          }`}
                        >
                          <ThumbsUp className="w-3 h-3" />
                          <span>{helpfulVoted[faq.id] ? 'Marked Helpful!' : 'Was this helpful?'}</span>
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>

      </div>
    </div>
  );
};
