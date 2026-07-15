import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Search, Trash2, Pencil, Package, AlertTriangle } from 'lucide-react'
import toast from 'react-hot-toast'
import { productsApi } from '@/api/products'
import { LoadingSpinner, EmptyState } from '@/components/ui/States'
import { formatCurrency } from '@/utils/formatters'
import ProductFormModal from './ProductFormModal'
import type { Product, ProductRequest } from '@/types'

export default function ProductsPage() {
    const queryClient = useQueryClient()
    const [search, setSearch] = useState('')
    const [category, setCategory] = useState('')
    const [modalOpen, setModalOpen] = useState(false)
    const [editing, setEditing] = useState<Product | null>(null)

    const { data, isLoading } = useQuery({
        queryKey: ['products', search, category],
        queryFn: () => productsApi.getAll({ search: search || undefined, category: category || undefined, size: 50, active: true }),
    })

    const { data: categories } = useQuery({
        queryKey: ['products', 'categories'],
        queryFn: productsApi.getCategories,
    })

    const createMutation = useMutation({
        mutationFn: productsApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] })
            toast.success('Product created')
            setModalOpen(false)
        },
    })

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: ProductRequest }) => productsApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] })
            toast.success('Product updated')
            setModalOpen(false)
            setEditing(null)
        },
    })

    const deleteMutation = useMutation({
        mutationFn: productsApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['products'] })
            toast.success('Product deactivated')
        },
    })

    const handleSubmit = (formData: ProductRequest) => {
        if (editing) updateMutation.mutate({ id: editing.id, data: formData })
        else createMutation.mutate(formData)
    }

    return (
        <div className="space-y-5 animate-fade-in">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">Products</h1>
                    <p className="mt-1 text-sm text-gray-500">Manage your product catalog</p>
                </div>
                <button onClick={() => { setEditing(null); setModalOpen(true) }} className="btn-primary">
                    <Plus className="h-4 w-4" /> New Product
                </button>
            </div>

            <div className="card flex flex-col gap-3 p-4 sm:flex-row">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                    <input placeholder="Search products..." className="input pl-10" value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="select sm:w-48" value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="">All Categories</option>
                    {categories?.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>
            </div>

            {isLoading ? (
                <LoadingSpinner />
            ) : !data || data.content.length === 0 ? (
                <div className="card"><EmptyState title="No products found" /></div>
            ) : (
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {data.content.map((p) => (
                        <div key={p.id} className="card p-4">
                            <div className="flex items-start justify-between">
                                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-50">
                                    <Package className="h-5 w-5 text-primary-600" />
                                </div>
                                <div className="flex gap-1">
                                    <button onClick={() => { setEditing(p); setModalOpen(true) }} className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-primary-600">
                                        <Pencil className="h-3.5 w-3.5" />
                                    </button>
                                    <button onClick={() => confirm('Deactivate this product?') && deleteMutation.mutate(p.id)} className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-600">
                                        <Trash2 className="h-3.5 w-3.5" />
                                    </button>
                                </div>
                            </div>

                            <h3 className="mt-3 font-semibold text-gray-900">{p.name}</h3>
                            <p className="text-xs text-gray-500">{p.category || 'Uncategorized'} {p.sku && `· ${p.sku}`}</p>

                            <div className="mt-3 flex items-center justify-between">
                                <p className="text-lg font-bold text-gray-900">{formatCurrency(p.price)}</p>
                                {p.marginPercent !== undefined && p.marginPercent !== null && (
                                    <span className="text-xs text-emerald-600 font-medium">{p.marginPercent.toFixed(0)}% margin</span>
                                )}
                            </div>

                            <div className="mt-2 flex items-center gap-1.5">
                                {p.stockQuantity <= 10 && (
                                    <AlertTriangle className="h-3.5 w-3.5 text-amber-500" />
                                )}
                                <span className={`text-xs ${p.stockQuantity <= 10 ? 'text-amber-600 font-medium' : 'text-gray-500'}`}>
                  {p.stockQuantity} in stock
                </span>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            <ProductFormModal
                isOpen={modalOpen}
                onClose={() => { setModalOpen(false); setEditing(null) }}
                onSubmit={handleSubmit}
                initialData={editing}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
            />
        </div>
    )
}