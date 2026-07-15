import { useState, useEffect, FormEvent } from 'react'
import Modal from '@/components/ui/Modal'
import type { Product, ProductRequest } from '@/types'

interface Props {
    isOpen: boolean
    onClose: () => void
    onSubmit: (data: ProductRequest) => void
    initialData?: Product | null
    isSubmitting: boolean
}

const emptyForm: ProductRequest = {
    name: '', sku: '', category: '', description: '', price: 0,
    costPrice: undefined, stockQuantity: 0, unit: '', active: true,
}

export default function ProductFormModal({ isOpen, onClose, onSubmit, initialData, isSubmitting }: Props) {
    const [form, setForm] = useState<ProductRequest>(emptyForm)

    useEffect(() => {
        if (initialData) {
            setForm({
                name: initialData.name, sku: initialData.sku || '', category: initialData.category || '',
                description: initialData.description || '', price: initialData.price,
                costPrice: initialData.costPrice, stockQuantity: initialData.stockQuantity,
                unit: initialData.unit || '', active: initialData.active,
            })
        } else setForm(emptyForm)
    }, [initialData, isOpen])

    const update = (field: keyof ProductRequest, value: any) =>
        setForm((prev) => ({ ...prev, [field]: value }))

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={initialData ? 'Edit Product' : 'Create Product'} maxWidth="max-w-xl">
            <form onSubmit={(e: FormEvent) => { e.preventDefault(); onSubmit(form) }} className="space-y-4">
                <div>
                    <label className="label">Product Name *</label>
                    <input className="input" required value={form.name} onChange={(e) => update('name', e.target.value)} />
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">SKU</label>
                        <input className="input" value={form.sku} onChange={(e) => update('sku', e.target.value)} />
                    </div>
                    <div>
                        <label className="label">Category</label>
                        <input className="input" value={form.category} onChange={(e) => update('category', e.target.value)} />
                    </div>
                </div>

                <div className="grid grid-cols-3 gap-3">
                    <div>
                        <label className="label">Price ($) *</label>
                        <input type="number" min="0" step="0.01" className="input" required
                               value={form.price} onChange={(e) => update('price', parseFloat(e.target.value) || 0)} />
                    </div>
                    <div>
                        <label className="label">Cost Price ($)</label>
                        <input type="number" min="0" step="0.01" className="input"
                               value={form.costPrice ?? ''} onChange={(e) => update('costPrice', parseFloat(e.target.value) || undefined)} />
                    </div>
                    <div>
                        <label className="label">Stock Qty</label>
                        <input type="number" min="0" className="input"
                               value={form.stockQuantity} onChange={(e) => update('stockQuantity', parseInt(e.target.value) || 0)} />
                    </div>
                </div>

                <div>
                    <label className="label">Unit</label>
                    <input className="input" placeholder="e.g. license, each, kg" value={form.unit} onChange={(e) => update('unit', e.target.value)} />
                </div>

                <div>
                    <label className="label">Description</label>
                    <textarea className="input" rows={3} value={form.description} onChange={(e) => update('description', e.target.value)} />
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={isSubmitting} className="btn-primary">
                        {isSubmitting ? 'Saving...' : initialData ? 'Update Product' : 'Create Product'}
                    </button>
                </div>
            </form>
        </Modal>
    )
}