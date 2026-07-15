import { useState, useRef, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Bot, Send, Sparkles, Mail, Loader2, User as UserIcon } from 'lucide-react'
import { aiApi } from '@/api/ai'
import { LoadingSpinner } from '@/components/ui/States'
import type { ChatMessage } from '@/types'
import clsx from 'clsx'

type Tab = 'chat' | 'summary' | 'email'

export default function AiCopilotPage() {
    const [tab, setTab] = useState<Tab>('chat')

    return (
        <div className="space-y-5 animate-fade-in">
            <div>
                <h1 className="page-title flex items-center gap-2">
                    <Bot className="h-6 w-6 text-primary-600" /> AI Sales Copilot
                </h1>
                <p className="mt-1 text-sm text-gray-500">Your AI-powered sales assistant</p>
            </div>

            <div className="flex gap-2 border-b border-gray-200">
                {[
                    { key: 'chat',    label: 'Sales Assistant' },
                    { key: 'summary', label: 'Daily Summary' },
                    { key: 'email',   label: 'Email Generator' },
                ].map((t) => (
                    <button
                        key={t.key}
                        onClick={() => setTab(t.key as Tab)}
                        className={clsx(
                            'px-4 py-2.5 text-sm font-medium border-b-2 transition-colors',
                            tab === t.key ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'
                        )}
                    >
                        {t.label}
                    </button>
                ))}
            </div>

            {tab === 'chat' && <ChatTab />}
            {tab === 'summary' && <SummaryTab />}
            {tab === 'email' && <EmailTab />}
        </div>
    )
}

// ── Chat Tab ─────────────────────────────────────────────────────────────────

function ChatTab() {
    const [messages, setMessages] = useState<ChatMessage[]>([
        { role: 'model', content: "Hi! I'm your AI Sales Copilot. Ask me about your leads, deals, or customers." },
    ])
    const [input, setInput] = useState('')
    const scrollRef = useRef<HTMLDivElement>(null)

    const chatMutation = useMutation({
        mutationFn: (message: string) => aiApi.chat(message, messages),
        onSuccess: (response) => {
            setMessages((prev) => [...prev, { role: 'model', content: response.reply }])
        },
    })

    useEffect(() => {
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
    }, [messages])

    const handleSend = (e: FormEvent) => {
        e.preventDefault()
        if (!input.trim()) return
        const userMessage = input.trim()
        setMessages((prev) => [...prev, { role: 'user', content: userMessage }])
        setInput('')
        chatMutation.mutate(userMessage)
    }

    const quickPrompts = [
        'Which customers need follow-up?',
        'Which deals are at risk?',
        'Show top opportunities',
        'Summarize customer activity',
    ]

    return (
        <div className="card flex flex-col h-[600px]">
            <div ref={scrollRef} className="flex-1 overflow-y-auto p-5 space-y-4">
                {messages.map((msg, i) => (
                    <div key={i} className={clsx('flex gap-3', msg.role === 'user' && 'justify-end')}>
                        {msg.role === 'model' && (
                            <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary-100">
                                <Bot className="h-4 w-4 text-primary-600" />
                            </div>
                        )}
                        <div className={clsx(
                            'max-w-[75%] rounded-2xl px-4 py-2.5 text-sm whitespace-pre-wrap',
                            msg.role === 'user' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-800'
                        )}>
                            {msg.content}
                        </div>
                        {msg.role === 'user' && (
                            <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-gray-200">
                                <UserIcon className="h-4 w-4 text-gray-600" />
                            </div>
                        )}
                    </div>
                ))}
                {chatMutation.isPending && (
                    <div className="flex gap-3">
                        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary-100">
                            <Bot className="h-4 w-4 text-primary-600" />
                        </div>
                        <div className="rounded-2xl bg-gray-100 px-4 py-2.5">
                            <Loader2 className="h-4 w-4 animate-spin text-gray-400" />
                        </div>
                    </div>
                )}
            </div>

            {messages.length === 1 && (
                <div className="flex flex-wrap gap-2 px-5 pb-3">
                    {quickPrompts.map((p) => (
                        <button
                            key={p}
                            onClick={() => { setInput(p) }}
                            className="rounded-full border border-gray-200 px-3 py-1.5 text-xs text-gray-600 hover:bg-gray-50"
                        >
                            {p}
                        </button>
                    ))}
                </div>
            )}

            <form onSubmit={handleSend} className="flex gap-2 border-t border-gray-100 p-4">
                <input
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Ask about your sales pipeline..."
                    className="input flex-1"
                />
                <button type="submit" disabled={chatMutation.isPending || !input.trim()} className="btn-primary px-4">
                    <Send className="h-4 w-4" />
                </button>
            </form>
        </div>
    )
}

// ── Daily Summary Tab ────────────────────────────────────────────────────────

