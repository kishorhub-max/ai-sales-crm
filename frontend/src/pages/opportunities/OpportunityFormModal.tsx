import { useState, useEffect, FormEvent } from 'react'
import { useQuery } from '@tanstack/react-query'
import Modal from '@/components/ui/Modal'
import { customersApi } from '@/api/customers'
import type { Opportunity, OpportunityRequest, OpportunityStage } from '@/types'

interface Props {
    isOpen: boolean
    onClose: () => void
    onSubmit: (data: OpportunityRequest) => void
    initialData?: Opportunity | null
    isSubmitting: boolean
}

const emptyForm: OpportunityRequest = {
    dealName: '', value: 0, stage: 'NEW', customerId: 0, description: '',
}

export default function OpportunityFormModal({ isOpen, onClose, onSubmit, initialData, isSubmitting }: Props) {
    const [form, setForm] = useState<OpportunityRequest>(emptyForm)

    const { data: customersData } = useQuery({
        queryKey: ['customers', 'dropdown'],
        queryFn: () => customersApi.getAll({ size: 100, active: true }),
        enabled: isOpen,
    })

    useEffect(() => {
        if (initialData) {
            setForm({
                dealName: initialData.dealName, value: initialData.value, stage: initialData.stage,
                probability: initialData.probability, expectedCloseDate: initialData.expectedCloseDate,
                customerId: initialData.customerId, description: initialData.description || '',
            })
        } else setForm(emptyForm)
    }, [initialData, isOpen])

    const update = (field: keyof OpportunityRequest, value: any) =>
        setForm((prev) => ({ ...prev, [field]: value }))

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={initialData ? 'Edit Opportunity' : 'Create Opportunity'} maxWidth="max-w-xl">
            <form onSubmit={(e: FormEvent) => { e.preventDefault(); onSubmit(form) }} className="space-y-4">
                <div>
                    <label className="label">Deal Name *</label>
                    <input className="input" required value={form.dealName} onChange={(e) => update('dealName', e.target.value)} />
                </div>

                <div>
                    <label className="label">Customer *</label>
                    <select className="select" required value={form.customerId} onChange={(e) => update('customerId', Number(e.target.value))}>
                        <option value={0}>Select customer...</option>
                        {customersData?.content.map((c) => (
                            <option key={c.id} value={c.id}>{c.fullName} {c.company && `(${c.company})`}</option>
                        ))}
                    </select>
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Value ($) *</label>
                        <input type="number" min="0" step="0.01" className="input" required
                               value={form.value} onChange={(e) => update('value', parseFloat(e.target.value) || 0)} />
                    </div>
                    <div>
                        <label className="label">Stage</label>
                        <select className="select" value={form.stage} onChange={(e) => update('stage', e.target.value as OpportunityStage)}>
                            <option value="NEW">New</option>
                            <option value="QUALIFIED">Qualified</option>
                            <option value="PROPOSAL">Proposal</option>
                            <option value="NEGOTIATION">Negotiation</option>
                            <option value="WON">Won</option>
                            <option value="LOST">Lost</option>
                        </select>
                    </div>
                </div>

                <div>
                    <label className="label">Expected Close Date</label>
                    <input type="date" className="input" value={form.expectedCloseDate || ''}
                           onChange={(e) => update('expectedCloseDate', e.target.value)} />
                </div>

                <div>
                    <label className="label">Description</label>
                    <textarea className="input" rows={3} value={form.description} onChange={(e) => update('description', e.target.value)} />
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={isSubmitting} className="btn-primary">
                        {isSubmitting ? 'Saving...' : initialData ? 'Update Deal' : 'Create Deal'}
                    </button>
                </div>
            </form>
        </Modal>
    )
}