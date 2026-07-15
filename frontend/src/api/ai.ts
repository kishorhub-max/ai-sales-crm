import api from './axios'
import type {
    ChatMessage, ChatResponse, CustomerInsightResponse,
    OpportunityAnalysisResponse, EmailGeneratorResponse, SalesSummaryResponse,
} from '@/types'

export const aiApi = {
    chat: (message: string, history: ChatMessage[] = []) =>
        api
            .post<ChatResponse>('/ai/chat', { message, history })
            .then((r) => r.data),

    getCustomerInsights: (customerId: number) =>
        api
            .get<CustomerInsightResponse>(`/ai/customer-insights/${customerId}`)
            .then((r) => r.data),

    getOpportunityAnalysis: (opportunityId: number) =>
        api
            .get<OpportunityAnalysisResponse>(`/ai/opportunity-analysis/${opportunityId}`)
            .then((r) => r.data),

    generateEmail: (
        emailType: 'FOLLOW_UP' | 'PROPOSAL' | 'ENGAGEMENT' | 'THANK_YOU',
        customerId: number,
        opportunityId?: number,
        additionalContext?: string
    ) =>
        api
            .post<EmailGeneratorResponse>('/ai/generate-email', {
                emailType, customerId, opportunityId, additionalContext,
            })
            .then((r) => r.data),

    getDailySummary: () =>
        api.get<SalesSummaryResponse>('/ai/daily-summary').then((r) => r.data),
}