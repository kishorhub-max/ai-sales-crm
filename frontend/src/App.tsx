import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import AppLayout from '@/components/layout/AppLayout'

import LoginPage from '@/pages/auth/LoginPage'
import RegisterPage from '@/pages/auth/RegisterPage'
import DashboardPage from '@/pages/dashboard/DashboardPage'
import LeadsPage from '@/pages/leads/LeadsPage'
import LeadDetailPage from '@/pages/leads/LeadDetailPage'
import CustomersPage from '@/pages/customers/CustomersPage'
import CustomerDetailPage from '@/pages/customers/CustomerDetailPage'
import OpportunitiesPage from '@/pages/opportunities/OpportunitiesPage'
import ProductsPage from '@/pages/products/ProductsPage'
import OrdersPage from '@/pages/orders/OrdersPage'
import OrderDetailPage from '@/pages/orders/OrderDetailPage'
import InvoicesPage from '@/pages/invoices/InvoicesPage'
import AiCopilotPage from '@/pages/ai/AiCopilotPage'

export default function App() {
  return (
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* ── Public routes ── */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* ── Protected routes ── */}
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<DashboardPage />} />

                <Route path="/leads" element={<LeadsPage />} />
                <Route path="/leads/:id" element={<LeadDetailPage />} />

                <Route path="/customers" element={<CustomersPage />} />
                <Route path="/customers/:id" element={<CustomerDetailPage />} />

                <Route path="/opportunities" element={<OpportunitiesPage />} />

                <Route path="/products" element={<ProductsPage />} />

                <Route path="/orders" element={<OrdersPage />} />
                <Route path="/orders/:id" element={<OrderDetailPage />} />

                <Route path="/invoices" element={<InvoicesPage />} />

                <Route path="/ai-copilot" element={<AiCopilotPage />} />
              </Route>
            </Route>

            {/* ── Fallback ── */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
  )
}