function SummaryTab() {
    const { data, isLoading, refetch, isFetching } = useQuery({
        queryKey: ['ai', 'daily-summary'],
        queryFn: aiApi.getDailySummary,
    })

    if (isLoading) return <LoadingSpinner label="Generating today's briefing..." />
    if (!data) return null

    return (
        <div className="space-y-4">
            <div className="flex justify-end">
                <button onClick={() => refetch()} disabled={isFetching} className="btn-secondary text-xs">
                    {isFetching ? 'Refreshing...' : 'Refresh Summary'}
                </button>
            </div>

            <div className="card p-5 border-primary-200 bg-primary-50/30">
                <div className="flex items-center gap-2 mb-2">
                    <Sparkles className="h-4 w-4 text-primary-600" />
                    <p className="text-sm font-semibold text-gray-900">Overview</p>
                </div>
                <p className="text-sm text-gray-700">{data.overallSummary}</p>
                <p className="mt-2 text-xs text-primary-700 font-medium">{data.forecastInsight}</p>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <SummaryList title="Top Priorities" items={data.topPriorities} />
                <SummaryList title="At-Risk Deals" items={data.atRiskDeals} variant="danger" />
                <SummaryList title="Follow-Up Required" items={data.followUpRequired} />
                <SummaryList title="Recommendations" items={data.recommendations} variant="success" />
            </div>
        </div>
    )
}

function SummaryList({ title, items, variant = 'default' }: { title: string; items: string[]; variant?: 'default' | 'danger' | 'success' }) {
    const dotColor = variant === 'danger' ? 'bg-red-500' : variant === 'success' ? 'bg-emerald-500' : 'bg-primary-500'
    return (
        <div className="card p-4">
            <p className="text-sm font-semibold text-gray-900 mb-2">{title}</p>
            {items.length === 0 ? (
                <p className="text-xs text-gray-400">None right now</p>
            ) : (
                <ul className="space-y-1.5">
                    {items.map((item, i) => (
                        <li key={i} className="flex gap-2 text-sm text-gray-600">
                            <span className={clsx('mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full', dotColor)} />
                            {item}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}

// ── Email Generator Tab ──────────────────────────────────────────────────────

function EmailTab() {
    const [emailType, setEmailType] = useState<'FOLLOW_UP' | 'PROPOSAL' | 'ENGAGEMENT' | 'THANK_YOU'>('FOLLOW_UP')
    const [customerId, setCustomerId] = useState('')
    const [context, setContext] = useState('')

    const mutation = useMutation({
        mutationFn: () => aiApi.generateEmail(emailType, Number(customerId), undefined, context || undefined),
    })

    return (
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
            <div className="card p-5 space-y-4">
                <div>
                    <label className="label">Email Type</label>
                    <select className="select" value={emailType} onChange={(e) => setEmailType(e.target.value as any)}>
                        <option value="FOLLOW_UP">Follow-Up</option>
                        <option value="PROPOSAL">Proposal</option>
                        <option value="ENGAGEMENT">Engagement</option>
                        <option value="THANK_YOU">Thank You</option>
                    </select>
                </div>
                <div>
                    <label className="label">Customer ID</label>
                    <input type="number" className="input" placeholder="Enter customer ID" value={customerId} onChange={(e) => setCustomerId(e.target.value)} />
                </div>
                <div>
                    <label className="label">Additional Context (optional)</label>
                    <textarea className="input" rows={4} placeholder="Any specific details to include..." value={context} onChange={(e) => setContext(e.target.value)} />
                </div>
                <button
                    onClick={() => mutation.mutate()}
                    disabled={mutation.isPending || !customerId}
                    className="btn-primary w-full justify-center"
                >
                    {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Mail className="h-4 w-4" />}
                    {mutation.isPending ? 'Generating...' : 'Generate Email'}
                </button>
            </div>

            <div className="card p-5">
                <p className="label">Generated Email</p>
                {!mutation.data ? (
                    <div className="flex h-full items-center justify-center text-sm text-gray-400 py-12">
                        Fill in the form and click generate
                    </div>
                ) : (
                    <div className="space-y-3">
                        <div>
                            <p className="text-xs text-gray-500">Subject</p>
                            <p className="text-sm font-semibold text-gray-900">{mutation.data.subject}</p>
                        </div>
                        <div className="rounded-lg bg-gray-50 p-3">
                            <p className="text-sm text-gray-700 whitespace-pre-wrap">{mutation.data.body}</p>
                        </div>
                        {mutation.data.suggestedSubjectLines.length > 0 && (
                            <div>
                                <p className="text-xs font-semibold text-gray-500 mb-1">ALTERNATE SUBJECT LINES</p>
                                {mutation.data.suggestedSubjectLines.map((s, i) => (
                                    <p key={i} className="text-xs text-gray-500">• {s}</p>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    )
}