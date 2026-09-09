import React, { useState, useEffect, useRef } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { 
  Bot, 
  X, 
  Send, 
  Sparkles, 
  User, 
  Flame, 
  Smile, 
  Frown, 
  Meh, 
  LifeBuoy, 
  ArrowUpRight, 
  ExternalLink,
  Loader2,
  Minimize2
} from 'lucide-react';

export const ChatWidget = ({ isFloating = true }) => {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(!isFloating);
  const [sessionId, setSessionId] = useState(() => localStorage.getItem('chatSessionId') || '');
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isEscalated, setIsEscalated] = useState(false);
  const [escalatedTicketId, setEscalatedTicketId] = useState(null);

  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      senderType: 'AI_BOT',
      content: "Hello! I'm SupportAI Assistant. How can I help you today? Ask me about billing, account security, API rate limits, or type 'human' to reach a specialist.",
      sentiment: 'POSITIVE',
      suggestedFaqs: []
    }
  ]);

  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isOpen]);

  // Load existing history if sessionId exists
  useEffect(() => {
    if (sessionId) {
      api.get(`/api/chat/history/${sessionId}`)
        .then((res) => {
          if (res.data && res.data.length > 0) {
            setMessages(res.data);
          }
        })
        .catch(() => {});
    }
  }, [sessionId]);

  const handleSend = async (e) => {
    e?.preventDefault();
    if (!input.trim() || loading) return;

    const userText = input.trim();
    setInput('');

    // Optimistic user message
    const tempUserMsg = {
      id: Date.now(),
      senderType: 'CUSTOMER',
      content: userText,
      sentiment: 'NEUTRAL',
      createdAt: new Date().toISOString()
    };
    setMessages((prev) => [...prev, tempUserMsg]);
    setLoading(true);

    try {
      const res = await api.post('/api/chat/message', {
        sessionId: sessionId || null,
        message: userText,
        customerName: user?.fullName || 'Alex Guest',
      });

      const data = res.data;
      if (data.sessionId && data.sessionId !== sessionId) {
        setSessionId(data.sessionId);
        localStorage.setItem('chatSessionId', data.sessionId);
      }

      if (data.escalated) {
        setIsEscalated(true);
        if (data.escalatedTicketId) {
          setEscalatedTicketId(data.escalatedTicketId);
        }
      }

      const botMsg = {
        id: Date.now() + 1,
        senderType: 'AI_BOT',
        content: data.content,
        sentiment: data.sentiment,
        suggestedFaqs: data.suggestedFaqs || [],
        createdAt: data.createdAt || new Date().toISOString()
      };

      setMessages((prev) => [...prev, botMsg]);
    } catch (err) {
      const errorMsg = {
        id: Date.now() + 1,
        senderType: 'AI_BOT',
        content: "I'm having trouble connecting right now. You can try submitting a ticket directly from the portal.",
        sentiment: 'NEUTRAL'
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleEscalateNow = async () => {
    if (isEscalated) return;
    setLoading(true);

    try {
      const res = await api.post('/api/chat/escalate', {
        sessionId: sessionId || 'session-' + Date.now(),
        customerName: user?.fullName || 'Customer',
        customerEmail: user?.email || 'customer@supportai.com',
        title: 'Customer requested human escalation via live chat',
        reason: 'User clicked Transfer to Human Specialist'
      });

      setIsEscalated(true);
      if (res.data?.id) {
        setEscalatedTicketId(res.data.id);
      }

      const confirmMsg = {
        id: Date.now(),
        senderType: 'AI_BOT',
        content: `I've connected you to our live agent queue (Ticket #${res.data?.ticketNumber || 'Assigned'}). A human specialist will review our conversation and take over shortly!`,
        sentiment: 'POSITIVE'
      };
      setMessages((prev) => [...prev, confirmMsg]);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Floating Launcher Button (if in floating mode) */}
      {isFloating && !isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-50 p-4 rounded-full bg-gradient-to-tr from-indigo-600 to-blue-600 text-white shadow-xl shadow-indigo-300 hover:scale-105 active:scale-95 transition-all flex items-center gap-2.5 font-medium group"
        >
          <Bot className="w-6 h-6 animate-pulse" />
          <span className="text-sm pr-1">Need Help? Chat with AI</span>
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 absolute top-1 right-1 border-2 border-white"></span>
        </button>
      )}

      {/* Chat Window */}
      {isOpen && (
        <div className={isFloating 
          ? "fixed bottom-5 right-5 z-50 w-[94vw] sm:w-[420px] h-[600px] max-h-[85vh] bg-white rounded-2xl shadow-2xl border border-slate-200 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-5 duration-200"
          : "w-full h-full bg-white rounded-2xl shadow-sm border border-slate-200 flex flex-col overflow-hidden"
        }>
          
          {/* Header */}
          <div className="px-5 py-4 bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white flex items-center justify-between shadow-xs">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-400 to-indigo-500 flex items-center justify-center text-white shadow-md">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-sm flex items-center gap-1.5">
                  SupportAI Assistant
                  <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                </h3>
                <p className="text-[11px] text-slate-300">Powered by Gemini & NLP Engine</p>
              </div>
            </div>

            <div className="flex items-center gap-1">
              {!isEscalated && (
                <button
                  onClick={handleEscalateNow}
                  title="Speak with a Human Representative"
                  className="px-2.5 py-1 text-[11px] font-semibold text-indigo-200 hover:text-white bg-indigo-500/20 hover:bg-indigo-500/30 rounded-lg border border-indigo-400/30 transition flex items-center gap-1"
                >
                  <ArrowUpRight className="w-3 h-3" />
                  <span>Human Agent</span>
                </button>
              )}
              {isFloating && (
                <button
                  onClick={() => setIsOpen(false)}
                  className="p-1.5 text-slate-400 hover:text-white hover:bg-white/10 rounded-lg transition"
                >
                  <Minimize2 className="w-4 h-4" />
                </button>
              )}
            </div>
          </div>

          {/* Escalation Alert Banner */}
          {isEscalated && (
            <div className="bg-amber-50 border-b border-amber-200 px-4 py-2 text-xs text-amber-800 flex items-center justify-between">
              <div className="flex items-center gap-1.5 font-medium">
                <LifeBuoy className="w-4 h-4 text-amber-600 shrink-0" />
                <span>Ticket #{escalatedTicketId || 'Queue'} created for Human Agent</span>
              </div>
              <span className="bg-amber-200 text-amber-900 text-[10px] uppercase font-bold px-1.5 py-0.5 rounded">
                Escalated
              </span>
            </div>
          )}

          {/* Messages Feed */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50">
            {messages.map((msg, idx) => {
              const isUser = msg.senderType === 'CUSTOMER';

              return (
                <div key={idx} className={`flex gap-2.5 max-w-[85%] ${isUser ? 'ml-auto flex-row-reverse' : 'mr-auto'}`}>
                  
                  {/* Avatar */}
                  <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 text-white text-xs ${
                    isUser ? 'bg-slate-700' : 'bg-gradient-to-tr from-indigo-600 to-cyan-500'
                  }`}>
                    {isUser ? <User className="w-3.5 h-3.5" /> : <Bot className="w-3.5 h-3.5" />}
                  </div>

                  {/* Message Body */}
                  <div>
                    <div className={`p-3.5 rounded-2xl text-xs sm:text-sm leading-relaxed whitespace-pre-line shadow-xs ${
                      isUser 
                        ? 'bg-indigo-600 text-white rounded-tr-xs' 
                        : 'bg-white border border-slate-200 text-slate-800 rounded-tl-xs'
                    }`}>
                      {msg.content}
                    </div>

                    {/* Grounding FAQ chips */}
                    {!isUser && msg.suggestedFaqs && msg.suggestedFaqs.length > 0 && (
                      <div className="mt-2 space-y-1">
                        <span className="text-[10px] text-slate-400 font-semibold block uppercase">
                          Related Knowledge Base Articles:
                        </span>
                        {msg.suggestedFaqs.map((faq) => (
                          <div
                            key={faq.id}
                            className="bg-white hover:bg-indigo-50 border border-slate-200 hover:border-indigo-200 rounded-lg p-2 text-xs text-indigo-700 cursor-pointer transition flex items-center justify-between"
                            onClick={() => {
                              setInput(`Can you explain "${faq.title}" in detail?`);
                            }}
                          >
                            <span className="font-medium truncate">{faq.title}</span>
                            <ExternalLink className="w-3 h-3 shrink-0 ml-1 opacity-70" />
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                </div>
              );
            })}

            {loading && (
              <div className="flex gap-2.5 mr-auto max-w-[85%]">
                <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-indigo-600 to-cyan-500 flex items-center justify-center text-white">
                  <Bot className="w-3.5 h-3.5" />
                </div>
                <div className="p-3 bg-white border border-slate-200 rounded-2xl rounded-tl-xs flex items-center gap-1.5 text-xs text-slate-500">
                  <Loader2 className="w-3.5 h-3.5 text-indigo-600 animate-spin" />
                  <span>SupportAI is thinking...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick Prompts */}
          <div className="px-4 py-2 bg-white border-t border-slate-100 flex gap-1.5 overflow-x-auto text-[11px] whitespace-nowrap">
            <button
              onClick={() => setInput("How do I reset my password?")}
              className="px-2.5 py-1 rounded-full bg-slate-100 hover:bg-indigo-50 text-slate-600 hover:text-indigo-600 transition"
            >
              🔑 Password Reset
            </button>
            <button
              onClick={() => setInput("I have a question about my invoice and refund")}
              className="px-2.5 py-1 rounded-full bg-slate-100 hover:bg-indigo-50 text-slate-600 hover:text-indigo-600 transition"
            >
              💳 Billing Refund
            </button>
            <button
              onClick={() => setInput("What are the API rate limits?")}
              className="px-2.5 py-1 rounded-full bg-slate-100 hover:bg-indigo-50 text-slate-600 hover:text-indigo-600 transition"
            >
              ⚡ API Limits
            </button>
          </div>

          {/* Chat Input */}
          <form onSubmit={handleSend} className="p-3 bg-white border-t border-slate-200 flex items-center gap-2">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask a question or type 'agent'..."
              className="flex-1 px-3.5 py-2 text-xs sm:text-sm rounded-xl border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none"
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              className="p-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl transition disabled:opacity-50 shadow-sm"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>

        </div>
      )}
    </>
  );
};
