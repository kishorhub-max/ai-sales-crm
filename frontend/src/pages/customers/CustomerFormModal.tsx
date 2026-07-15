import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import Modal from '@/components/ui/Modal'
import type { Customer, CustomerRequest } from '@/types'

interface Props {
    isOpen: boolean
    onClose: () => void
    onSubmit: (data: CustomerRequest) => void
    initialData?: Customer | null
    isSubmitting: boolean
}

const emptyForm: CustomerRequest = {
    firstName: '', lastName: '', email: '', phone: '', company: '',
    jobTitle: '', industry: '', city: '', country: '', notes: '', active: true,
}

export default function CustomerFormModal({ isOpen, onClose, onSubmit, initialData, isSubmitting }: Props) {
    const [form, setForm] = useState<CustomerRequest>(emptyForm)

    useEffect(() => {
        if (initialData) {
            setForm({
                firstName: initialData.firstName, lastName: initialData.lastName,
                email: initialData.email, phone: initialData.phone || '',
                company: initialData.company || '', jobTitle: initialData.jobTitle || '',
                industry: initialData.industry || '', city: initialData.city || '',
                country: initialData.country || '', notes: initialData.notes || '',
                active: initialData.active,
            })
        } else setForm(emptyForm)
    }, [initialData, isOpen])

    const update = (field: keyof CustomerRequest, value: any) =>
        setForm((prev) => ({ ...prev, [field]: value }))

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={initialData ? 'Edit Customer' : 'Create Customer'} maxWidth="max-w-2xl">
            <form onSubmit={(e: FormEvent) => { e.preventDefault(); onSubmit(form) }} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">First Name *</label>
                        <input className="input" required value={form.firstName} onChange={(e) => update('firstName', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Last Name *</label>
                        <input className="input" required value={form.lastName} onChange={(e) => update('lastName', e.target.value)} />
                    </div>
                </div>

                <div>
                    <label className="label">Email *</label>
                    <input type="email" className="input" required value={form.email} onChange={(e) => update('email', e.target.value)} />
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
                        <label className="label">City</label>
                        <input className="input" value={form.city} onChange={(e) => update('city', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Country</label>
                        <input className="input" value={form.country} onChange={(e) => update('country', e.target.value)} />
                    </div>
                </div>

                <div>
                    <label className="label">Notes</label>
                    <textarea className="input" rows={3} value={form.notes} onChange={(e) => update('notes', e.target.value)} />
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={isSubmitting} className="btn-primary">
                        {isSubmitting ? 'Saving...' : initialData ? 'Update Customer' : 'Create Customer'}
                    </button>
                </div>
            </form>
        </Modal>
    )
}