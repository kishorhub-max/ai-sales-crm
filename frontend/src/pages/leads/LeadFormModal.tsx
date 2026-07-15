import { useState, useEffect, FormEvent } from 'react'
import Modal from '@/components/ui/Modal'
import type { Lead, LeadRequest, LeadSource, LeadStatus } from '@/types'

interface LeadFormModalProps {
    isOpen: boolean
    onClose: () => void
    onSubmit: (data: LeadRequest) => void
    initialData?: Lead | null
    isSubmitting: boolean
}

const emptyForm: LeadRequest = {
    name: '', email: '', phone: '', company: '', jobTitle: '',
    source: 'WEBSITE', status: 'NEW', notes: '', website: '', industry: '',
}

export default function LeadFormModal({ isOpen, onClose, onSubmit, initialData, isSubmitting }: LeadFormModalProps) {
    const [form, setForm] = useState<LeadRequest>(emptyForm)

    useEffect(() => {
        if (initialData) {
            setForm({
                name: initialData.name, email: initialData.email, phone: initialData.phone || '',
                company: initialData.company || '', jobTitle: initialData.jobTitle || '',
                source: initialData.source, status: initialData.status, notes: initialData.notes || '',
                website: initialData.website || '', industry: initialData.industry || '',
                estimatedValue: initialData.estimatedValue,
            })
        } else {
            setForm(emptyForm)
        }
    }, [initialData, isOpen])

    const update = (field: keyof LeadRequest, value: any) =>
        setForm((prev) => ({ ...prev, [field]: value }))

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault()
        onSubmit(form)
    }

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={initialData ? 'Edit Lead' : 'Create Lead'} maxWidth="max-w-2xl">
            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Name *</label>
                        <input className="input" required value={form.name} onChange={(e) => update('name', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Email *</label>
                        <input type="email" className="input" required value={form.email} onChange={(e) => update('email', e.target.value)} />
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Phone</label>
                        <input className="input" value={form.phone} onChange={(e) => update('phone', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Company</label>
                        <input className="input" value={form.company} onChange={(e) => update('company', e.target.value)} />
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Job Title</label>
                        <input className="input" value={form.jobTitle} onChange={(e) => update('jobTitle', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Industry</label>
                        <input className="input" value={form.industry} onChange={(e) => update('industry', e.target.value)} />
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Source</label>
                        <select className="select" value={form.source} onChange={(e) => update('source', e.target.value as LeadSource)}>
                            <option value="WEBSITE">Website</option>
                            <option value="REFERRAL">Referral</option>
                            <option value="COLD_CALL">Cold Call</option>
                            <option value="EMAIL_CAMPAIGN">Email Campaign</option>
                            <option value="SOCIAL_MEDIA">Social Media</option>
                            <option value="TRADE_SHOW">Trade Show</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                    <div>
                        <label className="label">Status</label>
                        <select className="select" value={form.status} onChange={(e) => update('status', e.target.value as LeadStatus)}>
                            <option value="NEW">New</option>
                            <option value="CONTACTED">Contacted</option>
                            <option value="QUALIFIED">Qualified</option>
                            <option value="UNQUALIFIED">Unqualified</option>
                            <option value="CONVERTED">Converted</option>
                            <option value="LOST">Lost</option>
                        </select>
                    </div>
                </div>

                <div>
                    <label className="label">Estimated Value ($)</label>
                    <input type="number" className="input" value={form.estimatedValue || ''}
                           onChange={(e) => update('estimatedValue', parseFloat(e.target.value) || undefined)} />
                </div>

                <div>
                    <label className="label">Notes</label>
                    <textarea className="input" rows={3} value={form.notes} onChange={(e) => update('notes', e.target.value)} />
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={isSubmitting} className="btn-primary">
                        {isSubmitting ? 'Saving...' : initialData ? 'Update Lead' : 'Create Lead'}
                    </button>
                </div>
            </form>
        </Modal>
    )
}