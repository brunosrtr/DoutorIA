import axios from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type {
  PatientResponse,
  AssessmentResponse,
  AssessmentResultResponse,
  AssessmentSummaryResponse,
  PageResponse,
  SymptomCatalogItem,
  CreatePatientRequest,
  SymptomItemRequest,
  AddSymptomsResponse,
  ReportResponse,
  SubmitAnswersRequest,
  HealthSummaryResponse,
} from '@/types/api';

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

// ── Health Summary ────────────────────────────────────────────────────────────

export function useHealthSummary() {
  return useQuery<HealthSummaryResponse>({
    queryKey: ['health-summary'],
    queryFn: async () => (await api.get('/api/health-summary')).data,
    staleTime: 5 * 60_000,
  });
}

export function useRefreshHealthSummary() {
  const qc = useQueryClient();
  return useMutation<HealthSummaryResponse, Error, void>({
    mutationFn: async () => (await api.post('/api/health-summary/refresh')).data,
    onSuccess: (data) => qc.setQueryData(['health-summary'], data),
  });
}

// ── Profile (single-user) ─────────────────────────────────────────────────────

export function useProfile() {
  return useQuery<PatientResponse | null>({
    queryKey: ['profile'],
    queryFn: async () => {
      const res = await api.get('/api/profile', { validateStatus: (s) => s === 200 || s === 204 });
      return res.status === 204 ? null : res.data;
    },
  });
}

export function useSaveProfile() {
  const qc = useQueryClient();
  return useMutation<PatientResponse, Error, CreatePatientRequest>({
    mutationFn: async (data) => (await api.put('/api/profile', data)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['profile'] }),
  });
}

// ── Patients (legacy) ─────────────────────────────────────────────────────────

export function usePatients() {
  return useQuery<PatientResponse[]>({
    queryKey: ['patients'],
    queryFn: async () => (await api.get('/api/patients')).data,
  });
}

export function useCreatePatient() {
  const qc = useQueryClient();
  return useMutation<PatientResponse, Error, CreatePatientRequest>({
    mutationFn: async (data) => (await api.post('/api/patients', data)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['patients'] }),
  });
}

// ── Assessments ───────────────────────────────────────────────────────────────

export function useCreateAssessment() {
  return useMutation<AssessmentResponse, Error, string>({
    mutationFn: async (patientId) =>
      (await api.post('/api/assessments', { patientId })).data,
  });
}

export function useAddSymptoms() {
  return useMutation<
    AddSymptomsResponse,
    Error,
    { assessmentId: string; sintomas: SymptomItemRequest[] }
  >({
    mutationFn: async ({ assessmentId, sintomas }) =>
      (await api.post(`/api/assessments/${assessmentId}/symptoms`, { sintomas })).data,
  });
}

export function useSubmitAssessment() {
  return useMutation<AssessmentResultResponse, Error, string>({
    mutationFn: async (assessmentId) =>
      (await api.post(`/api/assessments/${assessmentId}/submit`)).data,
  });
}

export function useSubmitAnswers() {
  const qc = useQueryClient();
  return useMutation<
    AssessmentResultResponse,
    Error,
    { assessmentId: string; respostas: SubmitAnswersRequest['respostas'] }
  >({
    mutationFn: async ({ assessmentId, respostas }) =>
      (await api.post(`/api/assessments/${assessmentId}/answers`, { respostas })).data,
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ['assessment-result', vars.assessmentId] });
    },
  });
}

export function useAssessmentResult(assessmentId: string | null) {
  return useQuery<AssessmentResultResponse>({
    queryKey: ['assessment-result', assessmentId],
    queryFn: async () =>
      (await api.get(`/api/assessments/${assessmentId}/result`)).data,
    enabled: !!assessmentId,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return false;
      if (data.status === 'processando' && !data.precisaMaisInfo) return 3000;
      return false;
    },
  });
}

// ── Reports ───────────────────────────────────────────────────────────────────

export function useUploadReport() {
  return useMutation<ReportResponse, Error, { assessmentId: string; file: File }>({
    mutationFn: async ({ assessmentId, file }) => {
      const form = new FormData();
      form.append('file', file);
      return (
        await api.post(`/api/assessments/${assessmentId}/reports`, form, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
      ).data;
    },
  });
}

export function useAssessmentHistory(page = 0) {
  return useQuery<PageResponse<AssessmentSummaryResponse>>({
    queryKey: ['assessment-history', page],
    queryFn: async () =>
      (await api.get(`/api/assessments/history?page=${page}&size=20`)).data,
  });
}

export function useDeleteAssessment() {
  const qc = useQueryClient();
  return useMutation<void, Error, string>({
    mutationFn: async (assessmentId) => {
      await api.delete(`/api/assessments/${assessmentId}`);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ['assessment-history'] }),
  });
}

// ── Symptoms Catalog ──────────────────────────────────────────────────────────

export function useSymptomSearch(query: string, categoria?: string) {
  return useQuery<SymptomCatalogItem[]>({
    queryKey: ['symptoms', query, categoria],
    queryFn: async () => {
      const params = new URLSearchParams({ q: query });
      if (categoria) params.append('categoria', categoria);
      return (await api.get(`/api/symptoms?${params}`)).data;
    },
    enabled: query.length >= 2,
    staleTime: 30_000,
  });
}
