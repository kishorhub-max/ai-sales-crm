// ── Enums ─────────────────────────────────────────────────────────────────────
export type Role = 'ADMIN' | 'SALES_MANAGER' | 'SALES_REPRESENTATIVE'
export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'UNQUALIFIED' | 'CONVERTED' | 'LOST'
export type LeadSource = 'WEBSITE' | 'REFERRAL' | 'COLD_CALL' | 'EMAIL_CAMPAIGN' | 'SOCIAL_MEDIA' | 'TRADE_SHOW' | 'OTHER'
export type OpportunityStage = 'NEW' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'WON' | 'LOST'
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED'
export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED'

// ── Auth ──────────────────────────────────────────────────────────────────────
export interface AuthResponse {
    accessToken: string
    refreshToken: string
    tokenType: string
    userId: number
    firstName: string
    lastName: string
    email: string
    role: Role
}

export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    firstName: string
    lastName: string
    email: string
    password: string
    role: Role
    phone?: string
}

// ── Common ────────────────────────────────────────────────────────────────────
export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface User {
    id: number
    firstName: string
    lastName: string
    email: string
    role: Role
    phone?: string
    active: boolean
}

// ── Lead ──────────────────────────────────────────────────────────────────────
export interface Lead {
    id: number
    name: string
    email: string
    phone?: string
    company?: string
    jobTitle?: string
    source: LeadSource
    status: LeadStatus
    notes?: string
    website?: string
    industry?: string
    employeeCount?: number
    estimatedValue?: number
    assignedToId?: number
    assignedToName?: string
    assignedToEmail?: string
    createdAt: string
    updatedAt: string
    createdBy?: string
}

export interface LeadRequest {
    name: string
    email: string
    phone?: string
    company?: string
    jobTitle?: string
    source: LeadSource
    status?: LeadStatus
    assignedToId?: number
    notes?: string
    website?: string
    industry?: string
    employeeCount?: number
    estimatedValue?: number
}

// ── Customer ──────────────────────────────────────────────────────────────────
export interface Customer {
    id: number
    firstName: string
    lastName: string
    fullName: string
    email: string
    phone?: string
    mobile?: string
    company?: string
    jobTitle?: string
    industry?: string
    website?: string
    street?: string
    city?: string
    state?: string
    country?: string
    zipCode?: string
    notes?: string
    active: boolean
    totalPurchaseValue: number
    purchaseCount: number
    assignedToId?: number
    assignedToName?: string
    convertedFromLeadId?: number
    createdAt: string
    updatedAt: string
}

export interface CustomerRequest {
    firstName: string
    lastName: string
    email: string
    phone?: string
    mobile?: string
    company?: string
    jobTitle?: string
    industry?: string
    website?: string
    street?: string
    city?: string
    state?: string
    country?: string
    zipCode?: string
    notes?: string
    assignedToId?: number
    active?: boolean
}

// ── Product ───────────────────────────────────────────────────────────────────
export interface Product {
    id: number
    name: string
    sku?: string
    category?: string
    description?: string
    price: number
    costPrice?: number
    stockQuantity: number
    unit?: string
    active: boolean
    margin?: number
    marginPercent?: number
    createdAt: string
    updatedAt: string
}

export interface ProductRequest {
    name: string
    sku?: string
    category?: string
    description?: string
    price: number
    costPrice?: number
    stockQuantity?: number
    unit?: string
    active?: boolean
}

// ── Opportunity ───────────────────────────────────────────────────────────────
export interface Opportunity {
    id: number
    dealName: string
    value: number
    stage: OpportunityStage
    probability: number
    expectedCloseDate?: string
    description?: string
    lostReason?: string
    weightedValue: number
    customerId: number
    customerName: string
    customerCompany?: string
    assignedToId?: number
    assignedToName?: string
    productId?: number
    productName?: string
    createdAt: string
    updatedAt: string
}

