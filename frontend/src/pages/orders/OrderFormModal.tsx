import { useState } from 'react'
import type { FormEvent } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, Trash2 } from 'lucide-react'
import Modal from '@/components/ui/Modal'
import { customersApi } from '@/api/customers'
import { productsApi } from '@/api/products'
import { formatCurrency } from '@/utils/formatters'
import type { OrderRequest, OrderItemRequest } from '@/types'

interface Props {
    isOpen: boolean
    onClose: () => void
    onSubmit: (data: OrderRequest) => void
    isSubmitting: boolean
}

export default function OrderFormModal({ isOpen, onClose, onSubmit, isSubmitting }: Props) {
    const [customerId, setCustomerId] = useState(0)
    const [items, setItems] = useState<OrderItemRequest[]>([{ productId: 0, quantity: 1 }])
    const [discountPercent, setDiscountPercent] = useState(0)
    const [taxPercent, setTaxPercent] = useState(0)
    const [notes, setNotes] = useState('')

    const { data: customersData } = useQuery({
        queryKey: ['customers', 'dropdown'],
        queryFn: () => customersApi.getAll({ size: 100, active: true }),
        enabled: isOpen,
    })

    const { data: productsData } = useQuery({
        queryKey: ['products', 'dropdown'],
        queryFn: () => productsApi.getAll({ size: 100, active: true }),
        enabled: isOpen,
    })

    const addItem = () => setItems((prev) => [...prev, { productId: 0, quantity: 1 }])
    const removeItem = (index: number) => setItems((prev) => prev.filter((_, i) => i !== index))
    const updateItem = (index: number, field: keyof OrderItemRequest, value: any) =>
        setItems((prev) => prev.map((item, i) => i === index ? { ...item, [field]: value } : item))

    const getProductPrice = (productId: number) =>
        productsData?.content.find((p) => p.id === productId)?.price || 0

    const subtotal = items.reduce((sum, item) => {
        const price = getProductPrice(item.productId)
        return sum + price * item.quantity
    }, 0)

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault()
        onSubmit({
            customerId,
            items: items.filter((i) => i.productId > 0),
            discountPercent,
            taxPercent,
            notes,
        })
    }

    const resetAndClose = () => {
        setCustomerId(0)
        setItems([{ productId: 0, quantity: 1 }])
        setDiscountPercent(0)
        setTaxPercent(0)
        setNotes('')
        onClose()
    }

    return (
        <Modal isOpen={isOpen} onClose={resetAndClose} title="Create Order" maxWidth="max-w-2xl">
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="label">Customer *</label>
                    <select className="select" required value={customerId} onChange={(e) => setCustomerId(Number(e.target.value))}>
                        <option value={0}>Select customer...</option>
                        {customersData?.content.map((c) => (
                            <option key={c.id} value={c.id}>{c.fullName} {c.company && `(${c.company})`}</option>
                        ))}
                    </select>
                </div>

                <div>
                    <div className="mb-2 flex items-center justify-between">
                        <label className="label mb-0">Line Items</label>
                        <button type="button" onClick={addItem} className="flex items-center gap-1 text-xs font-medium text-primary-600 hover:text-primary-700">
                            <Plus className="h-3.5 w-3.5" /> Add Item
                        </button>
                    </div>

                    <div className="space-y-2">
                        {items.map((item, index) => (
                            <div key={index} className="flex gap-2 items-start">
                                <select
                                    className="select flex-1"
                                    required
                                    value={item.productId}
                                    onChange={(e) => updateItem(index, 'productId', Number(e.target.value))}
                                >
                                    <option value={0}>Select product...</option>
                                    {productsData?.content.map((p) => (
                                        <option key={p.id} value={p.id}>{p.name} — {formatCurrency(p.price)}</option>
                                    ))}
                                </select>
                                <input
                                    type="number" min="1" className="input w-20"
                                    value={item.quantity}
                                    onChange={(e) => updateItem(index, 'quantity', parseInt(e.target.value) || 1)}
                                />
                                {items.length > 1 && (
                                    <button type="button" onClick={() => removeItem(index)} className="rounded-lg p-2 text-gray-400 hover:bg-red-50 hover:text-red-600">
                                        <Trash2 className="h-4 w-4" />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="label">Discount (%)</label>
                        <input type="number" min="0" max="100" className="input" value={discountPercent}
                               onChange={(e) => setDiscountPercent(parseFloat(e.target.value) || 0)} />
                    </div>
                    <div>
                        <label className="label">Tax (%)</label>
                        <input type="number" min="0" className="input" value={taxPercent}
                               onChange={(e) => setTaxPercent(parseFloat(e.target.value) || 0)} />
                    </div>
                </div>

                <div>
                    <label className="label">Notes</label>
                    <textarea className="input" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
                </div>

                {/* Live total preview */}
                <div className="rounded-lg bg-gray-50 p-3 text-sm">
                    <div className="flex justify-between text-gray-600"><span>Subtotal</span><span>{formatCurrency(subtotal)}</span></div>
                    <div className="flex justify-between text-gray-600"><span>Discount ({discountPercent}%)</span><span>-{formatCurrency(subtotal * discountPercent / 100)}</span></div>
                    <div className="flex justify-between font-semibold text-gray-900 mt-1 pt-1 border-t border-gray-200">
                        <span>Estimated Total</span>
                        <span>{formatCurrency((subtotal - subtotal * discountPercent / 100) * (1 + taxPercent / 100))}</span>
                    </div>
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    <button type="button" onClick={resetAndClose} className="btn-secondary">Cancel</button>
                    <button type="submit" disabled={isSubmitting || customerId === 0} className="btn-primary">
                        {isSubmitting ? 'Creating...' : 'Create Order'}
                    </button>
                </div>
            </form>
        </Modal>
    )
}