export interface OpportunityRequest {
    dealName: string
    value: number
    stage?: OpportunityStage
    probability?: number
    expectedCloseDate?: string
    customerId: number
    assignedToId?: number
    productId?: number
    description?: string
    lostReason?: string
}

// ── Order ─────────────────────────────────────────────────────────────────────
export interface OrderItem {
    id: number
    productId: number
    productName: string
    productSku?: string
    quantity: number
    unitPrice: number
    discountPercent: number
    discountAmount: number
    totalPrice: number
}

export interface Order {
    id: number
    orderNumber: string
    status: OrderStatus
    customerId: number
    customerName: string
    customerCompany?: string
    assignedToId?: number
    assignedToName?: string
    items: OrderItem[]
    subtotal: number
    discountPercent: number
    discountAmount: number
    taxPercent: number
    taxAmount: number
    total: number
    shippingAddress?: string
    shippingCity?: string
    shippingCountry?: string
    notes?: string
    invoiceId?: number
    invoiceNumber?: string
    createdAt: string
    updatedAt: string
}

export interface OrderItemRequest {
    productId: number
    quantity: number
    unitPrice?: number
    discountPercent?: number
}

export interface OrderRequest {
    customerId: number
    assignedToId?: number
    items: OrderItemRequest[]
    discountPercent?: number
    taxPercent?: number
    notes?: string
    shippingAddress?: string
    shippingCity?: string
    shippingCountry?: string
}

// ── Invoice ───────────────────────────────────────────────────────────────────
export interface Invoice {
    id: number
    invoiceNumber: string
    status: InvoiceStatus
    customerId: number
    customerName: string
    customerCompany?: string
    customerEmail: string
    orderId?: number
    orderNumber?: string
    issueDate: string
    dueDate: string
    paidDate?: string
    subtotal: number
    taxPercent: number
    taxAmount: number
    discountPercent: number
    discountAmount: number
    total: number
    paymentMethod?: string
    notes?: string
    overdue: boolean
    daysUntilDue: number
    createdAt: string
    updatedAt: string
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
export interface DashboardStats {
    totalRevenue: number
    outstandingRevenue: number
    activeCustomers: number
    openOpportunities: number
    pipelineValue: number
    totalLeads: number
    newLeadsThisMonth: number
    overdueInvoices: number
    conversionRate: number
    monthlyRevenue: { month: string; revenue: number; orderCount: number }[]
    opportunityPipeline: { stage: string; count: number; value: number }[]
    leadsByStatus: Record<string, number>
    ordersByStatus: Record<string, { count: number; revenue: number }>
    topCustomers: Customer[]
    recentLeads: Lead[]
    recentOrders: Order[]
}

// ── AI ────────────────────────────────────────────────────────────────────────
export interface ChatMessage {
    role: 'user' | 'model'
    content: string
}

export interface ChatResponse {
    reply: string
    model: string
    tokensUsed: number
}

export interface CustomerInsightResponse {
    customerId: number
    customerName: string
    healthScore: number
    churnRisk: number
    purchaseProbability: number
    healthScoreLabel: string
    churnRiskLabel: string
    purchaseProbabilityLabel: string
    insights: string
    upsellSuggestions: string[]
    recommendedActions: string[]
    summary: string
}

export interface OpportunityAnalysisResponse {
    opportunityId: number
    dealName: string
    winProbability: number
    riskLevel: string
    risks: string[]
    strengths: string[]
    nextRecommendedAction: string
    suggestedActions: string[]
    competitiveAnalysis: string
    summary: string
}

export interface EmailGeneratorResponse {
    emailType: string
    subject: string
    body: string
    suggestedSubjectLines: string[]
}

export interface SalesSummaryResponse {
    overallSummary: string
    topPriorities: string[]
    atRiskDeals: string[]
    followUpRequired: string[]
    recommendations: string[]
    forecastInsight: string
}